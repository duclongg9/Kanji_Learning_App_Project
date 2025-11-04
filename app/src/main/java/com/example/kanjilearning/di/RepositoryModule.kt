package com.example.kanjilearning.di

import com.example.kanjilearning.data.repository.LearningRepository
import com.example.kanjilearning.data.repository.LearningRepositoryImpl
import com.example.kanjilearning.data.repository.auth.AuthRepository
import com.example.kanjilearning.data.repository.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Bind repository abstraction với implementation mới.
 * EN: Bind the learning repository implementation for DI consumers.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLearningRepository(impl: LearningRepositoryImpl): LearningRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
