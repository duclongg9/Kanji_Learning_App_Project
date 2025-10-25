plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

val javapoetVersion = libs.versions.javapoet.get()

// Ép Javapoet cho MỌI cấu hình (implementation/kapt/annotationProcessor…)
configurations.all {
    resolutionStrategy.force("com.squareup:javapoet:$javapoetVersion")
}

android {
    namespace = "com.example.kanjilearning"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kanjilearning"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Nếu bạn có class runner riêng thì đổi lại tên class của bạn.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Đảm bảo Gradle kéo đúng artifact Javapoet đã force
    compileOnly("com.squareup:javapoet:$javapoetVersion")

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Navigation (giữ một bộ, tránh trùng)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation(libs.play.services.auth)

    // Room (đồng bộ phiên bản giữa runtime/ktx/compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    // Hilt
    implementation(libs.hilt.android)

    // WorkManager + Hilt integration
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)

    // Khác
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.splashscreen)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.viewpager2)
    implementation(libs.mysql.connector)

    // Annotation processors (KHÔNG trộn KSP + KAPT cho cùng 1 lib)
    kapt(libs.room.compiler)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.compiler.androidx)
    kapt(libs.javapoet)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

kapt {
    correctErrorTypes = true
}
