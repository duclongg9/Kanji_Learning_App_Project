package com.example.kanjilearning.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kanjilearning.data.local.dao.KanjiDao
import com.example.kanjilearning.data.local.dao.UserDao
import com.example.kanjilearning.data.local.entity.KanjiEntity
import com.example.kanjilearning.data.local.entity.UserEntity

/**
 * VI: RoomDatabase gom toàn bộ DAO.
 */
@Database(
    entities = [KanjiEntity::class, UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class KanjiDatabase : RoomDatabase() {

    /**
     * VI: Truy cập DAO Kanji.
     */
    abstract fun kanjiDao(): KanjiDao

    /**
     * VI: Truy cập DAO User.
     */
    abstract fun userDao(): UserDao
}
