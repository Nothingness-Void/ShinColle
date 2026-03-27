# ShinColle 1.20.1 Migration Order

This document orders the legacy 1.12.2 systems by dependency, not by package size.
The goal is to migrate the lowest-level runtime contracts first so later entity and GUI work can build on stable modern foundations.

## Dependency-First Order

| Phase | System | Main dependencies | Unblocks next | Current status | Notes |
| --- | --- | --- | --- | --- | --- |
| 0 | Build/toolchain/mod entry | none | everything | Done | ForgeGradle 6, Java 17, modern `@Mod` entrypoint, modern item/block/menu registries already exist. |
| 1 | Static registries and reusable assets | Phase 0 | sounds, block entities, recipes, entities | In progress | Keep all low-risk registries data-driven where possible: items, blocks, tabs, menus, sounds, tags, loot, recipes. |
| 2 | Sound events and feedback layer | Phase 1 | item UX, block entities, entities, combat | Started in this batch | Legacy code uses `ModSounds` broadly across items, tile entities, entities, combat, and reactions. A modern sound registry removes a major shared dependency early. |
| 3 | Block entity types and persistent block state | Phases 1-2 | crane, desk, shipyard, volcano core, waypoint | Not started | These blocks are already registered; the next useful step is replacing placeholder blocks with `BlockEntityType`-backed logic. Start with desk/waypoint before crane. |
| 4 | Menus/screens for block-backed UIs | Phases 1-3 | desk/crane/shipyard control flows | Partially done | Item-driven menus already exist (`recipepaper`, desk reference items). Block-backed menus should wait until their block entities exist. |
| 5 | Networking (`SimpleChannel`) | Phases 1-4 | synced block entities, synced entity state, reactions | Not started | Old `SimpleNetworkWrapper` packets are a central dependency for tiles, GUI sync, reactions, and particle dispatch. Port packet contracts only after menu/block-entity shape is clear. |
| 6 | Saved data, attachments, capabilities | Phases 1-5 | ownership, admiral data, ship inventories, persistent runtime state | Not started | Old capability/data code touches blocks, entities, ownership, and server lifecycle. Migrate after packet and block/entity storage shape is known. |
| 7 | Entity types, attributes, spawn eggs | Phases 1-6 | entity AI, renderers, combat, ship interaction | Not started | Spawn eggs are already placeholder items; real entity migration should begin only after sound, sync, and saved-state layers exist. |
| 8 | Client renderers and model glue | Phases 2-7 | visual parity for entities and complex blocks | Not started | Legacy client code depends heavily on entity classes, tile entities, and synced state, so it should follow those migrations instead of leading them. |
| 9 | AI, combat, reaction handlers | Phases 2-8 | gameplay parity | Not started | These systems sit on top of entity state, packets, sounds, attributes, and effects, so they are intentionally late. |
| 10 | Worldgen, advanced recipes, inter-mod support | Phases 1-9 | content completion and ecosystem compatibility | Not started | These are valuable, but they should not block the core gameplay rebuild. |

## Why This Order Works

- `client` depends on migrated entities, block entities, synced state, and resources, so it cannot lead the port.
- `entity` depends on sound, networking, attributes, saved data, and often custom inventories/capabilities.
- `tileentity` depends on blocks first, then persistence, then menus/screens, then packets.
- `handler` code in the legacy tree is not a single layer; it is mostly orchestration around packets, state sync, combat, particles, and sounds.

## Immediate Recommended Batches

1. Finish the modern sound registry and reusable sound helper layer.
2. Introduce `BlockEntityType` registration and port the lightest persistent block first.
3. Replace placeholder desk/waypoint interaction with modern block-backed logic.
4. Add a modern `SimpleChannel` once the first synced block entity needs real server/client state updates.

## Candidate Block Entity Order

| Priority | Target | Why first |
| --- | --- | --- |
| 1 | Desk | Small state surface, already has modern placeholder screens, minimal entity coupling. |
| 2 | Waypoint | Good ownership/persistence exercise, but still touches player/ship routing concepts. |
| 3 | Volcano Core / Small Shipyard | Useful progression blocks, but likely need custom flows and more content around them. |
| 4 | Crane | High-value but highest risk: inventory, fluids, redstone, paired chest, ship interaction, and packet sync. |
