import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// 서명 정보는 저장소에 올리지 않는다. 파일이 없으면 release APK는 서명 없이 빌드된다 (CI 포함).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// AI 서버 주소와 키. 저장소에 올리지 않는 local.properties에 적으면 바꿀 수 있다.
//   kidsrhythm.serverUrl=http://192.168.0.10:8000   (기본: 에뮬레이터에서 본 이 PC, http://10.0.2.2:8000)
//   kidsrhythm.apiKey=...                            (서버에 KR_API_KEY를 정했을 때만)
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val serverUrl: String = localProperties.getProperty("kidsrhythm.serverUrl") ?: "http://10.0.2.2:8000"
val serverApiKey: String = localProperties.getProperty("kidsrhythm.apiKey") ?: ""

android {
    namespace = "com.hmgappchallenge.kidsrhythm"
    compileSdk = 35
    // AAOS Car API (속도·바깥 온도·주행 제한). SDK 플랫폼의 optional/android.car.jar를 쓴다
    useLibrary("android.car")

    defaultConfig {
        applicationId = "com.hmgappchallenge.kidsrhythm"
        // minSdk 28: Pleos Connect 차량 사이버 보안 요건. targetSdk 34: Pleos Connect 에뮬레이터 이미지 기준.
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")
        buildConfigField("String", "SERVER_API_KEY", "\"$serverApiKey\"")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.filament.utils.android)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
