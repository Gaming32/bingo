import com.modrinth.minotaur.ModrinthExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    java
    id("architectury-plugin") version "3.4.159"
    id("dev.architectury.loom") version "1.7.416" apply false
    id("com.modrinth.minotaur") version "2.8.7" apply false
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
            parchment("org.parchmentmc.data:parchment-1.21.1:2024.11.17@zip")
        })

        compileOnly("com.demonwav.mcdev:annotations:2.1.0")
    }

    if (name != "common") {
        apply(plugin = "com.modrinth.minotaur")
        extensions.configure<ModrinthExtension>("modrinth") {
            token = if (rootProject.hasProperty("modrinthKey")) {
                rootProject["modrinthKey"]
            } else {
                System.getenv("MODRINTH_TOKEN")
            }
            projectId = "bingo-mod"
            val changelogFile = rootProject.file("changelogs/${rootProject.version}.md")
            if (changelogFile.isFile) {
                println("Setting changelog to file $changelogFile")
                changelog = changelogFile.readText()
            } else {
                println("Changelog file $changelogFile doesn't exist!")
            }
            gameVersions.add(rootProject["minecraft_version"])
        }
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
        options.release = 21
    }

    java {
        withSourcesJar()
    }
}
