

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
   // Phiên bản có thể thay đổi, kiểm tra tại https://firebase.google.com/docs/android/setup

    id("com.google.gms.google-services")
    id ("kotlin-kapt")
}
android {
    namespace = "com.example.footballquizz"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.footballquizz"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
  implementation(libs.firebase.storage.ktx)
  testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
  implementation ("com.google.android.gms:play-services-auth:20.7.0")

  implementation ("com.google.android.material:material:1.9.0")
    implementation (libs.androidx.appcompat.v131)
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("androidx.core:core:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.firebase:firebase-storage")
  implementation ("com.google.firebase:firebase-auth:21.0.0")
  implementation ("com.google.firebase:firebase-firestore:24.0.0")
  implementation ("com.google.firebase:firebase-analytics:21.0.0")

}
