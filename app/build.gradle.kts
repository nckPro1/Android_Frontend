plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.frontend"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.frontend"
        minSdk = 26
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
    // Bật ViewBinding để dễ dàng tương tác với UI
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // HTTP requests & API Calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    // ✅ ĐÃ THÊM: Retrofit để gọi API dễ dàng hơn
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Jackson for JSON serialization/deserialization
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")


    // Coordinator Layout for HomeActivity
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Google Sign-In
    // 👍 GIỮ LẠI: Thư viện này cần thiết để lấy ID Token
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Cập nhật phiên bản mới nhất

    // Image Loading & Picker
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.fragment:fragment:1.6.2")

    // Firebase Realtime Database (Chat)
    implementation("com.google.firebase:firebase-database:20.3.1")

    // Firebase Cloud Messaging (Notification)
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}