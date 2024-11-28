plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.sami.pstudocscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sami.pstudocscanner"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ML-kit for scanner
    implementation(libs.play.services.mlkit.document.scanner)


    // ML Kit OCR
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Apache POI for DOCX creation
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("org.apache.xmlbeans:xmlbeans:5.1.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    // loading images efficiently
    implementation(libs.coil.compose)

    // Navigation class
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Pager and Indicators - Accompanist
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    // pdf Viewer
    implementation(libs.pdf.viewer)

    // viewmodel in compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // data - store in android
    implementation (libs.androidx.datastore.preferences)
}