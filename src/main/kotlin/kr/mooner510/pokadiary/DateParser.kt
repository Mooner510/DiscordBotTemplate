package kr.mooner510.pokadiary

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


object DateParser {
    private fun parseDateInline(s: String): LocalDate {
        val split = if (s.contains("-")) s.split("-") else s.split(".")
        return if (split.size == 2) {
            LocalDate.of(LocalDate.now().year, split[0].toInt(), split[1].toInt())
        } else {
            LocalDate.of(split[0].toInt(), split[1].toInt(), split[2].toInt())
        }
    }

    private fun parseTimeInline(s: String): LocalTime {
        val split = s.split(":")
        return if (split.size == 2) {
            LocalTime.of(split[0].toInt(), split[1].toInt())
        } else {
            LocalTime.of(split[0].toInt(), split[1].toInt(), split[2].toInt())
        }
    }

    private fun parseDateTimeInline(s: String): LocalDateTime {
        val (dateString, timeString) = s.split("T")
        return LocalDateTime.of(parseDateInline(dateString), parseTimeInline(timeString))
    }

    fun String.parseDate(): LocalDate {
        return parseDateInline(this)
    }

    fun String.parseTime(): LocalTime {
        return parseTimeInline(this)
    }

    fun String.parseDateTime(): LocalDateTime {
        return parseDateTimeInline(this)
    }
}