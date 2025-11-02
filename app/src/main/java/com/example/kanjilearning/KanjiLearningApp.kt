package com.example.kanjilearning

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * VI: Application gắn annotation Hilt để bật Dependency Injection.
 * EN: Minimal Application class required by Hilt.
 */
@HiltAndroidApp
class KanjiLearningApp : Application()
