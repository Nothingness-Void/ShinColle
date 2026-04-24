# ShinColle 1.20.1 Migration Order

This document orders the legacy 1.12.2 systems by dependency, not by package size.
The active status source is `MIGRATION-FLOW-CHECKLIST.md`; this file now mirrors the same verified-baseline closure state.

## Dependency-First Order

| Phase | System | Main dependencies | Unblocks next | Current status | Notes |
| --- | --- | --- | --- | --- | --- |
| 0 | Build/toolchain/mod entry | none | everything | Done | ForgeGradle 6, Java 17, modern `@Mod` entrypoint, and current source-set wiring are in place. |
| 1 | Static registries and reusable assets | Phase 0 | sounds, block entities, recipes, entities | Done | Item/block/menu/tab/sound registries are active; registered blockstates, models, item models, block loot tables, menu textures, and sound assets are GameTest-covered. |
| 2 | Sound events and feedback layer | Phase 1 | item UX, block entities, entities, combat | Done | Ship voice routing and combat sound events resolve through the modern sound registry/helper layer. |
| 3 | Block entity types and persistent block state | Phases 1-2 | crane, desk, shipyard, volcano core, waypoint | Done | Desk, waypoint, crane, small shipyard, VolCore, polymetal servants, and the 1.12 heavy-grudge multiblock large shipyard have modern persistence/control baselines. The modern-only standalone `blocklargeshipyard` ID is intentionally removed. |
| 4 | Menus/screens for block-backed UIs | Phases 1-3 | desk/crane/shipyard control flows | Done | Recipe paper, desk references, desk terminal, waypoint, crane, small shipyard, heavy-grudge large shipyard, legacy core, formation, morph, and ship inventory UI paths are wired. |
| 5 | Networking (`SimpleChannel`) | Phases 1-4 | synced block entities, synced entity state, reactions | Done (verified baseline) | Teitoku sync, gameplay state, command packets, combat/reaction/player-skill flows, and dedicated ship commands share tested modern packet paths. |
| 6 | Saved data, attachments, capabilities | Phases 1-5 | ownership, admiral data, ship inventories, persistent runtime state | Done (verified baseline) | Owner, Teitoku, team/formation, ship runtime, inventory, and online/offline/dead cache state persist and reload through tested modern storage. |
| 7 | Entity types, attributes, spawn eggs | Phases 1-6 | entity AI, renderers, combat, ship interaction | Done (verified baseline) | The modern ship adapter carries legacy egg/class variants, spawn eggs, owner gates, support items, recovered eggs, and catalog-routed behavior. |
| 8 | Client renderers and model glue | Phases 2-7 | visual parity for entities and complex blocks | Done (verified baseline) | Registered ship render catalog, legacy model-source parsing, textures, projectile visuals, and major block entity renderer baselines are covered. Large shipyard visuals are rendered from the formed `HeavyGrudgeBlockEntity`, matching the 1.12 TESR entry point. |
| 9 | AI, combat, reaction handlers | Phases 2-8 | gameplay parity | Done (verified baseline) | Escort/standby, command priority, targeting, stat rebuilds, resource gates, projectile profiles, high-value hooks, boss behavior, and loot metadata are tested. |
| 10 | Worldgen, advanced recipes, inter-mod support | Phases 1-9 | content completion and ecosystem compatibility | Done (verified baseline) | Single-player resource loops, recipes, chest loot, polymetal worldgen constants, Forge tags, and framework-only compat boundaries are documented and tested. |

## Current Follow-Up Work

- Manual client and multiplayer smoke remain outside the code-complete migration baseline: Pointer/ShipInventory commands, recovered egg owner gates, rider/morph input, combat FX, and ally/team authority should still be checked in real sessions.
- Exact legacy parity remains a review backlog for old GUI packet pages, rare per-ship behavior edges, model/layer fidelity, projectile visual edge cases, worldgen terrain behavior, and optional external inter-mod APIs.
- Block loot coverage is complete for normal drops. `blocklightair`, `blocklightliquid`, and special-NBT `blockgrudgeheavy` are explicit no-drop loot-table exceptions.
- Keep implementation work on the 1.20.1 modern side only: `src/modern/java`, `src/modern/resources`, and the related migration docs.
