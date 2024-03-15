package kr.mooner510.automention

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object EventListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.id != AutoMention.TARGET_USER) return
//        event.message.addReaction(Emoji.fromUnicode("U+1F44D")).queue()
        Trigger.message(AutoMention.TARGET_USER, event.jda)
    }
}