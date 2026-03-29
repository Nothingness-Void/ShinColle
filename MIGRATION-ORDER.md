# ShinColle 1.20.1 Migration Order

This document orders the legacy 1.12.2 systems by dependency, not by package size.
The goal is to migrate the lowest-level runtime contracts first so later entity and GUI work can build on stable modern foundations.

## Dependency-First Order

| Phase | System | Main dependencies | Unblocks next | Current status | Notes |
| --- | --- | --- | --- | --- | --- |
| 0 | Build/toolchain/mod entry | none | everything | Done | ForgeGradle 6, Java 17, modern `@Mod` entrypoint, modern item/block/menu registries already exist. |
| 1 | Static registries and reusable assets | Phase 0 | sounds, block entities, recipes, entities | Partial | Item/block/menu/tab/sound registries are in place and runtime-usable, but resource/model coverage is still being normalized for full parity. |
| 2 | Sound events and feedback layer | Phase 1 | item UX, block entities, entities, combat | Partial | A modern sound registry and helper exist. Item and baseline block feedback are restored; full entity/combat sound parity is still pending. |
| 3 | Block entity types and persistent block state | Phases 1-2 | crane, desk, shipyard, volcano core, waypoint | Partial | `desk`, `waypoint`, `crane`, small shipyard, and now `volcore` / polymetal / heavy grudge all run on modern block entities. Large shipyard structure logic remains pending. |
| 4 | Menus/screens for block-backed UIs | Phases 1-3 | desk/crane/shipyard control flows | Partial | Recipe paper, desk references, desk terminal, waypoint terminal, crane terminal, small shipyard, and ship inventory GUI baselines are back. Heavy control pages are still pending. |
| 5 | Networking (`SimpleChannel`) | Phases 1-4 | synced block entities, synced entity state, reactions | Partial | A first `SimpleChannel` path exists for Teitoku data sync. Ship command packets, target-class packet flows, and broader gameplay packet families are still pending. |
| 6 | Saved data, attachments, capabilities | Phases 1-5 | ownership, admiral data, ship inventories, persistent runtime state | Partial | Owner data, ship runtime/inventory NBT, and the first Teitoku capability + server UID persistence are migrated. Team/formation and deeper persistent state are still pending. |
| 7 | Entity types, attributes, spawn eggs | Phases 1-6 | entity AI, renderers, combat, ship interaction | Partial | The generic legacy ship entity baseline, spawn deployment, and first restored combat/stat slices are active. Per-ship runtime and full legacy behavior trees are still pending. |
| 8 | Client renderers and model glue | Phases 2-7 | visual parity for entities and complex blocks | Partial | Generic ship renderer and major block model bridges are in place. Per-ship model/render parity and more BER coverage are still pending. |
| 9 | AI, combat, reaction handlers | Phases 2-8 | gameplay parity | Partial | Escort/assist targeting, legacy-style stat rebuild, and first melee/light/heavy/air routing are restored. Full projectile families and reaction/command pages are still pending. |
| 10 | Worldgen, advanced recipes, inter-mod support | Phases 1-9 | content completion and ecosystem compatibility | Not started | These are valuable, but they should not block the core gameplay rebuild. |

## Why This Order Works

- `client` depends on migrated entities, block entities, synced state, and resources, so it cannot lead the port.
- `entity` depends on sound, networking, attributes, saved data, and often custom inventories/capabilities.
- `tileentity` depends on blocks first, then persistence, then menus/screens, then packets.
- `handler` code in the legacy tree is not a single layer; it is mostly orchestration around packets, state sync, combat, particles, and sounds.

## Immediate Recommended Batches

1. Extend the Teitoku baseline into real team data and formation state (target-class editing/sync and first pointer command slices are now restored).
2. Expand the generic ship baseline into per-ship runtime hooks and fuller combat/reaction behavior.
3. Add dedicated ship GUI/command packet families on top of the current Teitoku sync channel.
4. Continue from heavy-block baseline into large shipyard structure flow and its packet/UI synchronization.

## Candidate Block Entity Order

| Priority | Target | Why first |
| --- | --- | --- |
| 1 | Large Shipyard Structure | Highest-value remaining block flow, and now the primary unfinished heavy block system after the core-block baseline landed. |
| 2 | Shipyard Automation Extensions | Reattach fuel/material logistics, transfer loops, and persistent control states that depend on large shipyard structure logic. |
| 3 | Shipyard Packet/UI Parity | Rebuild legacy packet-driven controls and client pages on top of the current `SimpleChannel` foundation. |
| 4 | Heavy Block Multiblock Rules | Expand from single-block state baselines to full multiblock behavior for polymetal/heavy grudge chains. |
