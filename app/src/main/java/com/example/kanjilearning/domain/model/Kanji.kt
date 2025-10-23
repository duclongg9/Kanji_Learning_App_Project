package com.example.kanjilearning.domain.model

import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel

/**
 * VI: Model domain đại diện cho một Kanji được hiển thị trên UI.
 */
data class Kanji(
    val id: Long,
    val character: String,
    val onyomi: String,
    val kunyomi: String,
    val meaning: String,
    val jlptLevel: JlptLevel,
    val difficulty: Int,
    val accessTier: AccessTier
)
