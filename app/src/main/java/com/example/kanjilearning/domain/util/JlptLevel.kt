package com.example.kanjilearning.domain.util

/**
 * VI: Enum hỗ trợ lọc Kanji theo cấp độ JLPT.
 */
enum class JlptLevel(val label: String) {
    N5("N5"),
    N4("N4"),
    N3("N3"),
    N2("N2"),
    N1("N1");

    companion object {
        /**
         * VI: Parse từ chuỗi CSV; mặc định đưa về N5 để tránh crash khi dữ liệu thiếu.
         */
        fun fromLabel(input: String?): JlptLevel = entries.firstOrNull { it.label == input } ?: N5
    }
}
