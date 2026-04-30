buildscript {
    dependencies {
        classpath("org.postgresql:postgresql:42.7.5")
        classpath("org.flywaydb:flyway-database-postgresql:11.14.1")
    }
}

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("io.ktor.plugin") version "3.1.3"
    id("com.github.node-gradle.node") version "7.1.0"
    id("org.flywaydb.flyway") version "11.14.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "com.timothymarias"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("com.timothymarias.familyarchive.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development") ||
        gradle.startParameter.taskNames.any { it.contains("run", ignoreCase = true) }
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.1.3"
val exposedVersion = "0.61.0"
val koinVersion = "4.1.1"

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")

    // Koin DI
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")

    // Database
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.3.0")

    // Flyway migrations
    implementation("org.flywaydb:flyway-core:11.14.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.14.1")

    // Kotlin HTML DSL for server-side rendering
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    // Storage and media processing
    implementation(platform("software.amazon.awssdk:bom:2.29.29"))
    implementation("software.amazon.awssdk:s3")
    implementation("net.coobird:thumbnailator:0.4.20")

    // GEDCOM parsing
    implementation("org.gedcom4j:gedcom4j:4.0.1")

    // JobRunr for background job processing
    implementation("org.jobrunr:jobrunr:8.3.1")

    // Password hashing
    implementation("at.favre.lib:bcrypt:0.10.2")

    // DotEnv for local development
    implementation("io.github.cdimascio:dotenv-java:3.0.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:localstack:1.19.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Node.js configuration for Vite frontend build
node {
    download = true
    version = "22.12.0"
    npmVersion = "10.9.2"
    workDir = file("${project.projectDir}/.gradle/nodejs")
    npmWorkDir = file("${project.projectDir}/.gradle/npm")
    nodeProjectDir = file("${project.projectDir}")
}

// Install npm dependencies
val npmInstall by tasks.getting(com.github.gradle.node.npm.task.NpmInstallTask::class)

// Force install of optional dependencies (fixes Rollup binary issue in Docker/Linux)
val installOptionalDeps by tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    dependsOn(npmInstall)

    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    val rollupPackage =
        when {
            osName.contains("linux") && osArch.contains("aarch64") -> "@rollup/rollup-linux-arm64-gnu"
            osName.contains("linux") && osArch.contains("amd64") -> "@rollup/rollup-linux-x64-gnu"
            osName.contains("linux") && osArch.contains("x86_64") -> "@rollup/rollup-linux-x64-gnu"
            else -> null
        }

    if (rollupPackage != null) {
        args.set(listOf("install", "--no-save", rollupPackage))
    }

    onlyIf { rollupPackage != null }
}

// Build frontend with Vite
val buildFrontend by tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    dependsOn(installOptionalDeps)
    args.set(listOf("run", "build"))
    inputs.dir("src/main/frontend")
    inputs.files("vite.config.ts", "tailwind.config.js", "postcss.config.js")
    outputs.dir("src/main/resources/static/dist")
}

// Ensure frontend is built before processing resources
tasks.named("processResources") {
    dependsOn(buildFrontend)
}

// Clean frontend build outputs
tasks.named("clean") {
    doLast {
        delete("src/main/resources/static/dist")
        delete(".gradle/nodejs")
        delete(".gradle/npm")
    }
}

// Flyway configuration for Gradle tasks
flyway {
    url = "jdbc:postgresql://localhost:5432/familyarchive"
    user = "familyarchive"
    password = "familyarchive"
    locations = arrayOf("classpath:db/migration")
}

// ktlint configuration
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.5.0")
    android.set(false)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
