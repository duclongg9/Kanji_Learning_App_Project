package com.example.kanjilearning.data.remote.mysql

import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement
import javax.inject.Inject
import javax.inject.Singleton

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

    /**
     * VI: Thêm mới một Kanji và trả về bản ghi chuẩn hoá với ID mới.
     */
    suspend fun insertKanji(kanji: Kanji): Kanji = withContext(Dispatchers.IO) {
        client.execute { connection ->
            connection.autoCommit = false
            try {
                val kanjiId = connection.prepareStatement(
                    "INSERT INTO kanji(kanji, han_viet, am_on, am_kun, mo_ta) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    statement.setString(1, kanji.character)
                    statement.setString(2, kanji.meaning)
                    statement.setString(3, kanji.onyomi)
                    statement.setString(4, kanji.kunyomi)
                    statement.setString(5, kanji.meaning)
                    statement.executeUpdate()
                    statement.generatedKeys.use { keys ->
                        if (keys.next()) keys.getLong(1) else error("Không thể tạo bản ghi kanji")
                    }
                }

                val clusterId = findClusterId(connection, kanji.jlptLevel, kanji.accessTier)

                val mappingId = connection.prepareStatement(
                    "INSERT INTO kanji_muc_do(ma_chu_kanji, muc_do_cap_do_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    statement.setLong(1, kanjiId)
                    statement.setInt(2, clusterId)
                    statement.executeUpdate()
                    statement.generatedKeys.use { keys ->
                        if (keys.next()) keys.getLong(1) else error("Không thể tạo mapping kanji_muc_do")
                    }
                }

                connection.commit()
                kanji.copy(id = mappingId, difficulty = difficultyFor(kanji.jlptLevel))
            } catch (error: Exception) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    /**
     * VI: Cập nhật Kanji đã có, bao gồm cả cluster nếu người dùng đổi JLPT/tier.
     */
    suspend fun updateKanji(kanji: Kanji) = withContext(Dispatchers.IO) {
        client.execute { connection ->
            connection.autoCommit = false
            try {
                val kanjiId = resolveKanjiId(connection, kanji.id)
                connection.prepareStatement(
                    "UPDATE kanji SET kanji=?, han_viet=?, am_on=?, am_kun=?, mo_ta=? WHERE ma_chu_kanji=?"
                ).use { statement ->
                    statement.setString(1, kanji.character)
                    statement.setString(2, kanji.meaning)
                    statement.setString(3, kanji.onyomi)
                    statement.setString(4, kanji.kunyomi)
                    statement.setString(5, kanji.meaning)
                    statement.setLong(6, kanjiId)
                    statement.executeUpdate()
                }

                val clusterId = findClusterId(connection, kanji.jlptLevel, kanji.accessTier)
                connection.prepareStatement(
                    "UPDATE kanji_muc_do SET muc_do_cap_do_id=? WHERE ma_chu_kanji_muc_do=?"
                ).use { statement ->
                    statement.setInt(1, clusterId)
                    statement.setLong(2, kanji.id)
                    statement.executeUpdate()
                }

                connection.commit()
            } catch (error: Exception) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    /**
     * VI: Xoá Kanji khỏi MySQL và dọn bản ghi trôi nếu cần.
     */
    suspend fun deleteKanji(id: Long) = withContext(Dispatchers.IO) {
        client.execute { connection ->
            connection.autoCommit = false
            try {
                val kanjiId = resolveKanjiId(connection, id)
                connection.prepareStatement(
                    "DELETE FROM kanji_muc_do WHERE ma_chu_kanji_muc_do=?"
                ).use { statement ->
                    statement.setLong(1, id)
                    statement.executeUpdate()
                }

                val hasMoreMappings = connection.prepareStatement(
                    "SELECT COUNT(*) FROM kanji_muc_do WHERE ma_chu_kanji=?"
                ).use { statement ->
                    statement.setLong(1, kanjiId)
                    statement.executeQuery().use { rs ->
                        if (rs.next()) rs.getLong(1) > 0 else false
                    }
                }

                if (!hasMoreMappings) {
                    connection.prepareStatement("DELETE FROM kanji WHERE ma_chu_kanji=?").use { statement ->
                        statement.setLong(1, kanjiId)
                        statement.executeUpdate()
                    }
                }

                connection.commit()
            } catch (error: Exception) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun findClusterId(connection: java.sql.Connection, level: JlptLevel, tier: AccessTier): Int {
        connection.prepareStatement(
            "SELECT id FROM muc_do_cap_do WHERE cap_do_code=? AND access_tier=? AND is_enabled=TRUE ORDER BY id LIMIT 1"
        ).use { statement ->
            statement.setString(1, level.label)
            statement.setString(2, tier.name)
            statement.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }
        }
        error("Không tìm thấy cụm mức độ phù hợp cho ${level.label}/${tier.name}")
    }

    private fun resolveKanjiId(connection: java.sql.Connection, mappingId: Long): Long {
        connection.prepareStatement(
            "SELECT ma_chu_kanji FROM kanji_muc_do WHERE ma_chu_kanji_muc_do=?"
        ).use { statement ->
            statement.setLong(1, mappingId)
            statement.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getLong(1)
                }
            }
        }
        error("Không tìm thấy Kanji tương ứng với mapping $mappingId")
    }
}
