pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.6"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject){
        versions("1.21.4", "1.21.5")
        vcsVersion = "1.21.4"
    }

//    shared {
//
//    }
}

rootProject.name = "Bazaar Utils"