plugins {
    kotlin("jvm") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "pt.isel"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":service"))
    // To use Spring MVC and the Servlet API
    implementation("org.springframework:spring-webmvc:6.1.13")
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // To use SLF4J
    implementation("org.slf4j:slf4j-api:2.0.16")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
