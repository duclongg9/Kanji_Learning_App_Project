package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.KanjiRepository
import com.example.kanjilearning.data.model.KanjiEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * VI: UseCase là điểm vào của tầng domain, dễ dàng test và tái sử dụng.
 * EN: Use case exposing the read operation to the presentation layer.
 */
class GetAllKanjisUseCase @Inject constructor(
    private val repository: KanjiRepository
) {
    operator fun invoke(): Flow<List<KanjiEntity>> = repository.getAll()
}
