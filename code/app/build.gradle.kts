import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

val localProps = Properties()
val localPropsFile = File(rootDir, "local.properties")

val propsApiKey = if (localPropsFile.exists()) {
    try {
        localPropsFile.inputStream().use { stream ->
            localProps.load(stream)
        }
        localProps.getProperty("MAPS_API_KEY", "")
    } catch (e: Exception) {
        println("⚠️ Failed to load local.properties: ${e.message}")
        ""
    }
} else {
    ""
}

val envApiKey = System.getenv("MAPS_API_KEY")
val apiKey = envApiKey ?: propsApiKey

// Optional log
if (!envApiKey.isNullOrEmpty()) {
    println("✅ MAPS_API_KEY loaded from env")
} else if (propsApiKey.isNotEmpty()) {
    println("✅ MAPS_API_KEY loaded from local.properties")
} else {
    println("❌ MAPS_API_KEY not found!")
}

android {
    namespace = "com.example.team_16"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.team_16"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API key into BuildConfig
        buildConfigField("String", "MAPS_API_KEY", "\"${apiKey}\"")

        // fix for Manifest placeholders
        manifestPlaceholders["MAPS_API_KEY"] = apiKey
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.core)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    //test dependencies
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    // JUnit Test Dependencies
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    // Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:19.1.0")

    // Glide Dependencies
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    implementation("com.airbnb.android:lottie:6.1.0")


    // MPAndroidChart for mood visualizations
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    testImplementation("org.mockito:mockito-core:5.2.0")

}

