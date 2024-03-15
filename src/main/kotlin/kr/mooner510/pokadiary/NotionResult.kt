package kr.mooner510.pokadiary

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.json.JSONObject
import java.awt.Color
import java.time.LocalDate
import java.time.LocalDateTime

object NotionResult {
    fun packaging(obj: JSONObject, statusColor: Boolean = false): MessageEmbed? {
        val properties = obj.getJSONObject("properties")
        val status = properties.getJSONObject("Status").getJSONObject("status").getString("name")
        if (status == "Not Started") return null

        val titleArray = properties.getJSONObject("Name").getJSONArray("title")
        if (titleArray.isEmpty) return null
        val today = LocalDate.parse(properties.getJSONObject("Date").getJSONObject("date").getString("start"))

        val title = (titleArray[0] as JSONObject).getString("plain_text")

        val isNew = LocalDateTime.parse(obj.getString("created_time"), PokaDiary.formatter) > PokaDiary.lastFetchTime

        return PokaDiary.client.newCall(NotionRequest.requestBlocks(obj.getString("id"))).execute().use { response2 ->
            val blocks = JSONObject(response2.body?.string())

            var description = blocks.getJSONArray("results").mapNotNull {
                it as JSONObject
                val type = it.getString("type")
                val prefix = when (type) {
                    "heading_1" -> "# "
                    "heading_2" -> "## "
                    "heading_3" -> "### "
                    "quote" -> "> "
                    "bulleted_list_item" -> "- "
                    "paragraph" -> ""
                    else -> null
                }
                if (prefix == null) return@mapNotNull null

                prefix + it.getJSONObject(type).getJSONArray("rich_text").joinToString("") { texts ->
                    texts as JSONObject
                    val annotations = texts.getJSONObject("annotations")
                    val builder = StringBuilder()
                    if (annotations.getBoolean("code")) builder.append("`")
                    if (annotations.getBoolean("bold")) builder.append("**")
                    if (annotations.getBoolean("italic")) builder.append("*")
                    if (annotations.getBoolean("strikethrough")) builder.append("~~")
                    if (annotations.getBoolean("underline")) builder.append("__")
                    val str = builder.toString()
                    str + texts.getString("plain_text") + str.reversed()
                }
            }.joinToString("\n").replace(". ", ".\n")

            if (description.length > 1500) {
                description = description.substring(0, 1500) + "\n..."
            }

            EmbedBuilder()
                .setColor(
                    if (statusColor) {
                        when (status) {
                            "Done" -> Color(64, 222, 15)
                            "In progress" -> Color(15, 129, 222)
                            else -> Color(128, 128, 128)
                        }
                    } else if (isNew) Color(27, 246, 42)
                    else Color(3, 248, 216)
                )
                .setTitle(
                    if (statusColor) "$today: $title"
                    else if (isNew) "새 일기! $today: $title"
                    else "일기가 수정됨: $today: $title",
                    obj.getString("public_url")
                )
                .setAuthor(
                    "pokabook",
                    null,
                    "https://cdn.discordapp.com/avatars/463594297187500032/451e0ad4a2db8029b6cdef649328897c?size=1024"
                )
                .setDescription(description)
                .setFooter(
                    when (status) {
                        "Done" -> "작성 완료"
                        "In progress" -> "작성 중"
                        else -> "알 수 없는 상태"
                    } + "($status) • " + today.toString()
                )
                .build()
        }
    }
}