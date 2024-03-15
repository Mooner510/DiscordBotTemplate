package kr.mooner510.issuebot

enum class Priority {
    High,
    Medium,
    Low;

    companion object {
        fun parse(s: String): Priority? = when (s) {
            "높음" -> High
            "중간" -> Medium
            "낮음" -> Low
            else -> null
        }
    }
}