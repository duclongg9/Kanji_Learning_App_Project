package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.auth.AuthRepository
import com.example.kanjilearning.domain.model.UserRole
import javax.inject.Inject

/**
 * VI: Use case tải danh sách role hỗ trợ từ server.
 * EN: Use case retrieving supported roles from the backend.
 */
class LoadAvailableRolesUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): List<UserRole> = repository.loadRoles()
}
