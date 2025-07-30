plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.deflatam_pruebafinal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.deflatam_pruebafinal"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Se añaden posibles dependencias al proyecto:

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")

    // Para usar ViewModel en Jetpack Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")

    // Compose navigation
    implementation("androidx.navigation:navigation-compose:2.9.3")

    // Iconos extendidos para Material Design
    implementation("androidx.compose.material:material-icons-extended")
    // ---- Room (Base de datos) ----
    implementation("androidx.room:room-runtime:2.7.2")
    // Para usar 'suspend' en los DAOs (Coroutines)
    implementation("androidx.room:room-ktx:2.7.2")
    // Procesador de anotaciones de Room (usando ksp)
    ksp("androidx.room:room-compiler:2.7.2")

    // Coil para Jetpack Compose
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
}