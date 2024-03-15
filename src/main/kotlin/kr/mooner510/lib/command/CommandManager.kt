package kr.mooner510.lib.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command.Type
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter

object CommandManager : ListenerAdapter() {
    fun init(jda: JDA, pkg: String) {
        val scanner = ClassPathScanningCandidateComponentProvider(true)
        scanner.addIncludeFilter(AnnotationTypeFilter(Command::class.java))

        val commands = scanner.findCandidateComponents("kr.mooner510.$pkg").mapNotNull { bean ->
            val clazz = Class.forName(bean.beanClassName)
            val command = clazz.getAnnotation(Command::class.java)
            val cmd = when (command.type) {
                Type.SLASH -> Commands.slash(command.name, command.description)
                Type.USER -> Commands.user(command.name)
                Type.MESSAGE -> Commands.message(command.name)
                else -> return@mapNotNull null
            }

            clazz.getAnnotation(Localization::class.java)?.let { localization ->
                localization.locales.forEach {
                    cmd.setNameLocalization(it.locale, it.name)
                }
            }

            cmd.setNSFW(command.nsfw)
            cmd.

            cmd
        }
        jda.updateCommands().addCommands(commands)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {

    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {

    }
}