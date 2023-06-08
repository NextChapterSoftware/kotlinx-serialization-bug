pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.21" apply false
        kotlin("plugin.serialization") version "1.8.21" apply false
    }

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
        maven { url = uri("https://repo1.maven.org/maven2") }
        maven { url = uri("${rootProject.projectDir}/.maven") }
    }
}

rootProject.name = "kotlinx-serialization-bug"

