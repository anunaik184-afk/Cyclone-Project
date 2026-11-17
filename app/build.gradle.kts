plugins {
    alias(libs.plugins.android.application)
}

android {
<<<<<<< HEAD
    namespace = "com.example.loginpage"
=======
    namespace = "com.example.shelter"
>>>>>>> origin/shelter-page
    compileSdk {
        version = release(37)
    }

    defaultConfig {
<<<<<<< HEAD
        applicationId = "com.example.loginpage"
=======
        applicationId = "com.example.shelter"
>>>>>>> origin/shelter-page
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
<<<<<<< HEAD
                enable = false
=======
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
>>>>>>> origin/shelter-page
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
<<<<<<< HEAD
=======
    implementation("androidx.recyclerview:recyclerview:1.3.2")
>>>>>>> origin/shelter-page
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
<<<<<<< HEAD
    implementation(libs.play.services.ads)
=======
>>>>>>> origin/shelter-page
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}