import org.jetbrains.intellij.platform.gradle.extensions.excludeCoroutines

plugins {
    id("org.jetbrains.intellij.platform.module")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeCompiler)
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
        intellijIdeaCommunity(providers.gradleProperty("platformVersion")) {
            useCache = true
        }

        @Suppress("UnstableApiUsage")
        composeUI()
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