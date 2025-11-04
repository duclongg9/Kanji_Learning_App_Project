package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.auth.AuthRepository
import com.example.kanjilearning.domain.model.UserAccount
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * VI: Use case quan sát session đăng nhập hiện tại.
 * EN: Use case observing the active authentication session.
 */
class ObserveAuthSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<UserAccount?> = repository.observeSession()
}
