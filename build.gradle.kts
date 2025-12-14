plugins {
    `maven-publish`
    kotlin("jvm") version "1.9.25"
}


group = "gg.levely"
version = "2.0.0"


repositories {
    mavenCentral()
}


dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    api(kotlin("reflect"))
    implementation("ch.qos.logback:logback-classic:1.3.15")
}


kotlin {
    jvmToolchain(8)
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${project.group}.system"
            artifactId = "eventbus"
            version = project.version.toString()

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY")}")

            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}