package com.example.kanjilearning.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kanjilearning.data.datastore.UserPreferencesDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant

/**
 * VI: Worker gửi nhắc nhở ôn tập SRS hằng ngày.
 */
@HiltWorker
class SrsReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferencesDataSource: UserPreferencesDataSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // VI: Ghi log/logic gửi notification thật sự; ở đây chỉ cập nhật timestamp để demo.
        val now = Instant.now().toEpochMilli()
        preferencesDataSource.updateLastReminder(now)
        return Result.success()
    }
}
