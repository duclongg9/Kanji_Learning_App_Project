package com.example.kanjilearning.data.mysql

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.IOException
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Thực thi file schema.sql trên máy chủ MySQL để tạo bảng và seed dữ liệu.
 * EN: Executes the schema.sql asset against MySQL to create tables and seed baseline data.
 */
@Singleton
class MySqlSeedExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionProvider: MySqlConnectionProvider
) {

    private val seeded = AtomicBoolean(false)

    /**
     * VI: Đảm bảo script chỉ chạy một lần trong vòng đời ứng dụng.
     * EN: Guarantees the seed script is executed at most once per app lifetime.
     */
    fun ensureSeeded(assetName: String = "schema.sql") {
        if (seeded.compareAndSet(false, true)) {
            runCatching {
                executeSeed(assetName)
            }.onFailure { error ->
                seeded.set(false)
                throw if (error is MySqlDataException) error else MySqlDataException(
                    "Failed to seed MySQL database from $assetName",
                    error
                )
            }
        }
    }

    /**
     * VI: Đọc nội dung file SQL, loại bỏ comment và execute từng câu lệnh trong transaction.
     * EN: Reads the SQL script, strips comments, and executes each statement in a transaction.
     */
    private fun executeSeed(assetName: String) {
        val statements = loadStatements(assetName)
        connectionProvider.openConnection().use { connection ->
            runStatements(connection, statements)
        }
    }

    /**
     * VI: Load và tiền xử lý danh sách câu lệnh SQL từ asset.
     * EN: Loads and pre-processes SQL statements from the asset file.
     */
    private fun loadStatements(assetName: String): List<String> {
        return try {
            context.assets.open(assetName).bufferedReader().use { reader ->
                parseStatements(reader)
            }
        } catch (error: IOException) {
            throw MySqlDataException("Unable to read asset $assetName", error)
        }
    }

    /**
     * VI: Chạy tuần tự từng câu lệnh trong transaction và rollback nếu lỗi.
     * EN: Executes each statement inside a transaction and rolls back on failure.
     */
    private fun runStatements(connection: Connection, statements: List<String>) {
        if (statements.isEmpty()) return
        try {
            connection.autoCommit = false
            connection.createStatement().use { statement ->
                statements.forEach { sql ->
                    statement.execute(sql)
                }
            }
            connection.commit()
        } catch (error: SQLException) {
            runCatching { connection.rollback() }
            throw MySqlDataException("Error executing seed SQL: ${error.message}", error)
        } finally {
            runCatching { connection.autoCommit = true }
        }
    }

    /**
     * VI: Tách SQL thành từng statement, bỏ comment dạng `--` và dòng rỗng.
     * EN: Splits the SQL content into individual statements while removing `--` comments and blanks.
     */
    private fun parseStatements(reader: BufferedReader): List<String> {
        val builder = StringBuilder()
        val statements = mutableListOf<String>()
        reader.lineSequence().forEach { line ->
            val sanitized = line.substringBefore("--").trim()
            if (sanitized.isEmpty()) {
                return@forEach
            }
            builder.append(sanitized)
            if (sanitized.endsWith(";")) {
                val statement = builder.toString().trim().removeSuffix(";")
                if (statement.isNotEmpty()) {
                    statements.add(statement)
                }
                builder.clear()
            } else {
                builder.append(' ')
            }
        }
        val tail = builder.toString().trim().removeSuffix(";")
        if (tail.isNotEmpty()) {
            statements.add(tail)
        }
        return statements
    }
}
