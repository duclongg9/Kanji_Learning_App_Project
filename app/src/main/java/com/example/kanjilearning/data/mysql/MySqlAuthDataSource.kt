package com.example.kanjilearning.data.mysql

import com.example.kanjilearning.data.security.PasswordHasher
import com.example.kanjilearning.domain.model.UserAccount
import com.example.kanjilearning.domain.model.UserRole
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Truy cập dữ liệu xác thực và role người dùng thông qua MySQL.
 * EN: Data source handling authentication and role retrieval backed by MySQL.
 */
@Singleton
class MySqlAuthDataSource @Inject constructor(
    private val connectionProvider: MySqlConnectionProvider,
    private val seedExecutor: MySqlSeedExecutor,
    private val passwordHasher: PasswordHasher
) {

    private val seedReady = AtomicBoolean(false)
    private val seedLock = Any()

    /**
     * VI: Lấy danh sách role khả dụng để hiển thị khi đăng ký.
     * EN: Loads all available roles for registration choices.
     */
    fun loadRoles(): List<UserRole> = executeWithConnection("load roles") { connection ->
        connection.prepareStatement(LIST_ROLES_SQL).use { statement ->
            statement.executeQuery().use { resultSet ->
                buildList {
                    while (resultSet.next()) {
                        add(resultSet.toRole())
                    }
                }
            }
        }
    }

    /**
     * VI: Tạo tài khoản mới và gán role dựa trên roleCode.
     * EN: Creates a new account and assigns the requested role via its code.
     */
    fun registerUser(
        email: String,
        password: String,
        displayName: String,
        roleCode: String
    ): UserAccount = executeWithConnection("register user $email") { connection ->
        connection.autoCommit = false
        try {
            val role = fetchRoleByCode(connection, roleCode)
            val userId = insertUser(connection, email, password, displayName)
            connection.prepareStatement(INSERT_USER_ROLE_SQL).use { statement ->
                statement.setLong(1, userId)
                statement.setLong(2, role.id)
                statement.executeUpdate()
            }
            connection.commit()
            fetchUserById(connection, userId) ?: throw MySqlDataException("User creation failed for $email")
        } catch (error: SQLException) {
            runCatching { connection.rollback() }
            val message = if (error.sqlState == DUPLICATE_SQL_STATE) {
                "Email đã tồn tại, vui lòng thử địa chỉ khác."
            } else {
                "Unable to register user: ${error.message}"
            }
            throw MySqlDataException(message, error)
        } finally {
            runCatching { connection.autoCommit = true }
        }
    }

    /**
     * VI: Xác thực tài khoản bằng email và mật khẩu.
     * EN: Authenticates a user via email and password.
     */
    fun authenticate(email: String, password: String): UserAccount = executeWithConnection(
        "authenticate $email"
    ) { connection ->
        connection.prepareStatement(AUTHENTICATE_SQL).use { statement ->
            statement.setString(1, email)
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    throw MySqlDataException("Tài khoản không tồn tại hoặc đã bị khoá.")
                }
                val storedHash = resultSet.getString("password_hash")
                if (!passwordHasher.matches(password, storedHash)) {
                    throw MySqlDataException("Thông tin đăng nhập không chính xác.")
                }
                resultSet.toAccount()
            }
        }
    }

    /**
     * VI: Tải thông tin user theo id, trả null nếu không tồn tại.
     * EN: Fetches a user profile by id, returning null when missing.
     */
    fun findUser(userId: Long): UserAccount? = executeWithConnection("find user $userId") { connection ->
        fetchUserById(connection, userId)
    }

    private fun fetchRoleByCode(connection: Connection, code: String): UserRole {
        connection.prepareStatement(FIND_ROLE_SQL).use { statement ->
            statement.setString(1, code)
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    throw MySqlDataException("Role $code not found")
                }
                return resultSet.toRole()
            }
        }
    }

    private fun insertUser(
        connection: Connection,
        email: String,
        password: String,
        displayName: String
    ): Long {
        val hash = passwordHasher.hash(password)
        connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS).use { statement ->
            statement.setString(1, email)
            statement.setString(2, hash)
            statement.setString(3, displayName)
            statement.setLong(4, System.currentTimeMillis())
            statement.executeUpdate()
            statement.generatedKeys.use { keys ->
                if (keys.next()) {
                    return keys.getLong(1)
                }
            }
        }
        throw MySqlDataException("Unable to allocate id for $email")
    }

    private fun fetchUserById(connection: Connection, userId: Long): UserAccount? {
        connection.prepareStatement(FIND_USER_BY_ID_SQL).use { statement ->
            statement.setLong(1, userId)
            statement.executeQuery().use { resultSet ->
                return if (resultSet.next()) resultSet.toAccount() else null
            }
        }
    }

    private fun ResultSet.toRole(): UserRole = UserRole(
        id = getLong("role_id"),
        code = getString("code"),
        displayName = getString("role_display_name")
    )

    private fun ResultSet.toAccount(): UserAccount = UserAccount(
        id = getLong("user_id"),
        email = getString("email"),
        displayName = getString("user_display_name"),
        role = UserRole(
            id = getLong("role_id"),
            code = getString("code"),
            displayName = getString("role_display_name")
        )
    )

    private fun ensureSeedReady() {
        if (seedReady.get()) return
        synchronized(seedLock) {
            if (seedReady.get()) return
            try {
                seedExecutor.ensureSeeded()
                seedReady.set(true)
            } catch (error: Throwable) {
                seedReady.set(false)
                throw error
            }
        }
    }

    private fun <T> executeWithConnection(action: String, block: (Connection) -> T): T {
        ensureSeedReady()
        try {
            connectionProvider.openConnection().use { connection ->
                return block(connection)
            }
        } catch (error: MySqlDataException) {
            throw error
        } catch (error: SQLException) {
            throw MySqlDataException("Failed to $action: ${error.message}", error)
        }
    }

    companion object {
        private const val DUPLICATE_SQL_STATE = "23000"
        private const val LIST_ROLES_SQL =
            """
            SELECT role_id, code, display_name AS role_display_name
            FROM roles
            ORDER BY role_id
            """

        private const val FIND_ROLE_SQL =
            """
            SELECT role_id, code, display_name AS role_display_name
            FROM roles
            WHERE code = ?
            """

        private const val INSERT_USER_SQL =
            """
            INSERT INTO users(email, password_hash, display_name, created_at)
            VALUES (?, ?, ?, ?)
            """

        private const val INSERT_USER_ROLE_SQL =
            """
            INSERT INTO user_roles(user_id, role_id)
            VALUES (?, ?)
            """

        private const val AUTHENTICATE_SQL =
            """
            SELECT u.user_id, u.email, u.display_name AS user_display_name, u.password_hash,
                   r.role_id, r.code, r.display_name AS role_display_name
            FROM users u
            INNER JOIN user_roles ur ON ur.user_id = u.user_id
            INNER JOIN roles r ON r.role_id = ur.role_id
            WHERE u.email = ?
            LIMIT 1
            """

        private const val FIND_USER_BY_ID_SQL =
            """
            SELECT u.user_id, u.email, u.display_name AS user_display_name,
                   r.role_id, r.code, r.display_name AS role_display_name
            FROM users u
            INNER JOIN user_roles ur ON ur.user_id = u.user_id
            INNER JOIN roles r ON r.role_id = ur.role_id
            WHERE u.user_id = ?
            LIMIT 1
            """
    }
}
