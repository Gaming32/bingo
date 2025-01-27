plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

apply(plugin = "com.modrinth.minotaur")

operator fun Project.get(key: String) = properties[key] as String

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
    runs {
        val datagenClient by creating {
            client()
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${project(":common").file("src/main/generated")}")
            vmArg("-Dfabric-api.datagen.modid=bingo")
        }

        val gametest by creating {
            server()
            name("GameTest")
            vmArg("-Dfabric-api.gametest")
            vmArg("-Dfabric-api.gametest.report-file=${project.layout.buildDirectory.get()}/junit.xml")
            runDir("build/gametest")
        }
    }
}

val common by configurations.creating
val shadowCommon by configurations.creating
configurations {
    val developmentFabric by getting

    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    testCompileClasspath.get().extendsFrom(common)
    testRuntimeClasspath.get().extendsFrom(common)
    developmentFabric.extendsFrom(common)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${rootProject["fabric_api_version"]}")

    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    include(implementation("com.electronwill.night-config:core:3.8.1")!!)
    include(implementation("com.electronwill.night-config:toml:3.8.1")!!)

    modImplementation("com.terraformersmc:modmenu:13.0.1")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    injectAccessWidener = true
    inputFile = tasks.shadowJar.get().archiveFile
    dependsOn(tasks.shadowJar)
    archiveClassifier = null
}

tasks.jar {
    archiveClassifier = "dev"
}

tasks.sourcesJar {
    val commonSources by project(":common").tasks.sourcesJar
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}

modrinth {
    versionName.set("Bingo ${rootProject.version} for Fabric")
    uploadFile.set(tasks.remapJar)
    additionalFiles.add(tasks.sourcesJar)
    loaders.add("fabric")
    dependencies {
        required.project("fabric-api")
        optional.project("modmenu")
    }
}
