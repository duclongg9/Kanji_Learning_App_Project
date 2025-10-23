package com.example.kanjilearning.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kanjilearning.data.local.dao.KanjiDao
import com.example.kanjilearning.data.local.dao.UserDao
import com.example.kanjilearning.data.local.db.KanjiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * VI: Module Hilt cung cấp RoomDatabase và DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KanjiDatabase =
        Room.databaseBuilder(context, KanjiDatabase::class.java, "kanji.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // VI: Seed vài Kanji mẫu để app hiển thị ngay khi offline.
                    db.execSQL(
                        "INSERT INTO kanji(character, onyomi, kunyomi, meaning, jlpt_level, difficulty, access_tier) " +
                            "VALUES ('日', 'ニチ', 'ひ', 'Mặt trời, ngày', 'N5', 1, 'FREE')"
                    )
                    db.execSQL(
                        "INSERT INTO kanji(character, onyomi, kunyomi, meaning, jlpt_level, difficulty, access_tier) " +
                            "VALUES ('学', 'ガク', 'まな.ぶ', 'Học', 'N5', 2, 'FREE')"
                    )
                    db.execSQL(
                        "INSERT INTO kanji(character, onyomi, kunyomi, meaning, jlpt_level, difficulty, access_tier) " +
                            "VALUES ('輝', 'キ', 'かがや.く', 'Toả sáng', 'N2', 4, 'VIP')"
                    )
                }
            })
            .build()

    @Provides
    fun provideKanjiDao(database: KanjiDatabase): KanjiDao = database.kanjiDao()

    @Provides
    fun provideUserDao(database: KanjiDatabase): UserDao = database.userDao()
}
