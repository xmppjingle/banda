package com.xmppjingle.bjomeliga

import com.xmppjingle.bjomeliga.service.RateLimitService
import com.xmppjingle.bjomeliga.service.ratelimit.RateLimitExceededException
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import net.ishiis.redis.unit.RedisServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RateLimitServiceTest {

    private lateinit var redisServer: RedisServer
    private lateinit var redisClient: RedisClient
    private lateinit var connection: StatefulRedisConnection<String, String>
    private lateinit var redisCommands: RedisCommands<String, String>

    @BeforeEach
    fun setUp() {
        redisClient = RedisClient.create("redis://127.0.0.1:6333")
        connection = redisClient.connect()
        redisCommands = connection.sync()
    }

    @AfterEach
    fun tearDown() {
        connection.close()
        redisClient.shutdown()
    }

    @Test
    fun testRateLimitService() {
        val rateLimitService = RateLimitService()
        rateLimitService.init()
        rateLimitService.resetRate("customer1", "endpoint1")
        rateLimitService.resetRate("customer1", "endpoint2")

        // Configure a rate limit for endpoint1 for customer1
        rateLimitService.configureRateLimit("customer1", "endpoint1", 5)

        // Verify the rate limit was set correctly
        assertEquals(5, rateLimitService.getRateLimit("customer1", "endpoint1")?.limit)

        // Try to increase the rate limit over the limit and check exception
        try {
            for(i in 1..6) {
                assert( rateLimitService.incrementAndCheckRateLimit("customer1", "endpoint1", 6))
            }
            assertEquals(false, rateLimitService.incrementAndCheckRateLimit("customer1", "endpoint1", 6))
        } catch (e: RateLimitExceededException) {
            assertEquals("Rate limit exceeded for customer1:endpoint1", e.message)
        }

        // Check incrementAndCheckRateLimit is allowed
        for (i in 1..4) {
            assert( rateLimitService.incrementAndCheckRateLimit("customer1", "endpoint2", 4))
        }

        assertEquals(false, rateLimitService.incrementAndCheckRateLimit("customer1", "endpoint", 0))
    }

//    @Test
//    fun testApplyConfigurationsToCustomer() {
//        rateLimitService.applyConfigurationsToCustomer("customer1", "plan1")
//
//        assertEquals("10", redisCommands.get("customer1:endpoint1"))
//        assertEquals("20", redisCommands.get("customer1:endpoint2"))
//    }

//    @Test
//    fun testRespectLimits() {
//        rateLimitService.configureRateLimit("customer1", "endpoint1", 5)
//        rateLimitService.configureRateLimit("customer1", "endpoint2", 10)
//        val controller = RemoteConfigController()
//        val headers = HttpHeaders()
//        headers["customerId"] = "customer1"
//        for (i in 0..4) {
//            // first 5 requests should return 200 OK
//            val entity = HttpEntity(headers)
//            assertEquals(HttpStatus.OK, controller.getConfig("endpoint1", entity).statusCode)
//        }
//        // 6th request should return 429 TOO MANY REQUESTS
//        val entity = HttpEntity(headers)
//        assertEquals(HttpStatus.TOO_MANY_REQUESTS, controller.handleRequest("endpoint1", entity).statusCode)
//        for (i in 0..9) {
//            // first 10 requests should return 200 OK
//            val entity = HttpEntity(headers)
//            assertEquals(HttpStatus.OK, controller.handleRequest("endpoint2", entity).statusCode)
//        }
//        // 11th request should return 429 TOO MANY REQUESTS
//        val entity = HttpEntity(headers)
//        assertEquals(HttpStatus.TOO_MANY_REQUESTS, controller.handleRequest("endpoint2", entity).statusCode)
//    }
}
