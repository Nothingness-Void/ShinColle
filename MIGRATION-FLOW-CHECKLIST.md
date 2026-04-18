# ShinColle 1.20.1 Migration Flow Checklist

For the newest implementation handoff, read `AI-HANDOFF.md`.

Audit date: 2026-04-18

This file tracks the port in dependency order and checks what is genuinely migrated today.
It is stricter than `PORTING-1.20.1.md`: code that is only registered as a placeholder is not marked as fully migrated.
Before continuing entity, resource, or renderer work, re-read `PORTING-MISTAKES.md`.

## Self-check Rules

| Check item | Meaning |
| --- | --- |
| `Code` | Modern code exists under `src/modern/java` and is wired into the current registries. |
| `Compile` | `./gradlew compileJava processResources` passes with Java 17. |
| `Runtime` | Manually smoke-tested in a dev client. |
| `Partial` | Usable baseline exists, but a legacy subsystem is still missing. |

## Phase Flow

| Phase | System | Dependency | Current status | Self-check | Notes |
| --- | --- | --- | --- | --- | --- |
| 0 | Build, toolchain, mod entry | none | Done | `Code`, `Compile`, `Runtime` | ForgeGradle 6, Java 17, modern `@Mod` bootstrap, client can boot. |
| 1 | Core registries and static assets | Phase 0 | Done | `Code`, `Compile` | Modern item/block/menu/tab/sound/block-entity registries exist and GameTest now locks registered blockstates, model references, item models, menu GUI textures, and `sounds.json` packaged OGG references. Client visual smoke remains separate. |
| 2 | Sound layer | Phase 1 | Done | `Code`, `Compile` | Modern sound registry and helper are in place. Ship idle/hurt/death voice routing plus light/heavy/air/melee combat sound events resolve, and friendly/hostile ships use the expected sound sources. |
| 3 | Persistent block entities | Phases 1-2 | Done | `Code`, `Compile` | `desk`, `waypoint`, `crane`, small/large shipyard, and `volcore` / `polymetal` / `heavy grudge` run on modern block entities. Route transfer is ship-arrival driven, waypoint waits are ship-local, and large shipyard structure/fuel/energy/output is GameTest-covered. |
| 4 | Menus and screens | Phases 1-3 | Done | `Code`, `Compile` | `recipepaper`, desk reference items, desk terminal, crane terminal, waypoint terminal, small/large shipyard, legacy core, and ship inventory menus/screens have a Phase 4 server-button/readout baseline. |
| 5 | Networking (`SimpleChannel`) | Phases 1-4 | Partial | `Code`, `Compile` | `SimpleChannel` now carries Teitoku sync, gameplay command/state sync, combat FX, and a dedicated `ServerboundShipCommandPacket` family for single-player ship commands. Old gameplay ship commands and the new typed packet share `ShipCommandService`, command writes now refresh `ShipCacheSavedData` for detached UI fallback, and `ShipInventory` mode/AI controls no longer depend on a separate menu-only mutation path. Gameplay-state payload application is now GameTest-covered for team/world-rule/offline-ship-cache/player-skill client mirrors. Deeper legacy GUI packet families and multiplayer ally UI are still pending. |
| 6 | Saved data, attachments, capabilities | Phases 1-5 | Partial | `Code`, `Compile` | Teitoku/team/formation data plus ship runtime command state now persist the single-player command loop: AI flags, follow range, route energy/wait, command pos, guard target, current team slots, selected slots, formation id, ship UID lookup, and ship cache `online / offline / dead` lifecycle are GameTest-covered. Broader old `CapaTeitoku`, migration edge cases, and deeper ship task/combat state are still pending. |
| 7 | Entity types and spawn logic | Phases 1-6 | Partial | `Code`, `Compile` | A modern generic ship entity and real spawn-egg deployment path now exist. `LegacyShipBehaviorCatalog` owns the first per-ship dispatch layer for attack profile routing, marriage/ring passives, and hostile/elite/boss spawn level/morale baselines. Hostile boss spawn runtime and route/guard command boundaries now have GameTest coverage; deeper per-ship combat hooks and spawn depth are still pending. |
| 8 | Client renderers and visual glue | Phases 2-7 | Partial | `Code`, `Compile`, `Runtime` | Basic item property/render-layer hooks exist, several blocks use custom JSON models, and the first generic ship renderer can now draw legacy ship textures. No BER or per-ship renderer migration yet. |
| 9 | AI, combat, reactions | Phases 2-8 | Partial | `Code`, `Compile` | Ships now have escort / standby behavior, owner assist targeting, hostile-vs-friendly combat targeting, restored legacy stat rebuilding, legacy-style melee / light / heavy / air attack routing, and a first compatibility projectile layer for heavy / air attacks. Full projectile systems, per-ship weapon specials, and reaction pages are still missing. |
| 10 | Worldgen, advanced recipes, inter-mod | Phases 1-9 | Not started | none | Only basic resource/data reuse exists today. |

## Migrated Systems Checklist

| Area | Scope | Status | Self-check | Main entry points | Notes |
| --- | --- | --- | --- | --- | --- |
| Mod bootstrap | Entry, event bus, client setup | Done | `Code`, `Compile`, `Runtime` | `ShinColle.java` | Current root for all modern registrations and client hooks. |
| Creative tab | Modern creative tab | Done | `Code`, `Compile`, `Runtime` | `registry/ModCreativeModeTabs.java` | Usable in-game. |
| Item registry coverage | Legacy item IDs registered | Done | `Code`, `Compile` | `registry/ModItems.java` | Utility items, equipment lines, spawn eggs, and block items all exist in the modern registry, with item-model coverage locked by GameTest. Remaining gaps are deeper gameplay parity, not missing item IDs. |
| Block registry coverage | Legacy block IDs registered | Done | `Code`, `Compile` | `registry/ModBlocks.java` | All core IDs exist and registered blockstate/model/item-model resources are GameTest-covered. Gameplay depth still varies by block family. |
| Sound registry | Legacy voice/event bridge | Done | `Code`, `Compile` | `registry/ModSoundEvents.java`, `sound/ShinColleSoundHelper.java`, `entity/ship/LegacyShipEntity.java` | Current Phase 2 sound layer is wired: ship idle/hurt/death voices resolve, combat fire/light/heavy/air/melee events resolve, and friendly/hostile sound sources are split. |
| Recipe Paper | Item NBT + menu + screen | Done | `Code`, `Compile`, `Runtime` | `item/RecipePaperItem.java`, `menu/RecipePaperMenu.java`, `client/screen/RecipePaperScreen.java` | First complete modern item GUI chain. |
| Desk reference items | Portable book/radar placeholder UIs | Done | `Code`, `Compile`, `Runtime` | `item/DeskReferenceItem.java`, `menu/DeskReferenceMenu.java`, `client/screen/DeskReferenceScreen.java` | Keeps item-side GUI path alive. |
| Desk block | Block entity + block menu + persisted mode | Done | `Code`, `Compile`, `Runtime` | `block/DeskBlock.java`, `blockentity/DeskBlockEntity.java`, `menu/DeskTerminalMenu.java`, `client/screen/DeskTerminalScreen.java` | User-tested in dev client; model/orientation has also been corrected. |
| Waypoint block | Owner/stay/route/container persistence + terminal UI | Done | `Code`, `Compile` | `block/WaypointBlock.java`, `blockentity/WaypointBlockEntity.java`, `menu/WaypointTerminalMenu.java`, `client/screen/WaypointTerminalScreen.java`, `entity/ship/LegacyShipEntity.java` | Core routing data and block-side controls are modernized. Ship route handling now waits per ship and advances to the next route node without storing shared countdown state in the waypoint BE. |
| Crane block | Owner/route/container persistence + filter terminal | Done | `Code`, `Compile` | `block/CraneBlock.java`, `blockentity/CraneBlockEntity.java`, `menu/CraneTerminalMenu.java`, `client/screen/CraneTerminalScreen.java`, `entity/ship/LegacyShipEntity.java` | Crane stores configuration/filter/route state only. Item, fluid, and energy transfers execute when a ship reaches the crane route node, avoiding targetless chest-drain ticks. |
| Small shipyard | Material slots + fuel + ship/equip build loop + legacy build egg NBT | Done | `Code`, `Compile` | `block/SmallShipyardBlock.java`, `blockentity/SmallShipyardBlockEntity.java`, `menu/SmallShipyardMenu.java`, `client/screen/SmallShipyardScreen.java`, `crafting/SmallShipyardRecipes.java` | The small hydrothermal vent restores the old `4 materials + 1 fuel + 1 output` flow, including loop modes, instant construction support, and shipyard-built eggs carrying legacy material data. |
| Large shipyard | Multiblock structure + fuel/energy pool + large ship output | Done | `Code`, `Compile` | `block/LargeShipyardBlock.java`, `blockentity/LargeShipyardBlockEntity.java`, `blockentity/LargeShipyardStructureHelper.java`, `menu/LargeShipyardMenu.java`, `client/screen/LargeShipyardScreen.java`, `crafting/LargeShipyardRecipes.java` | Complete ring recognition, servant material proxying, fuel/route-energy storage, build-type buttons, material consumption, and large ship egg output are covered by GameTest. |
| Ownership foundation | Shared UUID/name/UID owner model for modern persistent content | Partial | `Code`, `Compile` | `ownership/PlayerOwnerData.java`, `blockentity/WaypointBlockEntity.java`, `blockentity/CraneBlockEntity.java`, `blockentity/SmallShipyardBlockEntity.java`, `entity/ship/LegacyShipEntity.java` | Owner-tagged blocks and ships now share one owner serialization/edit-check path instead of each system carrying custom code. The model has been expanded from `UUID + name` to `UUID + name + playerUID` so later team/formation systems can keep following the old design. |
| Admiral capability baseline | Old `CapaTeitoku` field slice + server UID registry + sync packet | Partial | `Code`, `Compile` | `teitoku/TeitokuData.java`, `teitoku/TeitokuSavedData.java`, `teitoku/TeitokuEvents.java`, `teitoku/TeitokuHelper.java`, `network/ModNetwork.java` | The first old-version-driven admiral data layer is back: player name/UID, ring state, marriage count, boss/team cooldowns, collected ship/equipment lists, target-class storage, current team, team slots, slot selection, formation id, and client sync on login/respawn/dimension change. Target-class editing UI and the old GUI packet families are still missing. |
| Block models | Desk/crane/waypoint custom silhouettes | Partial | `Code`, `Compile`, `Runtime` | `assets/shincolle/models/block/blockdesk.json`, `assets/shincolle/models/block/blockcrane.json`, `assets/shincolle/models/block/blockwaypoint.json` | Desk has been smoke-tested already; crane and waypoint custom models are now in place but not yet manually rechecked in client. |
| Target Wrench | Selected-target NBT + route-node pairing | Partial | `Code`, `Compile` | `item/TargetWrenchItem.java` | Works server-side for waypoint/crane route-node pairing and container links, but old packet-driven extensions are not back. |
| Waypoint item placement | In-air targeted placement flow | Partial | `Code`, `Compile` | `item/WaypointBlockItem.java` | Restores old placement style for waypoint blocks. |
| Ship tank | Fluid capability container | Partial | `Code`, `Compile` | `item/ShipTankItem.java` | Basic fill/place loop works; deeper ship logistics remain pending. |
| Pointer item | Command-scepter mode handling | Partial | `Code`, `Compile` | `item/PointerItem.java`, `network/ServerboundShipCommandPacket.java`, `network/ShipCommandService.java`, `teitoku/TeitokuHelper.java` | Ship move, guard, attack, stop, sit toggle, and open-inventory commands now use the dedicated ship command packet while desk/formation/morph/target-class flows stay on the existing gameplay command bus. Advanced route/target packet families are still pending. |
| Owner paper | Dual-signature item NBT | Partial | `Code`, `Compile` | `item/OwnerPaperItem.java` | Self-contained item logic restored. |
| Marriage ring | Toggle state + foil state + admiral ring sync | Partial | `Code`, `Compile` | `item/MarriageRingItem.java`, `teitoku/TeitokuHelper.java` | Item UX is restored and now writes back into the Teitoku capability (`hasRing`, active/flying state). Full old marriage bonus chains are still absent. |
| Kaitai hammer | Reusable crafting tool behavior | Partial | `Code`, `Compile` | `item/KaitaiHammerItem.java` | Tool durability path exists; legacy dismantle system is still not back. |
| Combat ration | Food/item-side morale baseline | Partial | `Code`, `Compile` | `item/CombatRationItem.java` | Standalone consumption works; ship-specific legacy hooks remain missing. |
| Repair goddess | Simple rescue item | Partial | `Code`, `Compile` | `item/RepairGoddessItem.java` | Minimal standalone behavior only. |
| Legacy equipment lines | Equipment stats, ordering, tooltip data, and models | Done | `Code`, `Compile` | `item/equipment/LegacyEquipmentItem.java`, `item/equipment/LegacyEquipmentStatsRepository.java`, `registry/ModItems.java` | All legacy equipment variants now use modern item classes backed by extracted legacy stat tables, restored tooltip data, and modern item models. Tooltip-side equipment restoration is no longer isolated from ship gameplay. |
| Ship entity baseline | Generic ship entity + legacy variant mapping + ownership baseline | Partial | `Code`, `Compile` | `entity/ship/LegacyShipEntity.java`, `entity/ship/LegacyShipBehaviorCatalog.java`, `entity/ship/ShipEntitySpecs.java`, `registry/ModEntityTypes.java` | One modern entity type now carries legacy egg-meta / class-id variants, basic owner claim / standby interaction, shared texture routing, first escort-follow behavior, owner-assist / faction targeting, active escort-vs-`Enemy` mob targeting, a persistent 6+18 slot ship inventory, runtime state (`level`, `morale`, `marriage`, `modernization`, `rescue`), restored legacy stats, the first catalog-routed legacy-style ranged attack layer, and a compatibility projectile entity for heavy / air attacks. Friendly death now locks interaction, prevents accidental revival, keeps cargo inside a recovered egg, and removes the ship from online teams. Full per-ship combat/runtime logic is still absent. |
| Ship inventory GUI | Owned ship menu + entity-backed slots + status screen | Partial | `Code`, `Compile` | `menu/ShipInventoryMenu.java`, `menu/ShipEquipmentSlot.java`, `client/screen/ShipInventoryScreen.java`, `network/ServerboundShipCommandPacket.java` | Sneak-right-click opens a real ship-side GUI backed by entity NBT inventory. Equipment slots enforce restored carrier restriction rules, the Phase 4 baseline exposes AI flags, route energy, combat stats, and server mode through menu data, and stop / mode toggle / AI flags / follow range now land in the shared Phase 5 ship-command service. Detached `shipUid + ship cache` fallback now preserves owner/readout access when the live entity is gone. The full old GUI page set is still pending. |
| Ship equipment integration | Equipment-slot stat application and restored attack math | Partial | `Code`, `Compile` | `entity/ship/ShipEquipmentProfile.java`, `entity/ship/LegacyShipEntity.java`, `entity/ship/LegacyShipStats.java`, `menu/ShipEquipmentSlot.java` | Mounted legacy equipment now feeds back into the restored 1.12.2 stat flow instead of a custom modern approximation. Equipment, morale, potion, and formation placeholders are combined in legacy order, ship UI now reads buffed legacy values, and ranged attack routing consumes the restored light / heavy / air damage families. Full projectile and per-weapon behavior parity is still pending. |
| Ship support items | Direct item-to-ship interactions for recovery, growth, and ownership flows | Partial | `Code`, `Compile` | `entity/ship/LegacyShipEntity.java`, `item/BucketRepairItem.java`, `item/CombatRationItem.java`, `item/MarriageRingItem.java`, `item/ModernKitItem.java`, `item/OwnerPaperItem.java`, `item/RepairGoddessItem.java`, `item/TrainingBookItem.java`, `item/PointerItem.java`, `item/KaitaiHammerItem.java` | Friendly ships now respond again to repair buckets, combat rations, training books, modernization kits, wedding rings, ownership papers, repair goddess storage, pointer caress mode, and kaitai dismantle. This is a focused support slice, not full legacy item parity. |
| Ship spawn eggs | Real spawn deployment + random small/large pools + hostile mob variants | Partial | `Code`, `Compile` | `item/LegacyShipSpawnEggItem.java`, `registry/ModItems.java`, `crafting/LegacyShipConstructionHelper.java`, `teitoku/TeitokuHelper.java` | The full current egg catalog now deploys real entities, including random primary/advanced pools and hostile `(Mob)` variants. Shipyard-produced `smallegg` now also restores legacy material-weighted ship rolls instead of using a flat random pool, friendly ship deployments record first collection state into Teitoku data, and recovered `shipegg*` stacks with `RecoveredShip` NBT can redeploy a preserved owner/variant/cargo snapshot. |
| Entity renderer baseline | Generic humanoid renderer + lowercase-safe legacy texture bridge | Partial | `Code`, `Compile` | `client/renderer/entity/LegacyShipRenderer.java`, `textures/entity/modern/*` | This is enough to see spawned ships in-client, but not enough for model-accurate legacy visuals. |
| Placeholder interaction items | Bucket/modern kit/training book etc. | Partial | `Code`, `Compile` | `item/LegacyUseFeedbackItem.java`, `item/LegacyPlaceholderItem.java` | They are intentionally usable placeholders, not full ports. |
| Legacy core heavy blocks | Volcore/polymetal/heavy grudge owner + mode + persistent charge baseline | Partial | `Code`, `Compile` | `block/LegacyCoreBlock.java`, `blockentity/LegacyCoreBlockEntity.java`, `menu/LegacyCoreMenu.java`, `client/screen/LegacyCoreScreen.java`, `registry/ModBlockEntities.java` | The three heavy blocks are no longer message-only placeholders: they persist owner, operating mode, and charge state, and share one Phase 4 control/readout page. Full multiblock energy chains and heavy-block-specific automation loops are still pending. |

## What Is Fully Checked Right Now

- `compileJava`, `processResources`, and `runGameTestServer` pass on Java 17.
- Current GameTest baseline: `All 68 required tests passed`.
- Phase 1 registered blockstate/model/item-model/menu texture/sound resource coverage is locked by tests.
- Phase 2 ship sound routing is locked by tests for friendly/hostile sound source semantics and combat event resolution.
- Phase 3 waypoint wait, crane route item/fluid/energy transfer, and large shipyard structure/fuel/energy/output loops are locked by tests.
- Phase 4 Crane, Waypoint, Large Shipyard, LegacyCore, and ShipInventory menu button/data paths are locked by tests.
- Phase 5 ship command packet encode/decode, old/new command equivalence, toggle-sit parity, owner/distance/dead-target gates, single-ship command loop, selected/current team dispatch, formation offsets, detached ship-inventory cache refresh, and gameplay-state client mirror fallback are locked by tests.
- Phase 6 AI flags, follow range, team slots, slot selection, formation id, ship command runtime NBT persistence, and ship cache online/offline/dead persistence are locked by tests.
- Phase 7 per-ship behavior catalog baseline is locked by tests for marriage/ring passive class-id dispatch and attack profile routing.
- Phase 7 hostile boss spawn runtime and route/guard command boundary behavior are locked by tests.
- Phase 7 hostile spawn scaling is locked by tests for boss level, morale, and full-health initialization.
- Friendly ship death recovery is locked by tests: dead/dying ships do not open ShipInventory, cargo stays inside the recovered egg, the dropped egg is owner-targeted, and redeploy restores owner/variant/cargo/health.
- Dev client can launch, Desk block interaction path has previously been smoke-tested in client, and the user reported the latest manual client pass found no obvious issues after the death/recovery fixes.

## What Is Still Only Code-Checked

- Fresh client-side smoke for the new Phase 5 typed ship commands: Pointer move/guard/attack/stop/sit/open-inventory and ShipInventory stop/AI flags/follow range.
- Deeper client-side visual smoke for rider/morph/mount skill HUD and `1~5` / `Z/X/C` input under longer sessions.
- Deeper client-side visual smoke for heavy/air projectile FX and reaction presentation under longer sessions.
- Deeper client-side smoke for friendly death recovery egg pickup/redeploy semantics, especially owner-only pickup behavior in a real multiplayer-style client situation.
- Target wrench pairing loop and ship tank interactions beyond their route/logistics GameTest coverage.
- Most placeholder item and block interaction messages.
- Equipment rendering and tooltip presentation in a real client.

## Immediate Next Targets

1. Run a short client smoke pass focused on the new Phase 5 typed ship commands from Pointer and ShipInventory.
2. Continue Phase 7 single-player gameplay depth: migrate more per-ship combat hooks into `LegacyShipBehaviorCatalog`, then deepen hostile/boss spawn and route/escort/standby edge cases.
3. Continue Phase 8/9 depth: model-accurate ship renderers, reaction pages, full projectile families, and remaining morph/player-skill special parity.

## Modern Source Snapshot

- Modern Java files: `172`
- Major slices:
  - `advancement`: 2
  - `registry`: 7
  - `item`: 23
  - `block`: 8
  - `blockentity`: 13
  - `entity`: 27
  - `ownership`: 1
  - `menu`: 16
  - `client`: 23
  - `crafting`: 6
  - `network`: 14
  - `sound`: 2
  - `teitoku`: 7
  - `team`: 2
  - `formation`: 1
  - `morph`: 9
  - `playerskill`: 3
  - `world`: 4
  - `combat`: 1
  - `gametest`: 1
