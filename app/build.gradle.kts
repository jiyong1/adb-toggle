plugins {
    id("com.android.application")
}

android {
    namespace = "dev.ren.adbtoggle"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.ren.adbtoggle"
        // 위젯 다이내믹 컬러·모서리 반경 등 Android 12 API를 그대로 쓴다.
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // 개인 기기에 직접 설치하는 용도라 별도 키스토어 없이 디버그 키로 서명한다.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        // WRITE_SECURE_SETTINGS 는 adb 로 직접 부여하는 것을 전제로 한다.
        disable += "ProtectedPermissions"
    }
}
