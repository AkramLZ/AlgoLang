plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

group = "com.akraml"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
}

tasks.withType<Jar> {
    manifest.attributes["Main-Class"] = "com.akraml.algo.AlgoMain"
}