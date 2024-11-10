import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
}


android {
    namespace = "com.ml.dhenu.docqa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ml.dhenu.docqa"
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
        // See https://stackoverflow.com/a/60474096/13546426
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
        }
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
    applicationVariants.configureEach {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3.icons.extended)
    implementation(libs.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Apache POI
    implementation(libs.apache.poi)
    implementation(libs.apache.poi.ooxml)

    // Sentence Embeddings
    // https://github.com/shubham0204/Sentence-Embeddings-Android
    implementation("com.github.shubham0204:Sentence-Embeddings-Android:0.0.3")
    implementation("com.facebook.fbjni:fbjni:0.5.1")
    // iTextPDF - for parsing PDFs
    implementation("com.itextpdf:itextpdf:5.5.13.3")

    // ObjectBox - vector database
    debugImplementation("io.objectbox:objectbox-android-objectbrowser:4.0.0")
    releaseImplementation("io.objectbox:objectbox-android:4.0.0")

    // Gemini SDK - LLM
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")
    implementation(files("libs/executorch.aar"))

    //MIkit
    implementation ("com.google.mlkit:translate:17.0.3")

    // compose-markdown
    // https://github.com/jeziellago/compose-markdown
    implementation("com.github.jeziellago:compose-markdown:0.5.0")

    // Koin dependency injection
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.koin.androidx.compose)
    ksp(libs.koin.ksp.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("setup") {
    doFirst {
        exec {
            commandLine("sh", "examples/demo-apps/android/LlamaDemo/setup.sh")
            workingDir("../../../../../")
        }
    }
}

tasks.register("setupQnn") {
    doFirst {
        exec {
            commandLine("sh", "examples/demo-apps/android/LlamaDemo/setup-with-qnn.sh")
            workingDir("../../../../../")
        }
    }
}

tasks.register("download_prebuilt_lib") {
    doFirst {
        exec {
            commandLine("sh", "examples/demo-apps/android/LlamaDemo/download_prebuilt_lib.sh")
            workingDir("../../../../../")
        }
    }
}

apply(plugin = "io.objectbox")