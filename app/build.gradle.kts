plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.crimeintelcompanion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.crimeintelcompanion"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // ✅ ViewBinding enabled (modern syntax)
    buildFeatures {
        viewBinding = true
    }

    // ✅ Optional (to avoid vector crashes on older devices)
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/NOTICE.txt",
                "META-INF/LICENSE.txt"
            )
        }
    }


dependencies {

    // --- 🧩 Android Core ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")

    // --- 🌐 Networking & JSON ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // ✅ for debugging API calls
    implementation("com.google.code.gson:gson:2.10.1")

    // --- 📱 Lifecycle / Jetpack ---
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")


    // --- 📍 Google Play Services (optional for GPS) ---
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // --- 🧪 Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}}
