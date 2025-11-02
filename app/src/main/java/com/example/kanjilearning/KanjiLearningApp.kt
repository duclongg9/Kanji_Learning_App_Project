package com.example.kanjilearning

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * VI: Application gắn @HiltAndroidApp để Hilt tạo graph dùng chung toàn app.
 * EN: Application annotated with @HiltAndroidApp so DI works everywhere.
 */
@HiltAndroidApp
class KanjiLearningApp : Application()
