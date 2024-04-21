plugins {
    kotlin("jvm") version "1.9.23"
}

group = "dev.mtib"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.sksamuel.scrimage:scrimage-core:4.1.1")
    testImplementation(kotlin("test"))
}
