package com.example.kanjilearning.di

import com.example.kanjilearning.data.repository.KanjiRepositoryImpl
import com.example.kanjilearning.data.repository.UserRepositoryImpl
import com.example.kanjilearning.domain.repository.KanjiRepository
import com.example.kanjilearning.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Bind interface repository với implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindKanjiRepository(impl: KanjiRepositoryImpl): KanjiRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
