package kr.mooner510.pokadiary

object Env {
    var DELAY: Long = (60 * 2.5 * 1000).toLong()
    var NOTIFY_CHANNEL_ID: String = ""
    var COMMAND_CHANNEL_ID: String = ""
    var NOTION_KEY: String = ""
    var NOTION_VERSION: String = ""

    fun init() {
        println("ENV Configuration Found: Setup ENVIRONMENT VARIABLES")
        System.getenv("DELAY")?.let { DELAY = it.toLong() }
        System.getenv("NOTIFY_CHANNEL_ID")?.let { NOTIFY_CHANNEL_ID = it }
        System.getenv("COMMAND_CHANNEL_ID")?.let { COMMAND_CHANNEL_ID = it }

        System.getenv("NOTION_KEY")?.let { NOTION_KEY = it }
        System.getenv("NOTION_VERSION")?.let { NOTION_VERSION = it }
    }
}