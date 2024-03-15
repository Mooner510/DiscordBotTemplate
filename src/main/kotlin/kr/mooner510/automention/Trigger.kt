package kr.mooner510.automention

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object Trigger {
    private val scheduler = Executors.newScheduledThreadPool(4)
    private val map = mutableMapOf<String, ScheduledFuture<*>>()

    private fun schedule(key: String, task: Runnable) {
        println("scheduled: $key")
        map.remove(key)?.let {
            println("re-scheduled: $key")
            it.cancel(true)
        }
        map[key] = scheduler.schedule({
            println("scheduled complete: $key")
            task.run()
            schedule(key, task)
        }, 10, TimeUnit.SECONDS)
    }

    fun message(key: String, jda: JDA) {
        schedule(key) {
            jda.getChannelById(TextChannel::class.java, AutoMention.TARGET_CHANNEL)?.sendMessage("<@${AutoMention.TARGET_USER}> 채팅 안함?")?.queue()
        }
    }
}