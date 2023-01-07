package com.xmppjingle.bjomeliga

import com.xmppjingle.bjomeliga.service.RecommendationService
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import net.ishiis.redis.unit.RedisServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class RecommendationServiceTest {
    private lateinit var redisServer: RedisServer
    private lateinit var redisClient: RedisClient
    private lateinit var connection: StatefulRedisConnection<String, String>
    private lateinit var redisCommands: RedisCommands<String, String>

    @BeforeEach
    fun setUp() {
//        redisServer = RedisServer(6333)
//        redisServer.start()
        redisClient = RedisClient.create("redis://127.0.0.1:6333")
        connection = redisClient.connect()
        redisCommands = connection.sync()
    }

    @AfterEach
    fun tearDown() {
        connection.close()
        redisClient.shutdown()
//        redisServer.stop()
    }

    @Test
    fun testGetLikelyChoice() {
        // Create Redis Graph database
        val graphId = "test"
        val command = "GRAPH.CREATE $graphId VERTEX Choice Option User Tag"
        redisCommands.publish("", command)


        // Create RecommendationService and add sample data
        val service = RecommendationService()
        service.init()

        service.addUserTags("user1", setOf("tag1", "tag2"))
        service.addUserTags("user2", setOf("tag1", "tag3"))
        service.addUserTags("user3", setOf("tag1", "tag4"))

        service.getUserTags("user1").forEach { println(it) }

        assertEquals(3, service.getUsers().size)

        service.addChoiceEvent("1", "user1", "1", setOf("1", "2"))
        service.addChoiceEvent("1", "user2", "1", setOf("1", "3"))
        service.addChoiceEvent("1", "user3", "3", setOf("1", "2", "3"))
        service.addChoiceEvent("1", "user1", "2", setOf("1", "2"))

        assertEquals(3, service.getOptions("1").size)

        // Test getLikelyChoice method
        val expectedResult = mapOf(
            "1" to 2L,
            "2" to 2L,
            "3" to 1L
        )
        assertEquals(expectedResult, service.getLikelyChoice("1", setOf("tag1", "tag2")))
    }

}