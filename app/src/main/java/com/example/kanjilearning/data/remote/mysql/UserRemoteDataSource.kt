package com.example.kanjilearning.data.remote.mysql

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.util.Role
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * VI: DataSource đồng bộ thông tin người dùng với MySQL.
 */
@Singleton
class UserRemoteDataSource @Inject constructor(
    private val client: MySqlClient
) {

    suspend fun upsertUser(user: User) = withContext(Dispatchers.IO) {
        client.execute { connection ->
            val email = user.email.ifBlank { "${user.googleId}@placeholder.local" }
            val displayName = user.displayName.ifBlank { email }
            val sql = """
                INSERT INTO users(user_name, email, status, role_id, oauth_provider, oauth_subject)
                VALUES(?, ?, 'ACTIVE', ?, 'google', ?)
                ON DUPLICATE KEY UPDATE
                    user_name = VALUES(user_name),
                    status = VALUES(status),
                    role_id = VALUES(role_id),
                    oauth_provider = VALUES(oauth_provider),
                    oauth_subject = VALUES(oauth_subject)
            """.trimIndent()
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, displayName)
                statement.setString(2, email)
                statement.setInt(3, user.role.id)
                statement.setString(4, user.googleId)
                statement.executeUpdate()
            }
        }
    }

    suspend fun updateRole(googleId: String, role: Role) = withContext(Dispatchers.IO) {
        client.execute { connection ->
            val sql = """
                UPDATE users SET role_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE oauth_provider = 'google' AND oauth_subject = ?
            """.trimIndent()
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, role.id)
                statement.setString(2, googleId)
                statement.executeUpdate()
            }
        }
    }
}
