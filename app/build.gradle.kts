plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.snapquest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.snapquest"
        minSdk = 24
        //noinspection OldTargetApi
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore.ktx)
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-core:1.3.4")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-camera2:1.3.4")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-view:1.3.4")
    //noinspection UseTomlInstead
    implementation("androidx.camera:camera-extensions:1.3.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //noinspection UseTomlInstead
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-analytics")
    //noinspection GradleDependency,UseTomlInstead
    implementation("com.google.firebase:firebase-auth:22.0.0")
    //noinspection UseTomlInstead
    implementation("com.squareup.okhttp3:okhttp:4.9.3")


}