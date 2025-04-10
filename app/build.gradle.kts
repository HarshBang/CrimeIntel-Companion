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

    buildFeatures {
        viewBinding {
            enable = true
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.amazonaws:aws-android-sdk-core:2.73.0")
    implementation ("com.amazonaws:aws-android-sdk-s3:2.73.0")
    implementation ("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.73.0")
}