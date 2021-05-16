package com.xmppjingle.bjomeliga.service

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

@Service
class CallMeService {

    var logger: Logger = LoggerFactory.getLogger(CallMeService::class.java)
    var client = OkHttpClient()

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    private final val scheduler = Timer("chauffeur", false)

    @PostConstruct
    fun init() {

    }

    fun callMe(cita: Cita) =
            scheduler.schedule(PostCallbackUrl(cita, client), Date.from(cita.time))

    fun callMeRedis(cita: Cita): Boolean =
            redisTemplate.opsForHash<String, String>().let { map ->
                map.putAll(cita.id, hashMapOf(
                        "time" to cita.time.toString(),
                        "url" to cita.url,
                        "payload" to cita.payload,
                        "contentType" to cita.contentType
                ))
                scheduler.schedule(PostCallbackUrl(cita, client), Date.from(cita.time))
                true
            }

}

class PostCallbackUrl(val cita: Cita, val client: OkHttpClient) : TimerTask() {
    override fun run() {
        try {
            val body: RequestBody = RequestBody.create(
                    MediaType.parse(cita.contentType), cita.payload)

            val request: Request = Request.Builder()
                    .url(cita.url)
                    .addHeader("id", cita.id)
                    .post(body)
                    .build()

            client.newCall(request).execute()

        } catch (e: Exception) {

        }
    }
}

data class Cita(
        val id: String,
        val time: Instant,
        val url: String,
        val payload: String,
        val contentType: String = "application/json"
)