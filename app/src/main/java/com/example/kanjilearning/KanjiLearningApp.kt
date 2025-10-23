package com.example.kanjilearning

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * VI: Application chính đăng ký với Hilt để toàn bộ app có thể sử dụng DI.
 * Đồng thời cấu hình WorkManager để dùng HiltWorkerFactory cho các Worker được inject.
 */
@HiltAndroidApp
class KanjiLearningApp : Application(), Configuration.Provider {

    /**
     * VI: Hilt tự động cung cấp HiltWorkerFactory giúp WorkManager biết cách khởi tạo Worker có inject.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * VI: Trả về cấu hình WorkManager mặc định nhưng gắn kèm factory Hilt.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
