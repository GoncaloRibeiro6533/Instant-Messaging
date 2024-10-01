plugins {
    kotlin("jvm") version "1.9.25"
}

group = "pt.isel"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":repository"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}