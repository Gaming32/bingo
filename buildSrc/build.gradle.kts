plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    implementation("net.fabricmc:class-tweaker:0.3.0-beta.2")
}
