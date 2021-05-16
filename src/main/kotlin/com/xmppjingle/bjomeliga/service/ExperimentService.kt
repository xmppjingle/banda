package com.xmppjingle.bjomeliga.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.dynamic.Commands
import io.lettuce.core.dynamic.RedisCommandFactory
import io.lettuce.core.dynamic.annotation.Command
import io.lettuce.core.dynamic.annotation.Param
import org.apache.commons.math3.distribution.EnumeratedDistribution
import org.apache.commons.math3.util.Pair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class ExperimentService {

    var logger: Logger = LoggerFactory.getLogger(ExperimentService::class.java)

    lateinit var redisClient: RedisClient
    lateinit var connection: StatefulRedisConnection<String, String>
    lateinit var factory: RedisCommandFactory
    lateinit var commands: JSonCommands
    lateinit var graphCommand: GraphCommands
    lateinit var baseCommands: RedisCommands<String, String>
    lateinit var gson: Gson

    val variantsEntropy = hashMapOf<String, EnumeratedDistribution<VariantDTO>>()
    val triggers = hashMapOf<String, List<String>>()

    @Autowired
    lateinit var remoteConfigService: RemoteConfigService

    @Value("\${redis.url:redis://localhost:6379}")
    var redisURI: String = "redis://localhost:6379"

    @PostConstruct
    fun init() {
        redisClient = RedisClient.create(redisURI)
        connection = redisClient.connect()
        factory = RedisCommandFactory(connection)
        commands = factory.getCommands(JSonCommands::class.java)
        graphCommand = factory.getCommands(GraphCommands::class.java)
        gson = GsonBuilder().create()
        baseCommands = connection.sync()
    }

    fun addExperiment(experimentDTO: ExperimentDTO) {
        commands.setObject(experimentDTO.id, gson.toJson(experimentDTO))
        addTrigger(experimentDTO)
    }

    fun getExperiment(id: String) =
        commands.getObject(id)?.let {
            gson.fromJson(it, ExperimentDTO::class.java)
        }

    fun removeExperiment(id: String): Boolean =
        getExperiment(id)?.let {
            baseCommands.del(id)
            removeTrigger(it)
            true
        } ?: false

    fun addTrigger(experimentDTO: ExperimentDTO) {
        baseCommands.hset(experimentDTO.triggerEventId, experimentDTO.id, "active")
    }

    fun removeTrigger(experimentDTO: ExperimentDTO) {
        baseCommands.hdel(experimentDTO.triggerEventId, experimentDTO.id)
    }

    fun getExperimentsForTriggerId(triggerEventId: String) =
        triggers[triggerEventId] ?: baseCommands.hgetall(triggerEventId).keys.toList().let {
            triggers[triggerEventId] = it
            it
        }

    fun fetchVariants(experimentId: String) =
        gson.fromJson(commands.getPathValue(experimentId, ".variants"), VariantsDTO::class.java)

    fun checkTriggers(event: EventDTO) {
        getExperimentsForTriggerId(event.id).forEach { experimentId ->
            if (!isEmitterEnrolled(event.emitterId, experimentId)) {
                enrollEmitterOnExperiment(event.emitterId, experimentId)
            }
        }
    }

    fun isEmitterEnrolled(emitterId: String, experimentId: String) =
        baseCommands.hexists("EXP-$emitterId", experimentId)

    fun enrollEmitterOnExperiment(emitterId: String, experimentId: String) {
        graphCommand.graphQuery("MERGE (:User {id: '$emitterId' })-[:participants]->(:Exp {id: '$experimentId' })")
        rolloutVariants(experimentId, emitterId)
    }

    fun fetchParticipantsOnExperiment(experimentId: String) =
        graphCommand.graphQueryString("MATCH  (u:User)-[:participants]->(:Exp {id: '$experimentId'}) RETURN COUNT(u.id)")

    private fun rolloutVariants(experimentId: String, emitterId: String) =
        sampleVariants(experimentId).sample().let { variant ->
            remoteConfigService.setParams(
                ConfigDTO(emitterId, variant.params)
            )
            variant
        }

    private fun sampleVariants(experimentId: String) =
        variantsEntropy[experimentId] ?: fetchVariants(experimentId)?.variants?.map { Pair(it, it.weight.toDouble()) }
            ?.toList().let { items ->
                val sample = EnumeratedDistribution(items)
                variantsEntropy[experimentId] = sample
                sample
            }

}

interface JSonCommands : Commands {

    @Command("JSON.SET :id . :json")
    fun setObject(
        @Param("id") id: String,
        @Param("json") json: String
    )

    @Command("JSON.GET :id . ")
    fun getObject(
        @Param("id") id: String
    ): String?

    @Command("JSON.SET :id :path :value")
    fun setPathVale(
        @Param("id") id: String,
        @Param("path") path: String,
        @Param("value") value: String
    )

    @Command("JSON.GET :id :path")
    fun getPathValue(
        @Param("id") id: String,
        @Param("path") path: String
    ): String

}

interface GraphCommands : Commands {

    @Command("GRAPH.QUERY experiments :cmd")
    fun graphQuery(
        @Param("cmd") cmd: String
    )

    @Command("GRAPH.QUERY experiments :cmd")
    fun graphQueryString(
        @Param("cmd") cmd: String
    ): String

}

data class VariantsDTO(
    val variants: List<VariantDTO>
)

data class VariantDTO(
    val id: String,
    val weight: Int,
    val params: Map<String, String>
)

data class ExperimentDTO(
    val id: String,
    val variants: VariantsDTO,
    val triggerEventId: String,
    val goalIds: List<String>
)
