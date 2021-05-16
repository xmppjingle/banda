package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.EventDTO
import com.xmppjingle.bjomeliga.service.EventsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
class EventsController {

    var logger: Logger = LoggerFactory.getLogger(EventsController::class.java)

    @Autowired
    lateinit var eventsService: EventsService

    @PostMapping(value = ["/events"])
    fun processEvent(@RequestBody eventDTO: EventDTO): ResponseEntity<String> {
        eventsService.emitEvent(eventDTO)
        return ResponseEntity.ok("OK")
    }

}