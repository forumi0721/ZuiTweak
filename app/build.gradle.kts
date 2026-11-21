plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}


val appVersion = providers.gradleProperty("appVersion").orElse("1.0.0").get()
val appVersionParts = appVersion.split(".").map(String::toInt)
require(appVersionParts.size == 3 && appVersionParts.all { it in 0..999 }) {
    "appVersion must use MAJOR.MINOR.PATCH with each part between 0 and 999"
}
val computedVersionCode = appVersionParts[0] * 1_000_000 + appVersionParts[1] * 1_000 + appVersionParts[2]


android {
    namespace = "kr.stonecold.zuitweak"
    compileSdk = 37
    compileSdkMinor = 1
    defaultConfig {
        applicationId = "kr.stonecold.zuitweak"
        minSdk = 34
        targetSdk = 37
        versionCode = computedVersionCode
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val releaseStore = providers.gradleProperty("keystoreFile")
                .map(::File)
                .orElse(File(projectDir, "release-keystore.jks"))
                .get()
            storeFile = releaseStore
            storePassword = System.getenv("KEY_STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    bundle {
        language {
            enableSplit = false
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        warningsAsErrors = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    compileOnly(project(":hidden-api"))

    compileOnly(libs.xposed.api)
    implementation(libs.libsu.core)
}
