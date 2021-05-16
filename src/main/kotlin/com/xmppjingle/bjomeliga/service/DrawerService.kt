package com.xmppjingle.bjomeliga.service

import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class DrawerService {

    var logger: Logger = LoggerFactory.getLogger(CallMeService::class.java)

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    fun updateDrawer(drawerDTO: DrawerDTO) {
        redisTemplate.opsForHash<String, String>().putAll(getDrawerKey(drawerDTO.id), drawerDTO.values)
    }

    fun getDrawer(id: String): DrawerDTO {
        return DrawerDTO(id, redisTemplate.opsForHash<String, String>().entries(getDrawerKey(id)))
    }

    fun getDrawerKey(id: String): String = "DRAWER_$id"

}

data class DrawerDTO(
        val id: String,
        val values: Map<String, String>
)
