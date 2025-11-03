package com.example.kanjilearning.data.mysql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Đảm bảo load driver MySQL một lần và mở kết nối JDBC khi cần.
 * EN: Lazily loads the MySQL driver and provides JDBC connections on demand.
 */
@Singleton
class MySqlConnectionProvider @Inject constructor(
    private val config: MySqlConfig
) {

    private val driverInitialized = AtomicBoolean(false)

    /**
     * VI: Mở một kết nối mới tới máy chủ MySQL dựa trên cấu hình hiện tại.
     * EN: Opens a fresh connection to the MySQL server using the configured credentials.
     *
     * @throws MySqlDataException nếu driver không thể load hoặc kết nối thất bại.
     */
    fun openConnection(): Connection {
        try {
            if (driverInitialized.compareAndSet(false, true)) {
                Class.forName("com.mysql.cj.jdbc.Driver")
            }
            return DriverManager.getConnection(config.jdbcUrl, config.username, config.password)
        } catch (error: ClassNotFoundException) {
            throw MySqlDataException("Missing MySQL JDBC driver on classpath.", error)
        } catch (error: SQLException) {
            throw MySqlDataException("Unable to open MySQL connection: ${error.message}", error)
        }
    }
}
