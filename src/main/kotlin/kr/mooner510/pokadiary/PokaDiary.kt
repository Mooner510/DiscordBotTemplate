package kr.mooner510.pokadiary

import kr.mooner510.pokadiary.Env.DELAY
import kr.mooner510.pokadiary.EventListener.toDateTime
import kr.mooner510.pokadiary.EventListener.toRelative
import kr.mooner510.pokadiary.NotionRequest.requestAfter
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Consumer
import kotlin.concurrent.timerTask


object PokaDiary {
    private val timer = Timer()

    var lastFetchTime: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).withNano(0)
    private val lastFetchFile = File("./src/main/resources/pokadiary/lastFetch")
    private val lastMessageFile = File("./src/main/resources/pokadiary/lastMessage")

    val client = OkHttpClient()
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private var lastMessageId: String? = null

    private lateinit var jda: JDA

    @JvmStatic
    fun main(args: Array<String>) {
        if (System.getenv("READY") != null) {
            Env.init()
        }

        if (lastFetchFile.exists()) {
            FileReader(lastFetchFile).use {
                lastFetchTime = LocalDateTime.parse(it.readText())
                println("FetchTime Loaded: ${lastFetchTime.toDateTime()}")
            }
        } else {
            FileWriter(lastFetchFile).use {
                it.write(lastFetchTime.toString())
                it.flush()
            }
        }
        if (lastMessageFile.exists()) {
            FileReader(lastMessageFile).use {
                lastMessageId = it.readText()
                println("MessageId Loaded: $lastMessageId")
            }
        }

        jda = JDABuilder.createDefault(args[0], GatewayIntent.entries.toMutableList())
            .addEventListeners(EventListener)
            .build()

        jda.awaitReady()

        timer.schedule(timerTask {
            send()
        }, Env.DELAY, Env.DELAY)
        send()

        Runtime.getRuntime().addShutdownHook(Thread {
            println("closing...")
            jda.cancelRequests()
            jda.awaitShutdown(Duration.ofSeconds(10))
        })
    }

    fun fetch() {
        send(false)
    }

    fun reset() {
        timer.purge()
        timer.schedule(timerTask {
            send()
        }, Env.DELAY, Env.DELAY)
    }

    fun channel(run: Consumer<TextChannel>) {
        jda.getChannelById(TextChannel::class.java, Env.NOTIFY_CHANNEL_ID)?.let {
            run.accept(it)
        }
    }

    private fun send(fetch: Boolean = false) {
        jda.presence.activity = Activity.customStatus("Next Fetch: ${LocalDateTime.now().plusSeconds(DELAY / 1000).withNano(0).toLocalTime()}")
        client.newCall(requestAfter(lastFetchTime)).execute().use { response ->
            val json = JSONObject(response.body?.string())

            val results = json.getJSONArray("results")
            if (results.isEmpty) {
                println("\n${LocalDateTime.now().withNano(0)}: No more diary founds after $lastFetchTime\n")
                channel { channel ->
                    lastFetchTime = LocalDateTime.now(ZoneOffset.UTC).withNano(0)
                    FileWriter(lastFetchFile).use {
                        it.write(lastFetchTime.toString())
                        it.flush()
                    }

                    lastMessageId?.let {
                        try {
                            channel.editMessageById(
                                it,
                                "새 일기 혹은 변경된 일기가 없습니다. (Updated: ${LocalDateTime.now().toRelative()}, Fetch After: ${lastFetchTime.toRelative()})"
                            ).complete()
                            return@channel
                        } catch (_: Throwable) {
                        }
                    }
                    channel.sendMessage(
                        "새 일기 혹은 변경된 일기가 없습니다. (Updated: ${
                            LocalDateTime.now().toRelative()
                        }, Fetch After: ${lastFetchTime.toRelative()})"
                    ).queue {
                        lastMessageId = it.id

                        FileWriter(lastMessageFile).use { writer ->
                            writer.write(it.id)
                            writer.flush()
                        }
                    }
                }
                return@use
            }

            lastMessageId?.let {
                channel { channel ->
                    channel.deleteMessageById(it).queue()
                }
            }

            if (fetch) channel { channel ->
                channel.sendMessage("## Skipped \n" + results.mapNotNull { obj ->
                    obj as JSONObject
                    val properties = obj.getJSONObject("properties")
                    val status = properties.getJSONObject("Status").getJSONObject("status").getString("name")
                    if (status == "Done") return@mapNotNull null

                    val today = LocalDate.parse(properties.getJSONObject("Date").getJSONObject("date").getString("start"))
                    "**${today}**: $status"
                }.joinToString("\n- ", prefix = "- ")).queue()
            }

            results.map { obj ->
                NotionResult.packaging(obj as JSONObject)?.let { embed ->
                    channel {
                        it.sendMessageEmbeds(embed).queue()
                    }
                }
            }

            lastFetchTime = LocalDateTime.now(ZoneOffset.UTC).withNano(0)
            FileWriter(lastFetchFile).use {
                it.write(lastFetchTime.toString())
                it.flush()
            }
        }
    }
}