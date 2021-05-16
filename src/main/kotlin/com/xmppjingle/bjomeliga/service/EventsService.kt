package com.xmppjingle.bjomeliga.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.dynamic.Commands
import io.lettuce.core.dynamic.RedisCommandFactory
import io.lettuce.core.dynamic.annotation.Command
import io.lettuce.core.dynamic.annotation.Param
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class EventsService {

    var logger: Logger = LoggerFactory.getLogger(EventsService::class.java)

    lateinit var redisClient: RedisClient
    lateinit var connection: StatefulRedisConnection<String, String>
    lateinit var factory: RedisCommandFactory
    lateinit var commands: EventCommands

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    lateinit var experimentService: ExperimentService

    @Value("\${redis.url:redis://localhost:6379}")
    var redisURI: String = "redis://localhost:6379"

    @PostConstruct
    fun init() {
        redisClient = RedisClient.create(redisURI)
        connection = redisClient.connect()
        factory = RedisCommandFactory(connection)
        commands = factory.getCommands(EventCommands::class.java)
    }

    fun emitEvent(eventDTO: EventDTO) {
        with(eventDTO) {
            with(commands) {
                pushEvent(
                        "event:${id}", //"$id:$emitterId",
                        category,
                        type,
                        retention,
                        hashMapOf<String, String>("id" to id).let {
                            it.putAll(labels)
                            it
                        },
                        value
                    )
            }
            experimentService.checkTriggers(eventDTO)
        }
    }

}

interface EventCommands : Commands {

    @Command("TS.ADD :id * :value RETENTION :retention LABELS category :category type :type :labels")
    fun pushEvent(
        @Param("id") id: String,
        @Param("category") category: String,
        @Param("type") type: String,
        @Param("retention") retention: Long,
        @Param("labels") labels: Map<String, String>,
        @Param("value") value: Double
    )

}

data class EventDTO(
    val id: String,
    val category: String,
    val type: String,
    val emitterId: String,
    val labels: Map<String, String>,
    val retention: Long = 0L,
    val value: Double = 1.0
)
