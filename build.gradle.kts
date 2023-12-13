import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    java
    id("architectury-plugin") version "3.4.+"
    id("dev.architectury.loom") version "1.4.+" apply false
}

operator fun Project.get(key: String) = properties[key] as String

architectury {
    minecraft = rootProject["minecraft_version"]
}

version = rootProject["mod_version"]

subprojects {
    apply(plugin = "dev.architectury.loom")

    val loom = extensions.getByName("loom") as LoomGradleExtensionAPI
    dependencies {
        "minecraft"("com.mojang:minecraft:${rootProject["minecraft_version"]}")
        @Suppress("UnstableApiUsage")
        "mappings"(loom.layered {
            officialMojangMappings {
                nameSyntheticMembers = true
            }
            parchment("org.parchmentmc.data:parchment-1.20.2:2023.10.22@zip")
        })

        compileOnly("com.demonwav.mcdev:annotations:2.0.0")
    }

    version = "${rootProject.version}+$name"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")

    base.archivesName = rootProject["archives_base_name"]
    group = rootProject["maven_group"]

    repositories {
        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }
        maven("https://maven.shedaniel.me/")
        maven("https://maven.blamejared.com/") {
            name = "Jared's maven"
        }
        maven("https://maven.terraformersmc.com/releases/") {
            name = "TerraformersMC"
        }
        maven("https://maven.neoforged.net/releases/")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 17
    }

    java {
        withSourcesJar()
    }
}
