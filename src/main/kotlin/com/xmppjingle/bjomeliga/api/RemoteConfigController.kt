package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.ConfigDTO
import com.xmppjingle.bjomeliga.service.RemoteConfigService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
class RemoteConfigController {

    var logger: Logger = LoggerFactory.getLogger(RemoteConfigController::class.java)

    @Autowired
    lateinit var remoteConfigService: RemoteConfigService

    @PostMapping(value = ["/config"])
    fun processConfig(@RequestBody configDTO: ConfigDTO): ResponseEntity<String> {
        remoteConfigService.setParams(configDTO)
        return ResponseEntity.ok("OK")
    }

    @GetMapping(value = ["/config/{userId}"])
    fun getConfig(@PathVariable(value = "userId") userId: String): ConfigDTO =
        remoteConfigService.fetchConfig(userId)

}