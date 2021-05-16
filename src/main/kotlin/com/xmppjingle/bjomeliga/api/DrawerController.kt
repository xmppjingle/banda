package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.DrawerDTO
import com.xmppjingle.bjomeliga.service.DrawerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
class DrawerController {

    var logger: Logger = LoggerFactory.getLogger(DrawerController::class.java)

    @Autowired
    lateinit var drawerService: DrawerService

    @PostMapping(value = ["/drawer"])
    fun processDrawer(@RequestBody drawerDTO: DrawerDTO): ResponseEntity<String> {
        drawerService.updateDrawer(drawerDTO)
        return ResponseEntity.ok("OK")
    }

    @GetMapping(value = ["/drawer/{id}"])
    fun getDrawer(@PathVariable(value = "id") id: String): DrawerDTO =
            drawerService.getDrawer(id)

}