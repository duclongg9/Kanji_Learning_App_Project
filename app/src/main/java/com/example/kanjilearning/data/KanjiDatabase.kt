package com.example.kanjilearning.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kanjilearning.data.dao.KanjiDao
import com.example.kanjilearning.data.model.KanjiEntity

/**
 * VI: Khai báo RoomDatabase để Room tạo implementation runtime.
 * EN: Room database holding the Kanji DAO.
 */
@Database(
    entities = [KanjiEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KanjiDatabase : RoomDatabase() {
    abstract fun kanjiDao(): KanjiDao
}
