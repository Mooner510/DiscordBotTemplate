package kr.mooner510.issuebot

import kr.mooner510.issuebot.data.DataSet
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object EventListener : ListenerAdapter() {
    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return

        val thread = event.channel.asThreadChannel()
        println("sending to ${thread.name} ${thread.messageCount} ${thread.totalMessageCount}")

        thread.retrieveStartMessage().queue {
            println(it.contentRaw)
        }
        val msg = thread.appliedTags.joinToString(" ") { "<@${DataSet.mentions[it.name]}>" }
        if (msg.isNotEmpty()) thread.sendMessage(msg).queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        println(event.channelType)
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return

        val thread = event.channel.asThreadChannel()

        println(event.message.contentRaw)
        println(event.message.contentDisplay)
        println(event.message.contentStripped)

        println(MDParser.parse(thread.name, event.message.contentRaw))
    }
}