
plugins {
    java
    checkstyle
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.mod.publish.plugin)
}

operator fun Project.get(key: String) = properties[key] as? String ?: throw IllegalArgumentException("Missing property $key")

base.archivesName = rootProject["archives_base_name"]
group = rootProject["maven_group"]
version = rootProject["mod_version"]

sourceSets {
    main {
        java {
            srcDir("src/datagen/java")
        }
        resources {
            srcDir("src/datagen/resources")
            srcDir("src/main/generated")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }

    withSourcesJar()
}

repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://maven.blamejared.com/") {
        name = "Jared's maven"
    }
    maven("https://maven.terraformersmc.com/releases/") {
        name = "TerraformersMC"
    }
}

loom {
    accessWidenerPath = file("src/main/resources/bingo.accessWidener")
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

dependencies {
    minecraft(libs.minecraft)
    libs.bundles.nightconfig.get().forEach {
        include(implementation(it)!!)
    }
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.modmenu) {
        isTransitive = false
    }
    compileOnly(libs.jei)
    compileOnly(libs.mcdev.annotations.get())

    testImplementation(libs.fabric.loader.junit)
    testImplementation(libs.junit)
}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    from("LICENSE")
}

val processResourcesValues = mapOf(
    "version" to project.version,
    "min_minecraft_version" to libs.versions.minecraft.min.get(),
    "max_minecraft_version" to libs.versions.minecraft.max.get(),
)

tasks.getByName<ProcessResources>("processResources") {
    inputs.properties(processResourcesValues)

    duplicatesStrategy = DuplicatesStrategy.WARN

    filesMatching("fabric.mod.json") {
        expand(processResourcesValues)
    }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configFile = rootProject.file("checkstyle.xml")
    isIgnoreFailures = false
}

publishMods {
    val changelogFile = file("changelogs/${project.version}.md")
    if (changelogFile.isFile) {
        println("Setting changelog to file $changelogFile")
        changelog.set(changelogFile.readText())
    } else {
        println("Changelog file $changelogFile doesn't exist!")
        changelog.set("")
    }

    type = STABLE

    val modrinthOpts = modrinthOptions {
        accessToken.set(providers.gradleProperty("modrinthKey").orElse(providers.environmentVariable("MODRINTH_TOKEN")))
        projectId.set("tXdFVOz6")
        minecraftVersions.add(libs.versions.minecraft.exact)
    }

    modrinth("modrinthFabric") {
        from(modrinthOpts)
        version.set("${project.version}+fabric")
        displayName.set("Bingo ${project.version} for Fabric")
        file.set(tasks.getByName<Jar>("jar").archiveFile)
        additionalFiles.from(tasks.getByName("sourcesJar"))
        modLoaders.add("fabric")
        requires {
            slug.set("fabric-api")
        }
        optional {
            slug.set("modmenu")
        }
    }
}
