# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An IntelliJ plugin that contains different tools for working with .NET/C#. 
Built with Kotlin using the IntelliJ Platform SDK and JetBrains Jewel (Compose for JetBrains IDEs).

## Build Commands

### Compilation
```bash
# Compile all modules
./gradlew buildPlugin

# Compile specific module
./gradlew :core:compileKotlin
./gradlew :frontend:compileKotlin
```

### Running & Testing
```bash
# Run plugin in IDE sandbox
./gradlew runIde

# Run tests
./gradlew check
```

### Plugin Verification
```bash
# Verify plugin compatibility
./gradlew verifyPlugin
```

## Module Architecture

The plugin uses a **modular architecture** with a clear separation of concerns:

### Core Module (`core/`)
Backend business logic and services. Does NOT depend on the `frontend`.

### Frontend Module (`frontend/`)
UI layer using JetBrains Jewel Compose. Depends on the `core` module.

### Root Plugin
Integrates both modules via `plugin.xml`. Both modules are loaded as content modules.

## Technology Stack

- **Language:** Kotlin (JVM toolchain 21)
- **UI Framework:** JetBrains Jewel (Compose for JetBrains IDEs)
- **HTTP Client:** Ktor CIO engine
- **Serialization:** Kotlinx Serialization (JSON)
- **Concurrency:** Kotlin Coroutines with project-scoped lifecycles
- **File System:** EEL (Environment Execution Layer) for cross-platform support

## Configuration

Key settings in `gradle.properties`:
- `platformVersion = 2025.3` - Target IntelliJ Platform version
- `pluginSinceBuild = 253` - Minimum supported build
- JVM toolchain: 21
