package com.example.kanjilearning.data.mysql

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * VI: Cấu hình kết nối MySQL đọc từ BuildConfig và sinh JDBC URL.
 * EN: MySQL connection configuration derived from BuildConfig that builds the JDBC URL.
 */
data class MySqlConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val additionalParams: Map<String, String> = mapOf(
        "useUnicode" to "true",
        "characterEncoding" to "UTF-8",
        "serverTimezone" to "UTC",
        "useSSL" to "false"
    )
) {

    /**
     * VI: Tạo JDBC URL dạng `jdbc:mysql://host:port/db?params`.
     * EN: Builds the JDBC URL formatted as `jdbc:mysql://host:port/db?params`.
     */
    val jdbcUrl: String
        get() {
            val encodedParams = additionalParams.entries.joinToString("&") { (key, value) ->
                val encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8)
                "$key=$encodedValue"
            }
            return "jdbc:mysql://$host:$port/$database?$encodedParams"
        }
}
