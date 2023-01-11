package com.xmppjingle.bjomeliga.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.dynamic.RedisCommandFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*
import javax.annotation.PostConstruct


@Service
class RateLimitService() {

    var logger: Logger = LoggerFactory.getLogger(RemoteConfigService::class.java)

    @Autowired
    private val resourceLoader: ResourceLoader? = null

    lateinit var redisClient: RedisClient
    lateinit var connection: StatefulRedisConnection<String, String>
    lateinit var factory: RedisCommandFactory
    lateinit var commands: RedisCommands<String, String>

    @Value("\${redis.url:redis://localhost:6379}")
    var redisURI: String = "redis://localhost:6379"

    @PostConstruct
    fun init() {
        redisClient = RedisClient.create(redisURI)
        connection = redisClient.connect()
        factory = RedisCommandFactory(connection)
        commands = connection.sync()
    }

    fun configureRateLimit(customerId: String, endpoint: String, limit: Int) {
        val key = "$customerId:$endpoint"
        commands.set(key, limit.toString())
    }

    fun removeRateLimit(customerId: String, endpoint: String) {
        val key = "$customerId:$endpoint"
        commands.del(key)
    }

    fun getRateLimit(customerId: String, endpoint: String): RateLimit? {
        val key = "$customerId:$endpoint"
        val limit = commands.get(key)
        return if(limit != null) {
            RateLimit(endpoint, limit.toInt())
        } else {
            null
        }
    }

    fun getAllLimits(customerId: String): List<RateLimit> {
        val key = "customer:$customerId:limits"
        val limitEntries = commands.hgetall(key)
        val rateLimits = mutableListOf<RateLimit>()

        limitEntries.forEach { (endpoint, limit) ->
            val rateLimit = RateLimit(endpoint, limit.toInt())
            rateLimits.add(rateLimit)
        }
        return rateLimits
    }

    fun incrementAndCheckRateLimit(customerId: String, endpoint: String, requestsPerHour: Int): Boolean {
        val key = "rate:$customerId:$endpoint"
        val current = commands.incr(key)
        commands.expire(key, 3600)
        return current <= requestsPerHour
    }

    fun resetRate(customerId: String, endpoint: String) {
        val key = "rate:$customerId:$endpoint"
        commands.del(key)
    }

    fun incrementAndCheckRateLimit(customerId: String, endpoint: String): Boolean {
        return incrementAndCheckRateLimit(customerId, endpoint, getRateLimit(customerId, endpoint)?.limit ?: 0)
    }

    fun applyConfigurationsToCustomer(customerId: String, plan: String) {
        val resource: Resource = resourceLoader?.getResource("classpath:rateLimits/$plan.properties") ?: throw IOException("Resource not found")
        val properties = Properties()
        properties.load(resource.getInputStream())
        for (key in properties.stringPropertyNames()) {
            val keyCust = "$customerId:$key"
            commands.set(keyCust, properties.getProperty(key))
        }
    }

}

data class RateLimit(val endpoint: String, val limit: Int)
