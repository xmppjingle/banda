package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.SummariesService
import com.xmppjingle.bjomeliga.service.SummaryDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
class SummariesController {

    var logger: Logger = LoggerFactory.getLogger(SummariesController::class.java)

    @Autowired
    lateinit var summariesService: SummariesService

    @PostMapping(value = ["/summary"])
    fun processSummary(@RequestBody summaryDTO: SummaryDTO): ResponseEntity<String> {
        summariesService.addSummary(summaryDTO)
        return ResponseEntity.ok("OK")
    }

    @GetMapping(value = ["/summary/{id}"])
    fun getSummary(@PathVariable(value = "id") id: String): SummaryDTO =
            summariesService.getSummary(id)

}