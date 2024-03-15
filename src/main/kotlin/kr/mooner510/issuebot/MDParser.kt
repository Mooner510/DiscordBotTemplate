package kr.mooner510.issuebot

import java.util.Scanner

object MDParser {
    private val titleRegex = Regex("### (.*)")

    fun parse(title: String, raw: String): Issue {
        var content: String? = null
        var label: List<String>? = null
        var priority: Priority? = null

        var section: Section? = null
        var list = mutableListOf<String>()
        println("============ PARSER ============")
        raw.split("\n").forEach { str ->
            println(str)
            val result = titleRegex.find(str)
            if (result != null) {
                val (group) = result.destructured
                when (section) {
                    Section.Content ->
                        content = if (list.last().isBlank()) {
                            list.subList(0, list.size - 1).joinToString("\n")
                        } else {
                            list.joinToString("\n")
                        }

                    Section.Label -> label = list

                    Section.Priority -> priority = Priority.parse(list[0])

                    else -> {}
                }
                list = mutableListOf()
                section = when (group.trim()) {
                    "이슈 상세 내용" -> Section.Content
                    "라벨" -> Section.Label
                    "우선순위" -> Section.Priority
                    else -> null
                }
                return@forEach
            }
            if (section == null) return@forEach
            list.add(str)
        }
        println("============ PARSER ============")

        return Issue(
            title,
            content ?: "",
            label ?: listOf(),
            priority
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val scanner = Scanner(System.`in`)
        while (true) {
            println(parse("제목", scanner.nextLine()))
        }
    }

    enum class Section {
        Content, Label, Priority
    }
}