import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("dev.rikka.tools.refine") version "4.4.0"
}

android {
    namespace = "com.anbs.applock"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.anbs.applock"
        minSdk = 26
        targetSdk = 37
        versionCode = 243
        versionName = "2.4.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    flavorDimensions.add("environment")

    productFlavors {
        create("rc") {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "NATIVE_LIB_NAME", "\"CC_dev\"")
            resValue("string", "app_name", "Dev ANBS Lock")
            buildConfigField("boolean", "LOGGING", "true")
            dimension = "environment"
        }
        create("prod") {
            buildConfigField("String", "NATIVE_LIB_NAME", "\"CC\"")
            resValue("string", "app_name", "ANBS Lock")
            dimension = "environment"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin.compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
        resValues = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

// Đổi tên file APK đầu ra cho bản release
androidComponents {
    onVariants { variant ->
        if (variant.buildType == "release") {
            val flavor = variant.flavorName ?: ""
            val vName = variant.outputs.firstOrNull()?.versionName?.get() ?: "unknown"
            val vCode = variant.outputs.firstOrNull()?.versionCode?.get() ?: 0
            val variantName = variant.name.replaceFirstChar { it.uppercase() }

            val renameTask = tasks.register<Copy>("rename${variantName}Apk") {
                val apkFolder = variant.artifacts.get(com.android.build.api.artifact.SingleArtifact.APK)
                from(apkFolder)
                include("**/*.apk")
                into(projectDir.resolve("$flavor/release"))
                eachFile {
                    if (name.endsWith(".apk")) {
                        name = "ANBSLock_${flavor}_v${vName}_${vCode}.apk"
                    }
                    path = name
                }
                includeEmptyDirs = false
            }

            tasks.matching { it.name == "create${variantName}ApkListingFileRedirect" }.configureEach {
                dependsOn(renameTask)
            }

            tasks.matching { it.name == "assemble$variantName" }.configureEach {
                finalizedBy(renameTask)
            }
        }
    }
}

dependencies {
    implementation(project(":appintro"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.refine.runtime)
    compileOnly(project(":hidden-api"))
    implementation(libs.hiddenapibypass)
    implementation(project(":patternlock"))



    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
