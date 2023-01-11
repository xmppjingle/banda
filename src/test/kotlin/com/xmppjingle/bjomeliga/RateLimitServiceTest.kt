package com.xmppjingle.bjomeliga

import com.xmppjingle.bjomeliga.api.RateLimitAdminController
import com.xmppjingle.bjomeliga.api.RemoteConfigController
import com.xmppjingle.bjomeliga.service.RateLimitService
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.dynamic.RedisCommandFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import javax.annotation.PostConstruct

@SpringBootTest
class RateLimitServiceTest {

    @Autowired
    private lateinit var rateLimitService: RateLimitService

    lateinit var redisClient: RedisClient
    lateinit var connection: StatefulRedisConnection<String, String>
    lateinit var factory: RedisCommandFactory
    lateinit var redisCommands: RedisCommands<String, String>

    @Value("\${redis.url:redis://localhost:6379}")
    var redisURI: String = "redis://localhost:6379"

    @PostConstruct
    fun init() {
        redisClient = RedisClient.create(redisURI)
        connection = redisClient.connect()
        factory = RedisCommandFactory(connection)
        redisCommands = connection.sync()
    }
    @Test
    fun testApplyConfigurationsToCustomer() {
        rateLimitService.applyConfigurationsToCustomer("customer1", "plan1")

        assertEquals("10", redisCommands.get("customer1:endpoint1"))
        assertEquals("20", redisCommands.get("customer1:endpoint2"))
    }

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
