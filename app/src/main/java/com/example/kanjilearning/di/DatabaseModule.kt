package com.example.kanjilearning.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kanjilearning.data.KanjiDatabase
import com.example.kanjilearning.data.dao.KanjiDao
import com.example.kanjilearning.data.model.KanjiEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Module cung cấp RoomDatabase và KanjiDao.
 * EN: Hilt module exposing the Room database and DAO singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KanjiDatabase =
        Room.databaseBuilder(context, KanjiDatabase::class.java, "kanji_sample.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // VI: Seed 3 Kanji mẫu khi database lần đầu được tạo.
                    // EN: Pre-populate the database with 3 demo rows.
                    listOf(
                        KanjiEntity(word = "日", meaning = "Mặt trời / Day"),
                        KanjiEntity(word = "学", meaning = "Học / Study"),
                        KanjiEntity(word = "心", meaning = "Trái tim / Heart")
                    ).forEach { entity ->
                        db.execSQL(
                            "INSERT INTO kanjis(word, meaning) VALUES(?, ?)",
                            arrayOf(entity.word, entity.meaning)
                        )
                    }
                }
            })
            .build()

    @Provides
    fun provideKanjiDao(database: KanjiDatabase): KanjiDao = database.kanjiDao()
}
