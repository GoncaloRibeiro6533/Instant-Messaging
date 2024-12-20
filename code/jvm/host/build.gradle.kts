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
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5434/db?user=dbuser&password=changeit")
    dependsOn(":repository-jdbi:dbTestsWait")
    finalizedBy(":repository-jdbi:dbTestsDown")
}

kotlin {
    jvmToolchain(21)
}

task<Copy>("extractUberJar") {
    dependsOn("assemble")
    // opens the JAR containing everything...
    from(zipTree(layout.buildDirectory.file("libs/host-$version.jar").get().toString()))
    // ... into the 'build/dependency' folder
    into("build/dependency")
}

val dockerImageJvm = "talkrooms-jvm"
val dockerImageNginx = "talkrooms-nginx"
val dockerImagePostgresTest = "talkrooms-postgres-test"

task<Exec>("buildImageJvm") {
    dependsOn("extractUberJar")
    commandLine("docker", "build", "-t", dockerImageJvm, "-f", "test-infra/Dockerfile-jvm", ".")
}

task<Exec>("buildImageNginx") {
    commandLine("docker", "build", "-t", dockerImageNginx, "-f", "test-infra/Dockerfile-nginx", ".")
}

task<Exec>("buildBundle") {
    workingDir("../../js")

    // Use the correct command depending on the operating system
    commandLine(
        if (System.getProperty("os.name").lowercase().contains("win")) "npm.cmd" else "npm",
        "run",
        "build",
    )
}

task<Copy>("copyBundle") {
    dependsOn("buildBundle")
    from("../../js/dist/bundle.js")
    into("./static-content")
    outputs.upToDateWhen { file("../../js/dist/bundle.js").lastModified() > file("./static-content/bundle.js").lastModified() }
}

task<Exec>("buildImagePostgresTest") {
    commandLine(
        "docker",
        "build",
        "-t",
        dockerImagePostgresTest,
        "-f",
        "test-infra/Dockerfile-postgres-test",
        "../repository-jdbi",
    )
}

/*

task<Exec>("buildImageUbuntu") {
    commandLine("docker", "build", "-t", dockerImageUbuntu, "-f", "test-infra/Dockerfile-jvm-postgres-test-ubuntu", ".")
}
*/

task("buildImageAll") {
    dependsOn("buildImageJvm")
    dependsOn("buildImageNginx")
    dependsOn("buildImagePostgresTest")
    dependsOn("copyBundle")
    // dependsOn("buildImageUbuntu")
}

task<Exec>("allUp") {
    commandLine("docker", "compose", "up", "--force-recreate", "-d")
}

task<Exec>("allDown") {
    commandLine("docker", "compose", "down")
}
