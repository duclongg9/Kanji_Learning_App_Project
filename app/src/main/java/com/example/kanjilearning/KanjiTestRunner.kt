package com.example.kanjilearning

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * VI: Test runner tuỳ biến để dùng Application thật (có Hilt) khi chạy instrumentation test.
 */
class KanjiTestRunner : AndroidJUnitRunner() {

    /**
     * VI: Khởi tạo Application thực tế để các module Hilt được load đúng.
     */
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, KanjiLearningApp::class.java.name, context)
    }
}
