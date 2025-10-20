plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.navigation.safe.args)

    alias(libs.plugins.ksp)

    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.google.gms.google.services)    // KSP plugin
}

android {
    namespace = "com.example.chatays"
    compileSdk = 36
    
    buildFeatures{
        dataBinding= true
    }

    defaultConfig {
        applicationId = "com.example.chatays"
        minSdk = 24
        targetSdk = 35
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

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    //implementation(libs.androidx.credentials)
   //implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.okhttp)                  // okhttp
    implementation(libs.okhttp.logging)          // logging interceptor
    implementation(libs.retrofit)
    implementation(libs.gson)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    implementation(libs.hilt)
    ksp(libs.hilt.kapt)

    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation ("io.getstream:photoview:1.0.3")

    implementation ("com.google.android.gms:play-services-auth:21.2.0")
    implementation ("com.vanniktech:emoji-google:0.9.0") //emıji kütüphanesi
}
