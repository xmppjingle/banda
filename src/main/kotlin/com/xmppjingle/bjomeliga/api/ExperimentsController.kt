package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.ExperimentDTO
import com.xmppjingle.bjomeliga.service.ExperimentService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
class ExperimentsController {

    var logger: Logger = LoggerFactory.getLogger(ExperimentsController::class.java)

    @Autowired
    lateinit var experimentService: ExperimentService

    @PostMapping(value = ["/experiment"])
    fun addExperiment(@RequestBody experimentDTO: ExperimentDTO): ResponseEntity<String> {
        experimentService.addExperiment(experimentDTO)
        return ResponseEntity.ok("OK")
    }

    @GetMapping(value = ["/experiment/{id}"])
    fun getExperiment(@PathVariable(value = "id") id: String): ExperimentDTO? =
        experimentService.getExperiment(id)

}