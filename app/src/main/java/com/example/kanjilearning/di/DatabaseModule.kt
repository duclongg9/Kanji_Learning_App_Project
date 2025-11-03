package com.example.kanjilearning.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kanjilearning.data.KanjiDatabase
import com.example.kanjilearning.data.dao.KanjiDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Module Hilt cung cấp Room Database, DAO và seed dữ liệu mẫu.
 * EN: Hilt module that exposes the Room database and seeds demo rows.
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
                    // VI/EN: Seed a few Kanji so the RecyclerView is not empty on first launch.
                    db.execSQL("INSERT INTO kanjis(word, meaning) VALUES('日', 'Mặt trời / Sun')")
                    db.execSQL("INSERT INTO kanjis(word, meaning) VALUES('学', 'Học / Study')")
                    db.execSQL("INSERT INTO kanjis(word, meaning) VALUES('心', 'Trái tim / Heart')")
                }
            })
            .build()

    @Provides
    fun provideKanjiDao(database: KanjiDatabase): KanjiDao = database.kanjiDao()
}
