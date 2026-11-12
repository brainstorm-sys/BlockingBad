
plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.4.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    implementation("com.github.retrooper:packetevents-spigot:2.14.0") {
        exclude(group = "net.kyori", module = "adventure-api")
        exclude(group = "net.kyori", module = "adventure-key")
        exclude(group = "net.kyori", module = "examination-api")
        exclude(group = "net.kyori", module = "examination-string")
    }
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("net.kyori:adventure-nbt:5.2.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate(
            "com.github.retrooper",
            "com.tesseractstudios.blockingbad.libs.packetevents"
        )

        relocate(
            "io.github.retrooper",
            "com.tesseractstudios.blockingbad.libs.packetevents"
        )

        relocate(
            "com.google.gson",
            "com.tesseractstudios.blockingbad.libs.gson"
        )
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)

        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
