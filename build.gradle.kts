plugins {
    kotlin("jvm") version "2.1.0"
    `java-library`
    id("com.gradleup.shadow") version "8.3.0"
}

group = "dev.arnagpal.dgenerator"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.unnamed.team/repository/unnamed-public/") }

    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // minimessage
    implementation("net.kyori:adventure-text-minimessage:4.24.0")

    // minestom
    implementation("net.minestom:minestom:2025.08.29-1.21.8")
    implementation("dev.oglass:kotstom:0.5.0-alpha.0")

    //lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // minestom libraries
    implementation("team.unnamed:hephaestus-api:0.6.0-SNAPSHOT")

    implementation("org.slf4j:slf4j-simple:2.0.17")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.arnagpal.dgenerator.DungeonGenerator" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}