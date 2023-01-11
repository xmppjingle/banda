package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.RateLimitService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/admin/{customerId}/rate-limit")
class RateLimitAdminController {
    @Autowired
    lateinit var rateLimitService: RateLimitService

    @GetMapping("/")
    fun getAllLimits(@PathVariable("customerId") customerId: String): ResponseEntity<List<Any>> {
        val rateLimits = rateLimitService.getAllLimits(customerId)
        return ResponseEntity.ok(rateLimits)
    }

    @PostMapping("/limits/{customerId}/{endpoint}/{limit}")
    fun addRateLimit(
        @PathVariable customerId: String?,
        @PathVariable endpoint: String?,
        @PathVariable limit: Int
    ): ResponseEntity<Void> {
        rateLimitService.configureRateLimit(customerId!!, endpoint!!, limit)
        return ResponseEntity<Void>(HttpStatus.CREATED)
    }

    @PutMapping("/{endpoint}/{limit}")
    fun updateRateLimit(
        @PathVariable customerId: String?,
        @PathVariable endpoint: String?,
        @PathVariable limit: Int
    ): ResponseEntity<Void> {
        rateLimitService.configureRateLimit(customerId!!, endpoint!!, limit)
        return ResponseEntity<Void>(HttpStatus.OK)
    }

    @DeleteMapping("/{endpoint}")
    fun deleteRateLimit(
        @PathVariable customerId: String?,
        @PathVariable endpoint: String?
    ): ResponseEntity<Void> {
        rateLimitService.removeRateLimit(customerId!!, endpoint!!)
        return ResponseEntity<Void>(HttpStatus.NO_CONTENT)
    }

    @PutMapping("/apply/{plan}")
    fun applyConfigurationsToCustomer(@PathVariable("customerId") customerId: String, @PathVariable("plan") plan: String): ResponseEntity<Any> {
        try {
            rateLimitService.applyConfigurationsToCustomer(customerId, plan)
            return ResponseEntity.ok().build()
        } catch (ex: Exception) {
            return ResponseEntity.badRequest().body(ex.message)
        }
    }
}
