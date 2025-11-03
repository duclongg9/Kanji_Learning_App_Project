package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.LearningRepository
import com.example.kanjilearning.domain.model.PaymentReceipt
import javax.inject.Inject

/**
 * VI: Mô phỏng thanh toán MoMo để mở khóa khoá học.
 * EN: Performs the simulated MoMo payment and returns a receipt.
 */
class UnlockCourseWithMomoUseCase @Inject constructor(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(courseId: Long, phoneNumber: String): PaymentReceipt =
        repository.unlockCourseWithMomo(courseId, phoneNumber)
}
