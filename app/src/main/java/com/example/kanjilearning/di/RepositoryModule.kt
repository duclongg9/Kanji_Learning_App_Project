package com.example.kanjilearning.di

import com.example.kanjilearning.data.repository.KanjiRepository
import com.example.kanjilearning.data.repository.KanjiRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Bind interface Repository với implementation để Hilt inject đúng kiểu.
 * EN: Binds the repository interface to its concrete implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindKanjiRepository(impl: KanjiRepositoryImpl): KanjiRepository
}
