import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
}

fun getVersionName(): String? {
    return try {
        val df = SimpleDateFormat("yyyy.MM.dd")
        val date = Date.from(ZonedDateTime.now().toInstant())
        df.format(date)
    } catch (ignored: Exception) {
        null
    }
}

android {
    namespace = "ru.mark99.carapp.geelynavigatorcrashfix"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.mark99.carapp.geelynavigatorcrashfix"
        minSdk = 28
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 28
        versionCode = 1
        versionName = "${getVersionName()}"

        setProperty("archivesBaseName", "GeelyNavCrashFix-${getVersionName()}")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}

dependencies {
    implementation(libs.androidx.preference)
    implementation(libs.androidx.activity)
}