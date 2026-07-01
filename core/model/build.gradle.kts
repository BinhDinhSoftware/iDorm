plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.bdsoftware.idorm.core.model"
    compileSdk = 35

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Pure kotlin module
}

kotlin {
    jvmToolchain(11)
}
