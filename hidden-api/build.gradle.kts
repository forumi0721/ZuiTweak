plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "kr.stonecold.hidden_api"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 34
    }
    compileOptions {
        // Refine 4.4.0's annotation processor requires Java 11 source compatibility.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    annotationProcessor(libs.refine.annotation.processor)
    compileOnly(libs.refine.annotation)
}
