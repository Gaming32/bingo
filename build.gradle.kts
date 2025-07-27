import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.internal.minecraft.MinecraftProvider

plugins {
    java
    alias(libs.plugins.unimined)
    alias(libs.plugins.mod.publish.plugin)
}

operator fun Project.get(key: String) = properties[key] as? String ?: throw IllegalArgumentException("Missing property $key")

base.archivesName = rootProject["archives_base_name"]
group = rootProject["maven_group"]
version = rootProject["mod_version"]

sourceSets {
    main {
        resources {
            srcDir("src/main/generated")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    withSourcesJar()
}

val fabric: SourceSet by sourceSets.creating
val neoforge: SourceSet by sourceSets.creating

repositories {
    unimined.neoForgedMaven()
    unimined.spongeMaven()
    unimined.wagYourMaven("releases")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.blamejared.com/") {
        name = "Jared's maven"
    }
    maven("https://maven.terraformersmc.com/releases/") {
        name = "TerraformersMC"
    }
}

val modCompileOnly: Configuration by configurations.creating
configurations.compileOnly.get().extendsFrom(modCompileOnly)

unimined.minecraft {
    version = libs.versions.minecraft.get()

    mappings {
        mojmap()
        parchment(libs.versions.parchment.mcversion.get(), libs.versions.parchment.date.get())
        intermediary()

        devFallbackNamespace("official")
    }

    accessWidener {
        accessWidener(file("src/main/resources/bingo.accessWidener"))
    }

    if (sourceSet == sourceSets.main.get()) {
        mods {
            remap(modCompileOnly) {
                namespace("intermediary")
                catchAWNamespaceAssertion()
            }
        }
    }

    defaultRemapJar = false
}

unimined.minecraft(fabric) {
    combineWith(sourceSets.main.get())

    fabric {
        loader(libs.versions.fabric.loader.get())
        accessWidener(file("src/main/resources/bingo.accessWidener"))
    }

    mods {
        modImplementation {
            mixinRemap {
                reset()
                enableBaseMixin()
                enableMixinExtra()
            }
        }
    }

    // TODO: remove internal API usage when there is a way to inherit run configs
    @Suppress("UnstableApiUsage")
    (this as MinecraftProvider).provideRunClientTask("datagenClient", file("run/datagenClient"))

    runs {
        config("datagenClient") {
            description = "Data Generation"
            jvmArgs(
                "-Dfabric-api.datagen",
                "-Dfabric-api.datagen.output-dir=${file("src/main/generated").absolutePath}",
                "-Dfabric-api.datagen.modid=bingo"
            )
        }
    }

    defaultRemapJar = true
}

unimined.minecraft(neoforge) {
    combineWith(sourceSets.main.get())

    neoForge {
        loader(libs.versions.neoforge.get())
        accessTransformer(aw2at(file("src/main/resources/bingo.accessWidener")))
    }

    minecraftRemapper.config {
        ignoreConflicts(true)
    }

    defaultRemapJar = true
}

val fabricCompileOnly by configurations.getting
val fabricInclude by configurations.getting
val fabricImplementation by configurations.getting
val fabricModImplementation by configurations.getting

dependencies {
    implementation(libs.mixin)
    implementation(libs.mixinextras)
    libs.bundles.nightconfig.get().forEach {
        fabricInclude(fabricImplementation(implementation(it)!!)!!)
    }
    fabricModImplementation(fabricApi.fabric(libs.versions.fabric.api.get()))
    fabricModImplementation(libs.modmenu)
    modCompileOnly(libs.rei) {
        exclude(group = "dev.architectury")
    }
    modCompileOnly(libs.jei)
    modCompileOnly(libs.emi) // Unfortunately, although the API does what I need, it does in a way that's wholly different from the other recipe viewers
    fabricCompileOnly(compileOnly(libs.mcdev.annotations.get())!!)
}

tasks.getByName("jar") {
    enabled = false
}

val fabricSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("fabric-sources")
    from(sourceSets.main.get().allSource)
    from(fabric.allSource)
}

val neoforgeSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("neoforge-sources")
    from(sourceSets.main.get().allSource)
    from(neoforge.allSource)
}

tasks.getByName<ProcessResources>("processFabricResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.getByName<ProcessResources>("processNeoforgeResources") {
    inputs.property("version", project.version)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
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
        minecraftVersions.add(libs.versions.minecraft)
    }

    modrinth("modrinthFabric") {
        from(modrinthOpts)
        file.set(tasks.getByName<RemapJarTask>("remapFabricJar").asJar.archiveFile)
        additionalFiles.from(fabricSourcesJar)
        modLoaders.add("fabric")
        requires {
            slug.set("fabric-api")
        }
        optional {
            slug.set("modmenu")
        }
    }

    modrinth("modrinthNeoforge") {
        from(modrinthOpts)
        file.set(tasks.getByName<RemapJarTask>("remapNeoforgeJar").asJar.archiveFile)
        additionalFiles.from(neoforgeSourcesJar)
        modLoaders.add("neoforge")
    }
}
