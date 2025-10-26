// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup:javapoet:${libs.versions.javapoet.get()}")
    }
}


plugins {
    alias(libs.plugins.android.application) apply false
}