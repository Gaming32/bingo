import com.modrinth.minotaur.ModrinthExtension

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

apply(plugin = "com.modrinth.minotaur")

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

    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    exclude("*-refmap.json")

    configurations = listOf(shadowCommon)
    archiveClassifier = "dev-shadow"
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

extensions.configure<ModrinthExtension>("modrinth") {
    token.set(if (rootProject.hasProperty("modrinthKey")) rootProject["modrinthKey"] else System.getenv("MODRINTH_TOKEN"))
    projectId.set("bingo-mod")
    versionName.set("Bingo ${rootProject.version} for NeoForge")
    uploadFile.set(tasks.jar)
    gameVersions.add(rootProject["minecraft_version"])
    loaders.add("neoforge")
}
