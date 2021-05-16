package com.xmppjingle.bjomeliga.service

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.dynamic.RedisCommandFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class RemoteConfigService {

    var logger: Logger = LoggerFactory.getLogger(RemoteConfigService::class.java)

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

    fun fetchConfig(userId: String): ConfigDTO =
        ConfigDTO(userId, commands.hgetall("config:$userId"))

    fun setParams(configDTO: ConfigDTO) {
        with(configDTO) {
            commands.hset("config:$userId", params)
        }
    }

}

data class ConfigDTO(
    val userId: String,
    val params: Map<String, String>
)
