import com.bnorm.power.PowerAssertGradleExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.gitlab.arturbosch.detekt") version "1.22.0-RC3"
    id("com.google.devtools.ksp") version "1.7.20-1.0.8"
    id("org.jetbrains.dokka") version "1.7.20"
    id("org.dddjava.jig-gradle-plugin") version "2022.10.3"
    id("com.bnorm.power.kotlin-power-assert") version "0.12.0"
    id("com.github.ben-manes.versions") version "0.43.0"
}

group = "app.vercel.shiftup"
version = "0.0.1"
application {
    mainClass.set("app.vercel.shiftup.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

repositories {
    mavenCentral()
}

object Version {
    const val ktor = "2.1.3"
    const val logback = "1.4.4"
    const val ktorCsrf = "1.0.0"
    const val detekt = "1.22.0-RC3"
    const val kotest = "5.5.4"
    const val kotestAssertionsKtor = "1.0.3"
    const val archUnit = "1.0.0"
    const val mockK = "1.13.2"
    const val kmongo = "4.7.2"
    const val koin = "3.2.2"
    const val koinAnnotations = "1.0.3"
    const val kotlinResult = "1.1.16"
    const val kotlinxDatetime = "0.4.0"
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
    implementation("io.ktor:ktor-server-sessions:${Version.ktor}")
    implementation("io.ktor:ktor-server-resources:${Version.ktor}")
    implementation("io.ktor:ktor-client-core-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-client-cio:${Version.ktor}")
    implementation("io.ktor:ktor-client-auth:${Version.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${Version.ktor}")
    implementation("io.ktor:ktor-server-netty-jvm:${Version.ktor}")
    implementation("ch.qos.logback:logback-classic:${Version.logback}")
    implementation("org.mpierce.ktor.csrf:ktor-csrf:${Version.ktorCsrf}")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:${Version.kmongo}")
    implementation("io.insert-koin:koin-core:${Version.koin}")
    implementation("io.insert-koin:koin-ktor:${Version.koin}")
    implementation("io.insert-koin:koin-logger-slf4j:${Version.koin}")
    implementation("io.insert-koin:koin-annotations:${Version.koinAnnotations}")
    implementation("com.michael-bull.kotlin-result:kotlin-result:${Version.kotlinResult}")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:${Version.kotlinResult}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Version.kotlinxDatetime}")
    testImplementation("io.ktor:ktor-server-tests-jvm:${Version.ktor}")
    testImplementation("io.kotest:kotest-runner-junit5:${Version.kotest}")
    testImplementation("io.kotest:kotest-assertions-core:${Version.kotest}")
    testImplementation("io.kotest:kotest-property:${Version.kotest}")
    testImplementation("io.kotest.extensions:kotest-assertions-ktor:${Version.kotestAssertionsKtor}")
    testImplementation("com.tngtech.archunit:archunit:${Version.archUnit}")
    testImplementation("io.mockk:mockk:${Version.mockK}")
    testImplementation("io.insert-koin:koin-test:${Version.koin}")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Version.detekt}")
    ksp("io.insert-koin:koin-ksp-compiler:${Version.koinAnnotations}")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("config/detekt/detekt.yml")
}

jig {
    modelPattern = "^(?=.+\\.domain\\..+)(?!.*\\\$)(?!.*Kt$)(?!.*Serializer$).*\$"
    outputOmitPrefix = ".+\\.features\\."
    linkPrefix = "https://github.com/normhesu/shiftup-server/tree/develop/src/main/kotlin"
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(true)
        sarif.required.set(false)
        md.required.set(true)
    }
}
tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
}

configure<PowerAssertGradleExtension> {
    functions = listOf(
        "kotlin.assert",
        "kotlin.require",
        "kotlin.check",
    )
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    outputFormatter = "html"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}
