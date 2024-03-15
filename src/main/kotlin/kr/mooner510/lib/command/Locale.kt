package kr.mooner510.lib.command

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import java.util.*

annotation class Locale(
    val locale: DiscordLocale,
    val name: String
)
