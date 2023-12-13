plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

operator fun Project.get(key: String) = properties[key] as String

architectury {
    platformSetupLoomIde()
    neoForge()
}

val common by configurations.creating
val shadowCommon by configurations.creating
configurations {
    val developmentNeoForge by getting

    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    developmentNeoForge.extendsFrom(common)
}

dependencies {
    neoForge("net.neoforged:neoforge:${rootProject["neoforge_version"]}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-neoforge:${rootProject["architectury_version"]}")

    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }

    implementation(annotationProcessor("io.github.llamalad7:mixinextras-neoforge:0.3.1")!!)

    shadowCommon(implementation("com.electronwill.night-config:core:3.6.0")!!)
    shadowCommon(implementation("com.electronwill.night-config:toml:3.6.0")!!)

    // TODO: Verify dependencies
//    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-neoforge:12.0.652")
//    modRuntimeOnly("mezz.jei:jei-1.20.1-neoforge:15.2.0.27")
//    modRuntimeOnly("dev.emi:emi-neoforge:1.0.19+1.20.1")
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    exclude("fabric.mod.json", "bingo-common-refmap.json")

    configurations = listOf(shadowCommon)
    archiveClassifier = "dev-shadow"

    relocate("com.electronwill.nightconfig", "io.github.gaming32.bingo.shadow.nightconfig")
}

tasks.remapJar {
    inputFile = tasks.shadowJar.get().archiveFile
    dependsOn(tasks.shadowJar)
    archiveClassifier = null
    atAccessWideners.add("bingo.accessWidener")
}

tasks.jar {
    archiveClassifier = "dev"
}

tasks.sourcesJar {
    val commonSources by project(":common").tasks.sourcesJar
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}
