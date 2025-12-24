plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.musictranscription.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.musictranscription.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Network - OkHttp for downloading audio from URLs
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Audio processing
    // 1. 确保 Mobile FFmpeg 存在 (用于 MP3 -> WAV 转码)
    implementation("com.arthenica:mobile-ffmpeg-full:4.4")


    // 添加 Gson 用于序列化音符数据传给 HTML
    implementation("com.google.code.gson:gson:2.10.1")

    // 添加 TarsosDSP 库 (音频分析)
    // 注意：TarsosDSP 通常需要下载 jar 包放入 libs 目录，或者使用第三方镜像
    // 这里为了方便，我们假设你使用 JitPack 或者你手动下载了 jar
    // 如果无法解析，请访问 https://0110.be/releases/TarsosDSP/TarsosDSP-latest/ 下载 TarsosDSP-Android-latest.jar 放入 app/libs/ 文件夹
    implementation(files("libs/TarsosDSP-Android-latest.jar"))

    // MIDI library
    // implementation("com.leff:midi:1.0")
    // implementation("com.github.crowjdh:AndroidMidiLibrary:master-SNAPSHOT")
    implementation("com.github.LeffelMania:android-midi-lib:master-SNAPSHOT")

    // PDF generation
    implementation("com.itextpdf:itext7-core:7.2.5")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
