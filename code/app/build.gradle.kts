plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// Block to fix the protobuf duplicate class error
configurations.all {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}

android {
    namespace = "com.example.ajilore.code"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ajilore.code"
        minSdk = 24
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
    //stuff I added
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.espresso.intents)
    implementation(libs.fragment.testing)
    implementation(libs.espresso.contrib)
    //added
    androidTestUtil("androidx.test:orchestrator:1.4.2")
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.robolectric:robolectric:4.10")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.espresso.contrib)

    //added for testing

// For instrumentation tests in androidTest/ directory
    androidTestImplementation ("com.google.truth:truth:1.4.2")




    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.android.material:material:1.12.0")
    //implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.cloudinary:cloudinary-android:2.3.1")
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database:20.3.3")

    // Image loading library - Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.android.material:material:1.12.0")


    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.register<Javadoc>("javadoc") {
    // This sets the title for the generated documentation page
    options.windowTitle = "Quartz Events Javadoc"

    // This makes sure the task fails on any error
    isFailOnError = true

    // This specifies which source files to include
    source(android.sourceSets["main"].java.srcDirs)

    // This adds all the project dependencies (Android SDK, Firebase, etc.) to the classpath
    classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
    // FIX: Use getByName("compileClasspath") to access the configuration
    classpath += configurations.getByName("compileClasspath")
}
