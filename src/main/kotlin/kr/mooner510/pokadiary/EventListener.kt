package kr.mooner510.pokadiary

import kr.mooner510.pokadiary.DateParser.parseDate
import kr.mooner510.pokadiary.DateParser.parseDateTime
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object EventListener : ListenerAdapter() {
    fun LocalDateTime.toRelative(): String {
        return "<t:${this.toEpochSecond(ZoneOffset.ofHours(9))}:R>"
    }

    fun LocalDateTime.toDiscordDateTime(): String {
        return "<t:${this.toEpochSecond(ZoneOffset.ofHours(9))}:F>"
    }

    fun LocalDateTime.toDateTime(): String {
        return this.format(DateTimeFormatter.ISO_DATE_TIME)
    }

    val userMap = hashMapOf<String, HashSet<LocalDateTime>>()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val userId = event.author.id
        if (event.channel.id != Env.COMMAND_CHANNEL_ID) return
        if (!event.message.contentRaw.startsWith(".")) return
        val raw = event.message.contentRaw
        val args = raw.split(" ")

        if (args[0].replace(".", "").isBlank()) {
            if (!userMap.containsKey(userId)) {
                val set = hashSetOf<LocalDateTime>()
                set.add(LocalDateTime.now())
                userMap[userId] = set
            } else {
                userMap[userId]?.let { set ->
                    set.add(LocalDateTime.now())
                    val hashSet = set.filter { it > LocalDateTime.now().minusMinutes(10) }.toHashSet()
                    userMap[userId] = hashSet
                    if (hashSet.size >= 5) {
                        if (hashSet.size < 10) {
                            event.message.addReaction(Emoji.fromUnicode("U+1F620")).queue() // 😠
                        } else if (hashSet.size < 15) {
                            event.message.addReaction(Emoji.fromUnicode("U+1F47F")).queue() // 👿
                        } else if (hashSet.size < 22) {
                            event.message.addReaction(Emoji.fromUnicode("U+1F92C")).queue() // 🤬
                        } else if (hashSet.size < 30) {
                            event.message.addReaction(Emoji.fromUnicode("U+1F595")).queue() // 🖕
                        } else if (hashSet.size == 30) {
                            event.channel.sendMessage("그만 해라 난 모르겠다")
                                .setMessageReference(event.message)
                                .queue()
                        }
                    }
                }
            }
            return
        }

        try {
            if (args[0] == ".fetch") {
                event.channel.sendMessage("**일기 새로 고침**: ${PokaDiary.lastFetchTime.toRelative()} 이후 일기 조회")
                    .setMessageReference(event.message)
                    .queue()
                PokaDiary.fetch()
            } else if (raw.startsWith(".diary")) {
                val date = args[1].parseDate()
                PokaDiary.client.newCall(NotionRequest.requestEquals(date)).execute().use { response ->
                    val json = JSONObject(response.body?.string())
                    val results = json.getJSONArray("results")
                    if (results.isEmpty) {
                        event.channel.sendMessage("${date}에 작성된 일기가 없습니다.").queue()
                        return
                    }
                    NotionResult.packaging(results[0] as JSONObject, true)?.let {
                        event.channel.sendMessageEmbeds(it)
                            .setMessageReference(event.message)
                            .queue()
                        return@use
                    }
                    event.channel.sendMessage("${date}에 작성된 일기는 준비중입니다.").queue()
                }
                PokaDiary.fetch()
            } else if (args[0] == ".next") {
                event.channel.sendMessage("**다음 Fetch**: ${PokaDiary.lastFetchTime.toRelative()}")
                    .setMessageReference(event.message)
                    .queue()
            }

            if (args[0] == ".reset") {
                if (userId != "524868734054039554" && userId != "463594297187500032") {
                    event.message.addReaction(Emoji.fromUnicode("U+2753")).queue()
                    return
                }
                event.channel.sendMessage("**타이머 초기화**\n다음 Fetch: ${PokaDiary.lastFetchTime.plusMinutes(10).toDiscordDateTime()}")
                    .setMessageReference(event.message)
                    .queue()
                PokaDiary.reset()
            } else if (args[0] == ".dateTime") {
                if (userId != "524868734054039554" && userId != "463594297187500032") {
                    event.message.addReaction(Emoji.fromUnicode("U+2753")).queue()
                    return
                }
                try {
                    val date = args[1].parseDateTime()
                    PokaDiary.lastFetchTime = date
                    event.channel.sendMessage("**날짜 재설정**: ${PokaDiary.lastFetchTime.toDiscordDateTime()}에 마지막으로 Fetch한 것으로 변경됨")
                        .setMessageReference(event.message)
                        .queue()
                } catch (_: Exception) {
                    try {
                        val date = args[1].parseDate()
                        PokaDiary.lastFetchTime = date.atStartOfDay()
                        event.channel.sendMessage("**날짜 재설정**: ${PokaDiary.lastFetchTime.toDiscordDateTime()}에 마지막으로 Fetch한 것으로 변경됨")
                            .setMessageReference(event.message)
                            .queue()
                    } catch (_: Exception) {
                        event.channel.sendMessage("날짜 포멧팅이 잘못됨! `yyyy-MM-dd`**T**`HH:mm:ss` 또는 `yyyy-MM-dd`")
                            .setMessageReference(event.message)
                            .queue()
                    }
                }
            }
        } catch (e: Exception) {
            println("==================== ERROR LOG ====================")
            e.printStackTrace()
            println("==================== ERROR LOG ====================")
            event.message.addReaction(Emoji.fromUnicode("U+2753")).queue()
            println("==================== ERROR INFO ====================")
            println(
                """user: ${event.author.name} / ${event.author.effectiveName} (${event.author.id})
text: ${event.message.contentRaw}
channel: ${event.channel.name} (${event.channel.id}, ${event.channelType})
guild: ${event.guild.name} (${event.guild.id})""".trimIndent()
            )
            println("==================== ERROR INFO ====================")
        }
    }
}