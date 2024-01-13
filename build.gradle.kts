plugins {
    id("java")
}

group = "com.akraml"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
}

tasks.withType<Jar> {
    manifest.attributes["Main-Class"] = "com.akraml.algo.AlgoMain"
}