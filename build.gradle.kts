plugins {
    id("fabric-loom") version Versions.loom
    id("maven-publish")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

base {
    archivesName.set(ModData.id)
}

group = ModData.group
version = ModData.version

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.minecraft}")
    mappings("net.fabricmc:yarn:${Versions.yarn}:v2")
    modImplementation("net.fabricmc:fabric-loader:${Versions.fabricLoader}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.fabricApi}")
}

@Suppress("UnstableApiUsage")
tasks {
    withType<ProcessResources> {
        inputs.property("version", ModData.version)
        filesMatching("fabric.mod.json") {
            expand("version" to ModData.version)
        }
    }
    withType<JavaCompile> {
        configureEach {
            options.release.set(17)
        }
    }

    // Publish to GitHub Releases
    register<Exec>("publishToGithub") {
        group = "publishing"
        description = "Publishes the current version to GitHub Releases"
        workingDir = projectDir
        dependsOn("build")

        val jarName = "${ModData.id}-${ModData.version}"
        val changelog = with(file("changelogs/${ModData.version}.md")) {
            if (exists()) readText() else ""
        }

        commandLine(
            "gh", "release",
            "create", ModData.version,
            "build/libs/${jarName}.jar",
            "build/libs/${jarName}-javadoc.jar",
            "build/libs/${jarName}-sources.jar",
            "-t", "Runtime Datagen ${ModData.versionType} ${ModData.version}",
            "-n", changelog,
        )
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = ModData.group
            artifactId = ModData.id
            version = ModData.version
        }
    }

    repositories {
        maven {
            url = uri("https://repo.repsy.io/mvn/mim1q/mods/")
            credentials {
                username = properties["repsyUsername"] as? String
                password = properties["repsyPassword"] as? String
            }
        }
    }
}