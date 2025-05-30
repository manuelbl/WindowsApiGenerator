import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    id("net.codecrete.windows-api") version "0.8.0"

    // Java support
    java

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

tasks.generateWindowsApi {
    functions = listOf(
        "EnumWindows",
        "GetWindowTextW",
        "GetWindowInfo",
        "FormatMessageW",
        "GetModuleHandleW",
        "LocalFree"
    )
}

tasks.compileKotlin {
    dependsOn("generateWindowsApi")
}

application {
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    mainClass = "net.codecrete.windowsapi.examples.enumwindows.AppKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
