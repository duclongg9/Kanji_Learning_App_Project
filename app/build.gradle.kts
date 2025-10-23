plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kanjilearning"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.kanjilearning"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // VI: Host/MySQL server nội bộ để kiểm thử (ví dụ: 10.0.2.2 khi chạy trên emulator Android).
        buildConfigField("String", "MYSQL_HOST", "\"10.0.2.2\"")
        // VI: Cổng kết nối MySQL mặc định; đổi nếu DBA quy định cổng khác.
        buildConfigField("int", "MYSQL_PORT", "3306")
        // VI: Tên database chứa bảng `users`; nhớ khởi tạo đúng schema trước khi build.
        buildConfigField("String", "MYSQL_DB_NAME", "\"kanji_app\"")
        // VI: Tài khoản MySQL có quyền INSERT/UPDATE vào bảng `users`; không commit tài khoản thật vào repo.
        buildConfigField("String", "MYSQL_USER", "\"root\"")
        // VI: Mật khẩu tương ứng với tài khoản trên; nên inject qua CI/Gradle properties khi lên môi trường thật.
        buildConfigField("String", "MYSQL_PASSWORD", "\"123456\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.auth)
    implementation(libs.mysql.connector)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
