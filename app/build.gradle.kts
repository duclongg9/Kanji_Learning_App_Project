plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

val javapoetVersion = libs.versions.javapoet.get()

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.squareup" && requested.name == "javapoet") {
            useVersion(javapoetVersion)
            because("Hilt requires ClassName.canonicalName() available in Javapoet $javapoetVersion")
        }
    }
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MYSQL_HOST", "\"10.0.2.2\"")
        buildConfigField("int", "MYSQL_PORT", "3306")
        buildConfigField("String", "MYSQL_DB_NAME", "\"kanji_test\"")
        buildConfigField("String", "MYSQL_USERNAME", "\"root\"")
        buildConfigField("String", "MYSQL_PASSWORD", "\"123456\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    constraints {
        add("kapt", "com.squareup:javapoet:$javapoetVersion") {
            because("Ensure KAPT resolves Javapoet $javapoetVersion so processors can access ClassName.canonicalName().")
        }
    }

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.activity.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.hilt.android)
    implementation(libs.mysql.connector)

    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

kapt {
    correctErrorTypes = true
}
