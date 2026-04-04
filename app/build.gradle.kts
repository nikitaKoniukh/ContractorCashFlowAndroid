plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.yetzira.ContractorCashFlowAndroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yetzira.ContractorCashFlowAndroid"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
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
        compose = true
        buildConfig = true
    }
    
    lint {
        disable.add("FlowOperatorInvokedInComposition")
        disable.add("PackageName")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.appcompat:appcompat:1.6.1")  // For AppCompatDelegate
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Lifecycle ViewModel Compose + Runtime Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Vico Charts
    implementation(libs.vico.compose.m3)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)

    // Google Play Billing
    implementation(libs.google.play.billing)
    implementation(libs.google.play.billing.ktx)
    implementation(libs.google.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // ML Kit Text Recognition
    implementation(libs.mlkit.text.recognition)


    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation("com.google.guava:guava:32.1.3-android")

    // Kotlinx Coroutines Play Services
    implementation(libs.kotlinx.coroutines.play.services)

    // Coil Compose
    implementation(libs.coil.compose)

    // Gson
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.matching { it.name in setOf("assembleDebug", "installDebug") }.configureEach {
    dependsOn("testDebugUnitTest")
}

// Match Finder-style duplicate suffixes like " 2", " 3", etc. before optional extension.
val duplicateBuildArtifactNameRegex = Regex(""".*\s\d+(\..+)?$""")
val duplicateBuildArtifactRiskPathMarkers = listOf(
    "intermediates/project_dex_archive",
    "intermediates/javac",
    "intermediates/runtime_app_classes_jar",
    "intermediates/compile_app_classes_jar",
    "intermediates/compile_and_runtime_not_namespaced_r_class_jar",
    "intermediates/dex"
)
val duplicateBuildArtifactRiskExtensions = setOf("jar", "class", "dex", "apk", "aar")

val verifyNoDuplicateBuildArtifacts by tasks.registering {
    group = "verification"
    description = "Removes Finder-style duplicate artifacts (e.g. '* 2.jar') from risky build outputs and fails only if cleanup cannot resolve them."

    doLast {
        val buildDirFile = layout.buildDirectory.asFile.get()
        if (!buildDirFile.exists()) return@doLast

        fun findRiskDuplicates() = buildDirFile
            .walkTopDown()
            .filter { file ->
                val relativePath = file.relativeTo(buildDirFile).invariantSeparatorsPath
                val inRiskPath = duplicateBuildArtifactRiskPathMarkers.any { marker ->
                    relativePath.contains(marker)
                }
                val isRiskFileType = file.isDirectory || file.extension.lowercase() in duplicateBuildArtifactRiskExtensions

                file.name.matches(duplicateBuildArtifactNameRegex) && inRiskPath && isRiskFileType
            }
            .toList()

        val duplicates = findRiskDuplicates()
        if (duplicates.isNotEmpty()) {
            duplicates.forEach { duplicate ->
                if (duplicate.isDirectory) {
                    duplicate.deleteRecursively()
                } else {
                    duplicate.delete()
                }
            }
            logger.warn("Removed ${duplicates.size} duplicate build artifacts from risky output paths.")
        }

        val remainingDuplicates = findRiskDuplicates()

        if (remainingDuplicates.isNotEmpty()) {
            val preview = remainingDuplicates
                .take(20)
                .joinToString(separator = "\n") { "- ${it.relativeTo(project.projectDir).path}" }
            val more = if (remainingDuplicates.size > 20) "\n...and ${remainingDuplicates.size - 20} more" else ""

            throw GradleException(
                "Duplicate generated artifacts were found in app/build.\n" +
                    "Automatic cleanup was attempted, but some duplicates could not be removed.\n" +
                    "Clean and rebuild: './gradlew --stop && rm -rf app/build && ./gradlew :app:assembleDebug'.\n\n" +
                    "Found:\n$preview$more"
            )
        }
    }
}

tasks.named("preBuild") {
    dependsOn(verifyNoDuplicateBuildArtifacts)
}

