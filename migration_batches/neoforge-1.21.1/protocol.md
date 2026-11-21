# NeoForge 1.21.1 version upgrade

- Base: origin/devin/parity-fix-integration (5b95cfe).
- Scope: build files, src/modern/java, src/modern/resources and migration documentation.
- Reference: fixed modern implementation on the base branch; src/main remains read-only.
- Goal: compileJava on JDK 21 with NeoForge 21.1.267 and ModDevGradle 2.0.148; adapt registry, events, network, components, capabilities, renderer and data APIs without changing gameplay or constants.
- Checks: compileJava, processResources, verifyItemModelCoverage and structural resource validation. Runtime and visual validation must be reported separately from compilation.
- Non-goals: new mechanics, parity backlog, legacy source changes, pushes.
