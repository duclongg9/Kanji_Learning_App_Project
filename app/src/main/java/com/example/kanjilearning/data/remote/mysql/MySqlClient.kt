package com.example.kanjilearning.data.remote.mysql

import com.example.kanjilearning.BuildConfig
import java.sql.Connection
import java.sql.DriverManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Lớp tiện ích tạo kết nối JDBC tới MySQL dựa trên BuildConfig.
 */
@Singleton
class MySqlClient @Inject constructor() {

    private val connectionUrl: String =
        "jdbc:mysql://${BuildConfig.MYSQL_HOST}:${BuildConfig.MYSQL_PORT}/${BuildConfig.MYSQL_DB_NAME}" +
            "?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true"

    init {
        Class.forName("com.mysql.cj.jdbc.Driver")
    }

    /**
     * VI: Thực thi khối lệnh với một Connection và đảm bảo đóng kết nối sau khi dùng xong.
     */
    fun <T> execute(block: (Connection) -> T): T {
        DriverManager.getConnection(connectionUrl, BuildConfig.MYSQL_USER, BuildConfig.MYSQL_PASSWORD).use { connection ->
            return block(connection)
        }
    }
}
