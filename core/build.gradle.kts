import org.jetbrains.intellij.platform.gradle.extensions.excludeCoroutines

plugins {
    id("org.jetbrains.intellij.platform.module")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
            )
        )
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion")) {
            useCache = true
        }

        bundledModule("intellij.platform.backend")
    }

    implementation(libs.serializationJson)
    implementation(libs.ktorCio) {
        excludeCoroutines()
    }
    implementation(libs.ktorContentNegotiation) {
        excludeCoroutines()
    }
    implementation(libs.ktorJson) {
        excludeCoroutines()
    }
}