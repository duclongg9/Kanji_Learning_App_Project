package com.example.kanjilearning.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Scheduler dùng để lập lịch Worker nhắc SRS mỗi ngày.
 */
@Singleton
class SrsReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val WORK_NAME = "srs_reminder_work"
    }

    /**
     * VI: Đặt lịch chạy Worker mỗi 24 giờ.
     */
    fun scheduleDailyReminder() {
        val request = PeriodicWorkRequestBuilder<SrsReminderWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
