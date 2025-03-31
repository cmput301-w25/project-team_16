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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.useJUnitPlatform()
                it.jvmArgs("-Dnet.bytebuddy.experimental=true")
                it.testLogging {
                    events("passed", "skipped", "failed")
                }
            }
        }
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
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")
    testImplementation("org.mockito:mockito-android:4.11.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.ext:truth:1.5.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("androidx.fragment:fragment-testing:1.6.2")
    testImplementation("androidx.viewpager2:viewpager2:1.0.0")
    testImplementation("com.google.android.material:material:1.12.0")
    testImplementation("androidx.appcompat:appcompat:1.7.0")

    // Android test dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("org.mockito:mockito-android:4.11.0")
    
    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:19.1.0")

    // Glide Dependencies
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")

    // MPAndroidChart for mood visualizations
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Dnet.bytebuddy.experimental=true")
    testLogging {
        events("passed", "skipped", "failed")
    }
}

