# Minecraft 1.21.1 build preparation

This branch starts with WIP commit `7de2e7f`. Java and data-pack migration remain deferred until the ongoing 1.20.1 fixes are merged.

## Toolchain

- Minecraft: 1.21.1
- NeoForge: 21.1.267
- ModDevGradle: 2.0.148
- Gradle wrapper: 8.8 (retained)
- JDK: 21; this machine has Ubuntu OpenJDK 21.0.13.1 at `/usr/lib/jvm/java-21-openjdk-amd64`.
- JDK 17 remains installed at `/usr/lib/jvm/java-17-openjdk-amd64`.

The [ModDevGradle README](https://github.com/neoforged/ModDevGradle#features) advertises Gradle 8.8 compatibility. Both configuration and full Minecraft artifact creation were successfully exercised with MDG 2.0.148 and the existing Gradle 8.8 wrapper, so no wrapper version upgrade was needed. The wrapper script now has executable permission on Linux.

## Verified commands

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew --no-daemon tasks
./gradlew --no-daemon createMinecraftArtifacts
```

Results:

```text
BUILD SUCCESSFUL in 24s
1 actionable task: 1 executed
```

```text
Total runtime: 73.12s

BUILD SUCCESSFUL in 1m 33s
1 actionable task: 1 executed
```

Artifact preparation resolved NeoForge and Minecraft dependencies, downloaded Minecraft 1.21.1, decompiled it, applied NeoForge patches, and generated compiled and source development artifacts. This does not compile ShinColle Java, migrate resources, or validate runtime behavior. No files under `src/modern` or `src/main` were changed in this preparation step.
