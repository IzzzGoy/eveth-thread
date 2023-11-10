plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("convention.publication")
}

version = "0.0.1-dev04"
group = "ru.alexey.event.threads"

kotlin {
    applyDefaultHierarchyTemplate()
    
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":event-thread-core"))
                implementation("com.liftric:kvault:1.12.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.6.0")
            }
        }
    }
}

android {
    namespace = "ru.alexey.event.threads.cache"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
}
