package com.example.kanjilearning.data.repository.auth

import com.example.kanjilearning.data.local.AuthLocalDataSource
import com.example.kanjilearning.data.mysql.MySqlAuthDataSource
import com.example.kanjilearning.domain.model.UserAccount
import com.example.kanjilearning.domain.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * VI: Repository kết hợp datasource MySQL và bộ nhớ local cho session.
 * EN: Repository combining the MySQL datasource with local storage for session state.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: MySqlAuthDataSource,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    private val sessionFlow = MutableStateFlow(localDataSource.loadSession())

    override fun observeSession(): Flow<UserAccount?> = sessionFlow.asStateFlow()

    override suspend fun login(email: String, password: String): UserAccount {
        val account = withContext(Dispatchers.IO) {
            authDataSource.authenticate(email, password)
        }
        persistSession(account)
        return account
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        roleCode: String
    ): UserAccount {
        val account = withContext(Dispatchers.IO) {
            authDataSource.registerUser(email, password, displayName, roleCode)
        }
        persistSession(account)
        return account
    }

    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            localDataSource.clearSession()
        }
        sessionFlow.value = null
    }

    override suspend fun loadRoles(): List<UserRole> = withContext(Dispatchers.IO) {
        authDataSource.loadRoles()
    }

    private fun persistSession(account: UserAccount) {
        localDataSource.saveSession(account)
        sessionFlow.value = account
    }
}
