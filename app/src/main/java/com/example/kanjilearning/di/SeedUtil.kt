package com.example.kanjilearning.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.BufferedReader

/**
 * VI: Đọc file schema.sql trong assets và execute tuần tự các câu lệnh.
 * EN: Helper that replays the SQL statements stored in assets/schema.sql on DB creation.
 */
object SeedUtil {

    fun seedFromAsset(context: Context, db: SupportSQLiteDatabase, assetName: String) {
        runCatching {
            context.assets.open(assetName).bufferedReader().use { reader ->
                executeStatements(reader, db)
            }
        }.onFailure { error ->
            error.printStackTrace()
        }
    }

    private fun executeStatements(reader: BufferedReader, db: SupportSQLiteDatabase) {
        val statementBuilder = StringBuilder()
        reader.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("--")) {
                return@forEach
            }
            statementBuilder.append(rawLine)
            if (line.endsWith(";")) {
                val statement = statementBuilder.toString().trim().removeSuffix(";")
                if (statement.isNotBlank()) {
                    db.execSQL(statement)
                }
                statementBuilder.clear()
            } else {
                statementBuilder.append('\n')
            }
        }
        val leftover = statementBuilder.toString().trim()
        if (leftover.isNotBlank()) {
            db.execSQL(leftover)
        }
    }
}
