package com.xmppjingle.bjomeliga.api

import com.xmppjingle.bjomeliga.service.RecommendationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/graphs/{graphId}/choices")
class RecommendationController(private val recommendationService: RecommendationService) {

    @PostMapping("/{choiceId}/users/{userId}/options/{selectedOption}")
    fun addChoiceEvent(
        @PathVariable graphId: String, @PathVariable choiceId: String, @PathVariable userId: String,
        @PathVariable selectedOption: String, @RequestBody options: Set<String>
    ) {
        recommendationService.addChoiceEvent(graphId, choiceId, userId, selectedOption, options)
    }

    @GetMapping("/{choiceId}/options")
    fun getOptions(@PathVariable graphId: String, @PathVariable choiceId: String): Set<String> {
        return recommendationService.getOptions(graphId, choiceId)
    }

    @GetMapping("/{choiceId}/likelihood")
    fun getLikelyChoice(
        @PathVariable graphId: String,
        @PathVariable choiceId: String,
        @RequestParam tags: Set<String>
    ): Map<String, Long> {
        return recommendationService.getLikelyChoice(graphId, choiceId, tags)
    }

    @PostMapping("/{userId}/tags")
    fun addUserTags(@PathVariable graphId: String, @PathVariable userId: String, @RequestBody tags: Set<String>) {
        recommendationService.addUserTags(graphId, userId, tags)
    }

    @GetMapping
    fun getUsers(@PathVariable graphId: String): Set<String> {
        return recommendationService.getUsers(graphId)
    }
}

