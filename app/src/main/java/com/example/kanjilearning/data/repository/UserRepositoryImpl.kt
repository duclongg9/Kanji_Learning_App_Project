package com.example.kanjilearning.data.repository

import android.util.Log
import com.example.kanjilearning.data.local.datasource.UserLocalDataSource
import com.example.kanjilearning.data.local.entity.UserEntity
import com.example.kanjilearning.data.remote.mysql.UserRemoteDataSource
import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import com.example.kanjilearning.domain.util.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Repository quản lý user dựa trên Room.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {

    override fun observeCurrentUser(): Flow<User?> =
        localDataSource.observeUser().map { entity -> entity?.toDomain() }

    override suspend fun getCurrentUser(): User? = localDataSource.getUser()?.toDomain()

    override suspend fun saveUser(user: User) {
        val syncedUser = user.copy(lastSyncedAt = System.currentTimeMillis())
        remoteDataSource.upsertUser(syncedUser)
        localDataSource.upsert(UserEntity.fromDomain(syncedUser))
    }

    override suspend fun updateRole(role: Role) {
        val current = localDataSource.getUser() ?: return
        try {
            remoteDataSource.updateRole(current.googleId, role)
        } catch (error: Exception) {
            Log.w(TAG, "Không thể cập nhật role trên MySQL", error)
        }
        localDataSource.upsert(
            current.copy(role = role.id, lastSyncedAt = System.currentTimeMillis())
        )
    }

    override suspend fun clear() {
        localDataSource.clear()
    }
    companion object {
        private const val TAG = "UserRepository"
    }
}
