import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Use the Kotlin Safe Args plugin (pick ONE of the two lines below):
    alias(libs.plugins.navigation.safe.args)              // if you have it in libs.versions.toml
    // id("androidx.navigation.safeargs.kotlin") version "2.7.7"  // else use explicit id+version

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" // KSP for Room
}

secrets {
    // ⬅️ must be TOP-LEVEL (not inside android{})
    defaultPropertiesFileName = "local.properties"
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val mapsApiKey: String = localProps.getProperty("MAPS_API_KEY") ?: ""

android {
    namespace = "com.example.huntquest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.huntquest"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        // Directions key from local.properties or env
        val directionsKey: String =
            localProps.getProperty("DIRECTIONS_API_KEY")      // <-- use localProps
                ?: System.getenv("DIRECTIONS_API_KEY")        // fallback for CI
                ?: ""

        // expose as a resource (or use buildConfigField if you prefer)
        resValue("string", "directions_key", directionsKey)
        // buildConfigField("String", "DIRECTIONS_API_KEY", "\"$directionsKey\"")
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
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { viewBinding = true }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation("com.google.android.gms:play-services-location:21.3.0")

    //for autocomplete in address input fields
    implementation("com.google.android.libraries.places:places:3.5.0")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    //kapt(libs.androidx.room.compiler)

    ksp("androidx.room:room-compiler:2.6.1")

    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Coroutines Android (main thread dispatcher)
    implementation(libs.kotlinx.coroutines.android)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
