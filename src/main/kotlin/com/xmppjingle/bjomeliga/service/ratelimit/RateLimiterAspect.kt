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

    @Around("@annotation(com.xmppjingle.bjomeliga.service.ratelimit.RateLimit)")
    fun rateLimit(joinPoint: ProceedingJoinPoint): Any {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        //val rateLimitAnnotation = method.getAnnotation(RateLimit::class.java)
        val customerId = joinPoint.args[0] as String
        val endpoint = method.name

        if (rateLimitService.incrementAndCheckRateLimit(customerId, endpoint)) {
            throw RateLimitExceededException("Rate limit requests per hour exceeded for $customerId endpoint: $endpoint")
        }
        return joinPoint.proceed()
    }

}

class RateLimitExceededException(message: String) : Exception(message)
