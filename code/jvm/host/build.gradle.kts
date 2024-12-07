plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "pt.isel"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":http-api"))
    implementation(project(":repository-jdbi"))
    implementation(project(":http-pipeline"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // for JDBI and Postgres
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.postgresql:postgresql:42.7.2")

    // Suporte ao envio de emails (Spring Mail)
    implementation("org.springframework.boot:spring-boot-starter-mail:3.2.4")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.bootRun {
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres")
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres")
}

kotlin {
    jvmToolchain(21)
}
