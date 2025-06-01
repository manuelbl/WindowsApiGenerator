import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.plugin-publish") version "1.2.1"

    alias(libs.plugins.kotlin.jvm)

    signing

    id("com.gradleup.shadow") version "8.3.6"
}

group = "net.codecrete.windows-api"
version = "0.8.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("net.codecrete.windows-api:code-generator:0.8.0")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest("2.1.0")
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest("2.1.0")

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project())
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) } 
                }
            }
        }
    }
}

gradlePlugin {
    plugins {
        website = "https://github.com/manuelbl/WindowsApiGenerator"
        vcsUrl = "https://github.com/manuelbl/WindowsApiGenerator.git"

        create("windowsApiGenerate") {
            id = "net.codecrete.windows-api"
            displayName = "Windows API Generator"
            description = """
                A plugin for generating Java code to the Windows APIs.
                
                Using metadata provided by Microsoft, the plugin generates function calls, struct/union
                layouts and accessors, callback function, enumerations, constants and code for COM interfaces.
                The generated code relies on the Java Foreign Function & Memory (FFM) API.
                """.trimIndent()
            tags = listOf("windows", "ffm", "generator")
            implementationClass = "net.codecrete.windowsapi.gradle.WindowsApiPlugin"
        }
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(testing.suites.named("functionalTest"))
}

tasks.shadowJar {
    archiveClassifier = ""
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../../gradle-plugin-repo")
        }
    }
}
