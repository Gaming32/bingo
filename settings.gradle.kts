pluginManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.neoforged.net/releases")
        }
        maven {
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            url = uri("https://maven.wagyourtail.xyz/releases")
        }
        maven {
            url = uri("https://maven.wagyourtail.xyz/snapshots")
        }
        gradlePluginPortal {
            content {
                excludeGroup("org.apache.logging.log4j")
            }
        }
    }
}

rootProject.name = "bingo"
