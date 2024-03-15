package kr.mooner510.issuebot

data class Issue(
    val title: String,
    val content: String,
    val label: List<String>,
    val priority: Priority?
)
