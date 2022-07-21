plugins {
    application
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "app.vercel.shiftup"
version = "0.0.1"
application {
    mainClass.set("app.vercel.shiftup.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

object Version {
    const val ktor = "2.0.3"
    const val kotlin = "1.7.10"
    const val logback = "1.2.3"
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-core-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-metrics-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-call-logging-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-cors-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-host-common-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-status-pages-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-auth-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-resources:${Version.ktor}")
    implementation("io.ktor:ktor-client-core-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-client-apache-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-server-netty-jvm:${Version.ktor}")
    implementation("ch.qos.logback:logback-classic:${Version.logback}")
    testImplementation("io.ktor:ktor-server-tests-jvm:${Version.ktor}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Version.kotlin}")
}
