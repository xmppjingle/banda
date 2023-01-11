package com.xmppjingle.bjomeliga.service.ratelimit

import com.xmppjingle.bjomeliga.service.RateLimitService
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.dynamic.RedisCommandFactory
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Value
import javax.annotation.PostConstruct

@Aspect
class RateLimiterAspect(private val rateLimitService: RateLimitService) {

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

    @Around("@annotation(com.example.RateLimit)")
    fun rateLimit(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val rateLimit = method.getAnnotation(RateLimit::class.java)

        val customerId = joinPoint.args[0] as String // assuming that customerId is the first argument
        val endpoint = rateLimit.value
        val limit = rateLimitService.getRateLimit(customerId, endpoint)

        if (limit == null) {
            return joinPoint.proceed()
        }
        val key = "$customerId:$endpoint"
        val value = commands.decr(key)
        if (value < 0) {
            // rate limit exceeded, throw exception or return error response
            throw RateLimitExceededException("Rate limit exceeded for $key")
        }
        // rate limit is not exceeded
        return joinPoint.proceed()
    }
}

class RateLimitExceededException(message: String) : Exception(message)
