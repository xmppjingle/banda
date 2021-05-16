package com.xmppjingle.bjomeliga.service

import com.fasterxml.jackson.annotation.JsonInclude
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Service

@Service
class SummariesService {

    var logger: Logger = LoggerFactory.getLogger(CallMeService::class.java)

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    fun addSummary(summaryDTO: SummaryDTO) {

        summaryDTO.metrics.forEach { metric ->
            redisTemplate.opsForHash<String, Long>().increment("TOTAL_VALUES_${summaryDTO.id}", metric.id, metric.value)
            redisTemplate.opsForSet().add("TRANSACTIONS_${summaryDTO.id}_${metric.id}", summaryDTO.transactionId)
        }

    }

    fun getSummary(id: String): SummaryDTO {

        val metrics = ArrayList<MetricDTO>()

        redisTemplate.opsForHash<String, String>().scan("TOTAL_VALUES_${id}", ScanOptions.NONE).forEach { entry ->
            val transactions = redisTemplate.opsForSet().members("TRANSACTIONS_${id}_${entry.key}")
            metrics.add(
                    MetricDTO(entry.key, entry.value.toLong(), transactions?.size?.toLong() ?: 0, transactions?.toList()
                            ?: emptyList())
            )
        }

        return SummaryDTO(id, metrics = metrics)
    }

}

data class SummaryDTO(
        val id: String,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val transactionId: String? = null,
        val metrics: List<MetricDTO>
)

data class MetricDTO(
        val id: String,
        val value: Long,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val count: Long? = null,
        val transactionIds: List<String>? = null
)
