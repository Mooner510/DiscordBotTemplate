package kr.mooner510.automention

import kr.mooner510.lib.command.Command
import kr.mooner510.lib.command.CommandManager
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import java.time.Duration

object AutoMention {
    const val TARGET_USER = "524868734054039554"
    const val TARGET_CHANNEL = "1217620324418588754"

    @JvmStatic
    fun main(args: Array<String>) {
//        println("token: ${args[0]}")
//        val jda = JDABuilder.createDefault(args[0], GatewayIntent.entries.toMutableList())
//            .addEventListeners(EventListener)
//            .build()

        val pkg = "automention"

        val scanner = ClassPathScanningCandidateComponentProvider(true)
        scanner.addIncludeFilter(AnnotationTypeFilter(Command::class.java))

        val components = scanner.findCandidateComponents("kr.mooner510.$pkg")

        for (component in components) {
            println(component.beanClassName)
            val clazz = Class.forName(component.beanClassName)

            clazz.getField("INSTANCE").get(null) as ListenerAdapter
        }

//        Trigger.message(TARGET_USER, jda)
//
//        Runtime.getRuntime().addShutdownHook(Thread {
//            println("closing...")
//            jda.cancelRequests()
//            jda.awaitShutdown(Duration.ofSeconds(10))
//        })
    }
}