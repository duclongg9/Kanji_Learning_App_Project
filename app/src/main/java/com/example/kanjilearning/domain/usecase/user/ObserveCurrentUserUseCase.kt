package com.example.kanjilearning.domain.usecase.user

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * VI: UseCase dùng cho UI để lắng nghe thông tin user hiện tại.
 */
class ObserveCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUser()
}
