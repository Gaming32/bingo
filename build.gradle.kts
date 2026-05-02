import io.github.gaming32.bingo.buildscript.ConvertClassTweakerTask

plugins {
    java
    checkstyle
    alias(libs.plugins.mod.dev.gradle)
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

val convertClassTweaker by tasks.registering(ConvertClassTweakerTask::class) {
    description = "Converts the class tweaker to an access transformer etc"
    classTweakerFile.set(file("src/main/resources/bingo.accessWidener"))
    accessTransformerFile.set(layout.buildDirectory.file("classTweaker/accesstransformer.cfg"))
}
afterEvaluate {
    tasks.named("createMinecraftArtifacts") {
        dependsOn(convertClassTweaker)
    }
}

neoForge {
    version = libs.versions.neoforge.get()

    accessTransformers.from(convertClassTweaker.flatMap { it.accessTransformerFile })

    runs {
        create("client") {
            client()
        }
        create("server") {
            server()
        }

        mods {
            create("bingo") {
                sourceSet(sourceSets.main.get())
            }
        }
    }

    unitTest {
        enable()
        testedMod.set(mods["bingo"])
    }
}

dependencies {
    libs.bundles.nightconfig.get().forEach {
        jarJar(implementation(it)!!)
    }
    compileOnly(libs.jei)
    compileOnly(libs.mcdev.annotations.get())

    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    from("LICENSE")
    from(convertClassTweaker.flatMap { it.accessTransformerFile }) {
        rename { "META-INF/accesstransformer.cfg" }
    }
}

val processResourcesValues = mapOf(
    "version" to project.version,
    "min_minecraft_version" to libs.versions.minecraft.min.get(),
    "max_minecraft_version" to libs.versions.minecraft.max.get(),
)

val generateModMetadata by tasks.registering(ProcessResources::class) {
    inputs.properties(processResourcesValues)
    expand(processResourcesValues)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main.get().resources.srcDir(generateModMetadata)
neoForge.ideSyncTask(generateModMetadata)

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

    modrinth("modrinthNeoforge") {
        from(modrinthOpts)
        version.set("${project.version}+neoforge")
        displayName.set("Bingo ${project.version} for NeoForge")
        file.set(tasks.getByName<Jar>("jar").archiveFile)
        additionalFiles.from(tasks.getByName("sourcesJar"))
        modLoaders.add("neoforge")
    }
}
