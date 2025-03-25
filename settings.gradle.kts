pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://repo.redlance.org/public/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
    }
}

include("common")
include("fabric")
include("neoforge")
