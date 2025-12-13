# Copilot Onboarding

## Repository at a Glance
- **What it is:** Fabric client-side QOL mod for the Hypixel Bazaar (features like bookmarks, custom orders, stash helper, price charts). Primary code in Java; build logic in Kotlin DSL.
- **Stack/targets:** Java **21**, Gradle wrapper **8.14**, Fabric Loom + Stonecutter for multi-Minecraft support (versions defined in `settings.gradle.kts` / `stonecutter.gradle.kts`: active version set to **1.21.5** in `stonecutter.gradle.kts`; `settings.gradle.kts` also declares `vcsVersion = 1.21.6`). Dependencies include Fabric API, YACL, Owo, Hypixel API, Orbit events, Lombok.
- **Project size/layout:** Root contains `build.gradle.kts` (shared build), `gradle.properties` (mod metadata/versions), `stonecutter.gradle.kts` (active version), `settings.gradle.kts`, `buildSrc/` (ASM-based `BuildtimeInjectionTask`), `src/main/java/com/github/mkram17/bazaarutils` (config, features, mixins, UI, utils; main entry `BazaarUtils`), `src/main/resources` (fabric.mod.json, mixin config, assets), `versions/<mc>/gradle.properties` per target, docs (`README.MD` filename is uppercase, `CONTRIBUTING.md`, `UPDATES.MD`), `.github/workflows/build.yml`.
- **CI:** `.github/workflows/build.yml` on Ubuntu 22.04 with JDK 21 validates the wrapper, runs `./gradlew build`, and uploads artifacts from `versions/1.21.5/build/libs` and `versions/1.21.6/build/libs`.
- **Contribution note:** CONTRIBUTING asks for Java (no new Kotlin), PRs typically target `modern-dev` first; style mirrors SkyHanni guide.

## Build, Test, Run, Lint
- **Prereqs:** Java 21 on PATH, ~4 GB RAM available (see `org.gradle.jvmargs`), internet access to Fabric/Yarn/Modrinth/Curse maven repos. Repository expects execution bit on the wrapper.
- **Always first:** From repo root, run `chmod +x ./gradlew`.
- **Build:** `./gradlew build` (same as CI). On a clean environment this currently fails after ~1 minute while resolving plugin `fabric-loom:1.10-SNAPSHOT` (configured in `stonecutter.gradle.kts` with `apply false`), reporting the plugin is not found in gradle/plugin/fabric maven. If you have access to the Fabric snapshot repo or cached plugin, the command should proceed to compile, run `processInitAnnotations`, process resources, and produce jars under `versions/<mc>/build/libs`. Note the failure and check network/repo availability or adjust the Loom version only if project owners approve.
- **Tests/lint:** No dedicated test or lint tasks present; `build` is the primary validation.
- **Run client:** Loom run configs are enabled (`loom.runConfigs.all` with shared `run` dir). `./gradlew runClient` should work once the Loom plugin resolves; not validated here due to the plugin resolution failure.
- **Publishing:** `publishMods` exists but requires `MODRINTH_TOKEN`, `GITHUB_TOKEN`, `CURSEFORGE_TOKEN`; not needed for local dev.

## Project Layout Pointers
- **Key code:** `BazaarUtils` wires config load, event bus (Orbit), commands, keybinds. Packages: `config/BUConfig` (YACL config and widgets), `features/*` (custom orders, flip helper, stash helper, sell restrictions), `events/handlers` (BUListener, chat handlers), `misc` (compatibility, order info), `ui` (screens/buttons), `mixin` (Fabric mixins), `utils` (commands, resources, GUI helpers).
- **Build internals:** `buildSrc` ASM visitors inject `@RunOnInit` and `@RegisterWidget` calls post-compile via `processInitAnnotations`, which `classes` depends on automatically.
- **Versioning:** Stonecutter controls MC variants; `stonecutter.gradle.kts` sets the active version, and per-version dependency pins live in `versions/<mc>/gradle.properties` (1.21.5/1.21.6 emphasized in CI).
- **Configs:** `fabric.mod.json` declares entrypoints and dependency ranges (Minecraft >= `${mcVersion}` <= `${maxMcVersion}`, Fabric Loader >=0.16.9 while `gradle.properties` pins `deps.fabricLoaderVersion=0.17.2`, Java >=21).

## Working Efficiently
- Mirror CI steps (chmod +x → `./gradlew build`) to surface issues early; expect the current Loom plugin resolution error unless you have the plugin available.
- No tests exist—focus on targeted builds and manual verification of affected features.
- Trust these instructions; only search the tree when something here is missing or appears incorrect.
