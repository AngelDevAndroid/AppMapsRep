plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.appmaps"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appmaps"
        minSdk = 23
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    //implementation (platform(libs.firebase.bom))
    implementation (platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //implementation (platform(libs.firebase.bom.v33100))

    //implementation (libs.firebase.firestore.ktx)
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-auth-ktx")


    // Maps
    //implementation (libs.maps.ktx)
    //implementation (libs.maps.utils.ktx)
    //implementation (libs.android.maps.utils)

    implementation ("com.google.maps.android:maps-ktx:3.2.0")
    implementation ("com.google.maps.android:maps-utils-ktx:3.2.0")
    implementation ("com.google.maps.android:android-maps-utils:2.2.3")

    //implementation (libs.play.services.maps)
    //implementation (libs.play.services.location)

    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation ("com.google.android.gms:play-services-location:20.0.0")

    implementation ("com.google.android.libraries.places:places:3.3.0")

    //implementation (libs.github.easywaylocation)
    //implementation (libs.github.geofirestore.android)
    implementation ("com.github.prabhat1707:EasyWayLocation:2.4")

    implementation ("com.google.firebase:firebase-firestore:24.6.1")
    implementation ("com.github.imperiumlabs:GeoFirestore-Android:v1.5.0")

    // Anim lotti
    implementation (libs.lottie)

    //implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /*implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //implementation (platform(libs.firebase.bom))
    implementation (platform(libs.firebase.bom.v33100))
    implementation (libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth)

    // Maps
    implementation (libs.maps.ktx)
    implementation (libs.maps.utils.ktx)

    implementation ("com.google.maps.android:android-maps-utils:2.2.3")
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation ("com.github.prabhat1707:EasyWayLocation:2.4")


    //implementation (libs.google.play.services)

    // Places
    implementation (libs.android.places.ktx)


    // -->
    //implementation (libs.github.easywaylocation)

    implementation (libs.github.geofirestore.android)

    //implementation ("com.google.maps.android:places-ktx:20.0.0") // ->
    //implementation ("com.android.volley:volley:1.2.1") // ->

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)*/
}