package kr.mooner510.lib.command

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import java.util.*

annotation class Command(
    val name: String,
    val type: Command.Type,
    val description: String = "",
    val nsfw: Boolean = false
)
