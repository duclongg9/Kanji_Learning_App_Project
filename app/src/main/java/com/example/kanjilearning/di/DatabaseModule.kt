package com.example.kanjilearning.di

import com.example.kanjilearning.BuildConfig
import com.example.kanjilearning.data.mysql.MySqlConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Cung cấp cấu hình MySQL dùng cho datasource.
 * EN: Provides the MySQL configuration consumed by the data source layer.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * VI: Đọc thông tin kết nối từ BuildConfig và tạo MySqlConfig.
     * EN: Builds the MySqlConfig from BuildConfig connection properties.
     */
    @Provides
    @Singleton
    fun provideMySqlConfig(): MySqlConfig = MySqlConfig(
        host = BuildConfig.MYSQL_HOST,
        port = BuildConfig.MYSQL_PORT,
        database = BuildConfig.MYSQL_DB_NAME,
        username = BuildConfig.MYSQL_USER,
        password = BuildConfig.MYSQL_PASSWORD
    )
}
