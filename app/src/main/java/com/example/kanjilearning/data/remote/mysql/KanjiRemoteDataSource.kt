package com.example.kanjilearning.data.remote.mysql

import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * VI: DataSource đọc danh sách Kanji trực tiếp từ MySQL.
 */
@Singleton
class KanjiRemoteDataSource @Inject constructor(
    private val client: MySqlClient
) {

    suspend fun fetchKanjiCatalog(allowedTiers: List<AccessTier>): List<Kanji> =
        withContext(Dispatchers.IO) {
            if (allowedTiers.isEmpty()) return@withContext emptyList<Kanji>()
            client.execute { connection ->
                val placeholders = allowedTiers.joinToString(separator = ",") { "?" }
                val sql = """
                    SELECT
                        ma_chu_kanji_muc_do AS id,
                        kanji,
                        IFNULL(han_viet, '') AS han_viet,
                        IFNULL(am_on, '') AS am_on,
                        IFNULL(am_kun, '') AS am_kun,
                        IFNULL(mo_ta, '') AS mo_ta,
                        cap_do_code,
                        access_tier
                    FROM v_kanji_catalog
                    WHERE is_enabled = TRUE AND access_tier IN ($placeholders)
                    ORDER BY cap_do_code ASC, kanji ASC
                """.trimIndent()

                connection.prepareStatement(sql).use { statement ->
                    allowedTiers.forEachIndexed { index, tier ->
                        statement.setString(index + 1, tier.name)
                    }
                    statement.executeQuery().use { resultSet ->
                        val records = mutableListOf<Kanji>()
                        while (resultSet.next()) {
                            val jlpt = JlptLevel.fromLabel(resultSet.getString("cap_do_code"))
                            val tier = runCatching {
                                AccessTier.valueOf(resultSet.getString("access_tier"))
                            }.getOrDefault(AccessTier.FREE)
                            val meaning = resultSet.getString("han_viet").ifBlank {
                                resultSet.getString("mo_ta")
                            }
                            records += Kanji(
                                id = resultSet.getLong("id"),
                                character = resultSet.getString("kanji"),
                                onyomi = resultSet.getString("am_on"),
                                kunyomi = resultSet.getString("am_kun"),
                                meaning = meaning,
                                jlptLevel = jlpt,
                                difficulty = difficultyFor(jlpt),
                                accessTier = tier
                            )
                        }
                        records
                    }
                }
            }
        }

    private fun difficultyFor(level: JlptLevel): Int = when (level) {
        JlptLevel.N5 -> 1
        JlptLevel.N4 -> 3
        JlptLevel.N3 -> 5
        JlptLevel.N2 -> 7
        JlptLevel.N1 -> 9
    }
}
