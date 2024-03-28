plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

operator fun Project.get(key: String) = properties[key] as String

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
    runs {
        val client by getting
        val datagenClient by creating {
            inherit(client)
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${project(":common").file("src/main/generated")}")
            vmArg("-Dfabric-api.datagen.modid=bingo")
        }
    }
}

val common by configurations.creating
val shadowCommon by configurations.creating
configurations {
    val developmentFabric by getting

    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    developmentFabric.extendsFrom(common)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${rootProject["fabric_api_version"]}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-fabric:${rootProject["architectury_version"]}")

    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    include(implementation("com.electronwill.night-config:core:3.6.0")!!)
    include(implementation("com.electronwill.night-config:toml:3.6.0")!!)

//    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:14.0.680") {
//        exclude(module = "fabric-api")
//    }

//    modRuntimeOnly("mezz.jei:jei-1.20.1-fabric:15.2.0.27") {
//        exclude(module = "fabric-api")
//    }

//    modRuntimeOnly("dev.emi:emi-fabric:1.0.19+1.20.1") {
//        exclude(module = "fabric-api")
//    }
//    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api-deprecated:${rootProject.fabric_api_version}") // Required by EMI

    modImplementation("com.terraformersmc:modmenu:9.0.0-pre.1")
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
