package kr.mooner510.issuebot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import java.time.Duration

object IssueBot {
    @JvmStatic
    fun main(args: Array<String>) {
        val jda = JDABuilder.createDefault(args[0], GatewayIntent.entries.toMutableList())
            .addEventListeners(EventListener)
            .build()

        Runtime.getRuntime().addShutdownHook(Thread {
            println("closing...")
            jda.cancelRequests()
            jda.awaitShutdown(Duration.ofSeconds(10))
        })
    }
}