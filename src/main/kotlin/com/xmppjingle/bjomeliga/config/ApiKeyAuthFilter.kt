package com.xmppjingle.bjomeliga.config

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.dynamic.RedisCommandFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ApiKeyAuthFilter(authenticationManager: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {

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

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        // Extract the customer ID and API key from the request
        val customerId = request.getParameter("customerId")
        val apiKey = request.getHeader("x-api-key")

        if (apiKey == null) {
            throw BadCredentialsException("Missing API key in request headers")
        }

        // Look up the API key in Redis
        val storedApiKey = commands.get("customer:$customerId:api-key")

        // Compare the API key in the request headers with the one stored in Redis
        if (apiKey != storedApiKey) {
            throw BadCredentialsException("Invalid API key")
        }
        // Create an authentication token with the customer ID as the principal
        val authToken = UsernamePasswordAuthenticationToken(customerId, apiKey)
        setDetails(request, authToken)
        return authenticationManager.authenticate(authToken)
    }
}
