package com.example.kanjilearning.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kanjilearning.data.KanjiDatabase
import com.example.kanjilearning.data.dao.CourseDao
import com.example.kanjilearning.data.dao.KanjiDao
import com.example.kanjilearning.data.dao.LessonDao
import com.example.kanjilearning.data.dao.QuizDao
import com.example.kanjilearning.di.SeedUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Module Hilt cung cấp Room Database, DAO và seed dữ liệu từ schema.sql.
 * EN: Hilt module wiring the Room database + DAO singletons seeded via schema.sql.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KanjiDatabase =
        Room.databaseBuilder(context, KanjiDatabase::class.java, "kanji_learning.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    SeedUtil.seedFromAsset(context, db, "schema.sql")
                }
            })
            .build()

    @Provides
    fun provideKanjiDao(database: KanjiDatabase): KanjiDao = database.kanjiDao()

    @Provides
    fun provideCourseDao(database: KanjiDatabase): CourseDao = database.courseDao()

    @Provides
    fun provideLessonDao(database: KanjiDatabase): LessonDao = database.lessonDao()

    @Provides
    fun provideQuizDao(database: KanjiDatabase): QuizDao = database.quizDao()
}
