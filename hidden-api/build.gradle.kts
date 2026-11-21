plugins {
    id("com.android.library")
}

android {
    namespace = "kr.stonecold.hidden_api"
    compileSdk = 37
    compileSdkMinor = 1
    defaultConfig {
        minSdk = 34
    }
    compileOptions {
        // Refine 4.4.0's annotation processor supports Java 11 source; the app module remains Java 17.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    //noinspection UseTomlInstead
    annotationProcessor("dev.rikka.tools.refine:annotation-processor:4.4.0")
    //noinspection UseTomlInstead
    compileOnly("dev.rikka.tools.refine:annotation:4.4.0")
}
