import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.attributes.Attribute

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
        unitTests {
            isReturnDefaultValues = true
        }
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

    implementation("io.github.g00fy2.quickie:quickie-bundled:1.7.0")
    // Image loading library - Glide

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.android.material:material:1.12.0")


    implementation("com.google.code.gson:gson:2.10.1")
   // implementation(files("/Users/preciousajilore/Library/Android/sdk/platforms/android-36/android.jar"))

    //for location
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")

}

fun Javadoc.configureAndroidJavadocTask() {
    dependsOn("assembleDebug")

    // Java sources only (javadoc can’t parse Kotlin)
    val javaSrcDirs = android.sourceSets["main"].java.srcDirs.filter { it.exists() }
    setSource(javaSrcDirs)
    include("**/*.java")

    // Classpath pieces
    val bootCp = files(android.bootClasspath)
    val debugJavaCompile = tasks.named("compileDebugJavaWithJavac", JavaCompile::class.java).get()
    val compiledDebugOut = debugJavaCompile.destinationDirectory

    // Turn AARs on debug classpath into JARs for javadoc
    val jarView = configurations.getByName("debugCompileClasspath")
        .incoming
        .artifactView {
            attributes {
                attribute(Attribute.of("artifactType", String::class.java), "jar")
            }
            lenient(true)
        }
        .files

    val generatedDirs = files(
        "$buildDir/generated/source/r/debug",
        "$buildDir/generated/not_namespaced_r_class_sources/debug/r",
        "$buildDir/generated/source/buildConfig/debug",
        "$buildDir/intermediates/javac/debug/classes",
        "$buildDir/tmp/kapt3/classes/debug",
        "$buildDir/intermediates/compile_and_runtime_not_namespaced_r_class_jar/debug"
    )

    classpath = files(bootCp, jarView, debugJavaCompile.classpath, compiledDebugOut, generatedDirs)
    destinationDir = file("$buildDir/outputs/javadoc")

    (options as StandardJavadocDocletOptions).apply {
        setEncoding("UTF-8")
        setDocEncoding("UTF-8")
        setCharSet("UTF-8")
        setLocale("en")
        setAuthor(true)
        setVersion(true)
        setSplitIndex(true)
        addBooleanOption("Xdoclint:none", true)
        addBooleanOption("quiet", true)
        links("https://docs.oracle.com/en/java/javase/11/docs/api/")
        val sdk = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        if (sdk != null) {
            val offlineRef = file("$sdk/docs/reference")
            if (offlineRef.exists()) {
                linksOffline("https://developer.android.com/reference/", offlineRef.absolutePath)
            }
        }
    }

    isFailOnError = false
}

// Configure existing or register if missing
val existing = tasks.findByName("androidJavadocs")
if (existing is Javadoc) {
    existing.configureAndroidJavadocTask()
} else {
    tasks.register("androidJavadocs", Javadoc::class.java) {
        configureAndroidJavadocTask()
    }
}




