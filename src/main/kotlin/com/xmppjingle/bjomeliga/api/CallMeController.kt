package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.CallMeService
import com.xmppjingle.bjomeliga.service.Cita
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@CrossOrigin(origins = ["*"])
class CallMeController {

    var logger: Logger = LoggerFactory.getLogger(CallMeController::class.java)

    @Autowired
    lateinit var callMeService: CallMeService

    @PostMapping(value = ["/schedule/{id}/{dateTime}"])
    fun processPhoneBook(@RequestBody payload: String, @RequestHeader("Content-Type") contentType: String,
                         @PathVariable(value = "id") id: String, @PathVariable(value = "dateTime") dateTime: String,
                         @RequestParam(value = "url", required = true) url: String): ResponseEntity<String> =

            Instant.parse(dateTime).let { instant ->
                when {
                    Instant.now().isAfter(instant) -> {
                        ResponseEntity.status(400).body("I guess you guys aren't ready for that yet.")
                    }
                    else -> {
                        callMeService.callMe(
                                Cita(
                                        id,
                                        instant,
                                        url,
                                        payload,
                                        contentType
                                ))
                        ResponseEntity.ok("OK")
                    }
                }
            }

}