pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.2"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject){
        versions("1.21.8", "1.21.10")
        vcsVersion = "1.21.10"
    }
}

rootProject.name = "Bazaar-Utils-Modern"