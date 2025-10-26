plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    id("com.google.gms.google-services")
}

fun String.escapeForJava(): String = replace("\"", "\\\"")

val javapoetVersion = libs.versions.javapoet.get()

val mysqlHost = providers.gradleProperty("MYSQL_HOST").orElse("10.0.2.2")
val mysqlPort = providers.gradleProperty("MYSQL_PORT").orElse("3306")
val mysqlDbName = providers.gradleProperty("MYSQL_DB_NAME").orElse("kanji_app")
val mysqlUser = providers.gradleProperty("MYSQL_USER").orElse("root")
val mysqlPassword = providers.gradleProperty("MYSQL_PASSWORD").orElse("123456")
val googleWebClientId = providers.gradleProperty("GOOGLE_WEB_CLIENT_ID").orElse("")

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

        testInstrumentationRunner = "com.example.kanjilearning.KanjiTestRunner"

        buildConfigField("String", "MYSQL_HOST", "\"${mysqlHost.get().escapeForJava()}\"")
        buildConfigField("int", "MYSQL_PORT", mysqlPort.get())
        buildConfigField("String", "MYSQL_DB_NAME", "\"${mysqlDbName.get().escapeForJava()}\"")
        buildConfigField("String", "MYSQL_USER", "\"${mysqlUser.get().escapeForJava()}\"")
        buildConfigField("String", "MYSQL_PASSWORD", "\"${mysqlPassword.get().escapeForJava()}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${googleWebClientId.get().escapeForJava()}\"")
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
    constraints {
        add("kapt", "com.squareup:javapoet:$javapoetVersion") {
            because("Ensure KAPT resolves Javapoet $javapoetVersion so processors can access ClassName.canonicalName().")
        }
    }
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.hilt.android)
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.splashscreen)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.viewpager2)
    implementation(libs.mysql.connector)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

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
