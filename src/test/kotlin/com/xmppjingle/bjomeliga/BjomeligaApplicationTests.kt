package com.xmppjingle.bjomeliga

import com.xmppjingle.bjomeliga.service.*
import org.apache.commons.math3.distribution.EnumeratedDistribution
import org.apache.commons.math3.util.Pair
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


@SpringBootTest
class BjomeligaApplicationTests {

//    @Test
    fun test1() {

        val experimentService = ExperimentService()
        experimentService.init()

        val eventsService = EventsService()
        eventsService.experimentService = experimentService
        eventsService.init()

        val remoteConfigService = RemoteConfigService()
        remoteConfigService.init()
        experimentService.remoteConfigService = remoteConfigService

        val variants = VariantsDTO(
            listOf(
                VariantDTO("a", 90, hashMapOf("x" to "a")),
                VariantDTO("b", 10, hashMapOf("x" to "b"))
            )
        )

        val e = ExperimentDTO(
            "exp1",
            variants,
            "trig1",
            listOf("1", "2")
        )

        experimentService.addExperiment(e)
        val v1 = experimentService.fetchVariants(e.id)

        assert(v1 == variants)
        assert(e == experimentService.getExperiment(e.id))
        assert(experimentService.getExperimentsForTriggerId(e.triggerEventId).isNotEmpty())

        val sessionIds = ArrayList<String>()

        val participants = 100
        for (i in 1..participants) {
            val uuid = UUID.randomUUID().toString()
            eventsService.emitEvent(EventDTO("trig1", "cat2", "type1", uuid, emptyMap(), 60000, 1.0))
            eventsService.emitEvent(EventDTO("trig2", "cat3", "type1", uuid, emptyMap(), 60000, 1.0))
            sessionIds.add(uuid)
        }

        val m = hashMapOf<String, AtomicInteger>()
        m["a"] = AtomicInteger()
        m["b"] = AtomicInteger()

//        sessionIds.forEach {
//            m[remoteConfigService.fetchConfig(it).params["x"]]?.incrementAndGet()
//        }

        println(" a: ${m["a"]} / b: ${m["b"]}  ")

//        println(experimentService.fetchParticipantsOnExperiment(e.id))

//        assert(participants.toLong() == experimentService.fetchParticipantsOnExperiment(e.id).toLong())
    }

//    @Test
    fun entropyTests() {

        val variants = VariantsDTO(
            listOf(
                VariantDTO("a", 90, hashMapOf("a" to "b")),
                VariantDTO("b", 10, hashMapOf("a" to "b"))
            )
        )

        val items = variants.variants.map { Pair(it, it.weight.toDouble()) }.toList()
        val x = EnumeratedDistribution(items)

        val m = hashMapOf<String, AtomicInteger>()
        m["a"] = AtomicInteger()
        m["b"] = AtomicInteger()

        for (i in 1..10000) {
            val w = x.sample()
            m[w.id]?.incrementAndGet()
        }

        print(" a: ${m["a"]} / b: ${m["b"]}  ")

    }

}
