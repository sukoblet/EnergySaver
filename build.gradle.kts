// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        // Android Gradle Plugin (AGP) для Android Studio Ladybug 2024.2.2
//        classpath("com.android.tools.build:gradle:8.1.0")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
//        classpath("com.google.gms:google-services:4.3.15")
//    }
//}
//
//allprojects {
//    repositories {
//        google()
//        mavenCentral()
//    }
//}