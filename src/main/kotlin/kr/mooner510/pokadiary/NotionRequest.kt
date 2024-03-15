package kr.mooner510.pokadiary

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object NotionRequest {
    private val header = mapOf(
        Pair("Authorization", "Bearer ${Env.NOTION_KEY}"),
        Pair("Notion-Version", Env.NOTION_VERSION),
    )

    private fun Request.Builder.header(): Request.Builder {
        for (entry in header) {
            this.addHeader(entry.key, entry.value)
        }
        return this
    }

    fun requestAfter(time: LocalDateTime) = Request.Builder()
        .url("https://api.notion.com/v1/databases/5394d1d254f14b9e85a9d1f88409865c/query")
        .post(
            JSONObject()
                .put(
                    "filter",
                    JSONObject()
                        .put("timestamp", "last_edited_time")
                        .put("last_edited_time", JSONObject().put("after", time.toString()))
                )
                .put(
                    "sorts",
                    JSONArray()
                        .put(
                            JSONObject()
                                .put("property", "Date")
                                .put("direction", "ascending")
                        )
                ).toString().toRequestBody("application/json".toMediaType())
        )
        .header()
        .build()

    fun requestEquals(date: LocalDate) = Request.Builder()
        .url("https://api.notion.com/v1/databases/5394d1d254f14b9e85a9d1f88409865c/query")
        .post(
            JSONObject()
                .put(
                    "filter",
                    JSONObject()
                        .put("property", "Date")
                        .put("date", JSONObject().put("equals", date.toString()))
                )
                .put(
                    "sorts",
                    JSONArray()
                        .put(
                            JSONObject()
                                .put("property", "Date")
                                .put("direction", "ascending")
                        )
                ).toString().toRequestBody("application/json".toMediaType())
        )
        .header()
        .build()

    fun requestBlocks(id: String) = Request.Builder()
        .get()
        .url("https://api.notion.com/v1/blocks/${id}/children?page_size=50")
        .header()
        .build()

    fun Response?.debug(): String? {
        println()
        println(this?.request?.url)
        println(this?.request?.headers)
        println()
        println(this?.code)
        println(this?.headers)
        val body = this?.body?.string()
        println(body)
        println()
        return body
    }
}