operator fun Project.get(key: String) = properties[key] as String

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject["fabric_loader_version"]}")

    implementation("com.electronwill.night-config:toml:3.8.1")

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:18.0.800")
    modCompileOnly("mezz.jei:jei-1.21.1-common-api:19.21.2.313")
    modCompileOnly("dev.emi:emi-xplat-intermediary:1.1.16+1.21.1") // Unfortunately, although the API does what I need, it does in a way that's wholly different from the other recipe viewers
}

architectury {
    common("fabric", "neoforge")
}

loom {
    accessWidenerPath = file("src/main/resources/bingo.accessWidener")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/generated")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.WARN
}
