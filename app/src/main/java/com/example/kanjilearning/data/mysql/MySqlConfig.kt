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
        "useSSL" to "false",
        "allowPublicKeyRetrieval" to "true",
        "connectTimeout" to "10000",
        "socketTimeout" to "10000",
        "tcpKeepAlive" to "true"
    )
) {
    val jdbcUrl: String
        get() {
            val encodedParams = additionalParams.entries.joinToString("&") { (k, v) ->
                val enc = URLEncoder.encode(v, StandardCharsets.UTF_8)
                "$k=$enc"
            }
            // DÙNG CÁC FIELD host/port/database THAY VÌ CHUỖI CỨNG
            return "jdbc:mysql://$host:$port/$database?$encodedParams"
        }
}
