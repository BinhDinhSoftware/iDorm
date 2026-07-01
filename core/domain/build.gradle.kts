plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bdsoftware.idorm.core.domain"
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
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))
    implementation(libs.hilt.android)

    implementation(libs.androidx.core.ktx)
    ksp(libs.hilt.compiler)
}







kotlin {
    jvmToolchain(11)
}
