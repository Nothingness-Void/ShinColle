# ShinColle 1.20.1 Migration Flow Checklist

For the newest implementation handoff, read `AI-HANDOFF.md`.

Audit date: 2026-03-27

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
| 1 | Core registries and static assets | Phase 0 | Partial | `Code`, `Compile` | Modern item/block/menu/tab/sound/block-entity registries exist. Legacy resources are being reused, but data coverage is still uneven. |
| 2 | Sound layer | Phase 1 | Partial | `Code`, `Compile` | Modern sound registry and helper are in place. Item-side feedback is restored; entity/combat/block-wide usage is still missing. |
| 3 | Persistent block entities | Phases 1-2 | Partial | `Code`, `Compile`, `Runtime` | `desk`, `waypoint`, `crane`, and the small `shipyard` are now migrated at block-entity level. `volcore`, `polymetal`, `heavy grudge`, and the large shipyard structure remain pending. |
| 4 | Menus and screens | Phases 1-3 | Partial | `Code`, `Compile`, `Runtime` | `recipepaper`, desk reference items, desk terminal, crane terminal, waypoint terminal, the first ship inventory GUI, and the small shipyard GUI now exist. Most heavy block-backed flows are still absent. |
| 5 | Networking (`SimpleChannel`) | Phases 1-4 | Partial | `Code`, `Compile` | A first modern `SimpleChannel` now exists for Teitoku/admiral data sync on login, respawn, and dimension change. Ship GUI command packets, target-class packets, and broader gameplay packet families are still pending. |
| 6 | Saved data, attachments, capabilities | Phases 1-5 | Partial | `Code`, `Compile` | Item NBT and block-entity NBT exist. Route nodes share a modern owner model, ships now persist inventory plus the first runtime state slice (`level`, `morale`, `marriage`, modernization, rescue items), and the first old-version-driven Teitoku player capability plus server UID registry are now back. Team data, formations, and broader persistent player/server state are still not ported. |
| 7 | Entity types and spawn logic | Phases 1-6 | Partial | `Code`, `Compile` | A modern generic ship entity and real spawn-egg deployment path now exist. The first persistent ship inventory/menu slice is back, but legacy per-ship classes and combat logic are still pending. |
| 8 | Client renderers and visual glue | Phases 2-7 | Partial | `Code`, `Compile`, `Runtime` | Basic item property/render-layer hooks exist, several blocks use custom JSON models, and the first generic ship renderer can now draw legacy ship textures. No BER or per-ship renderer migration yet. |
| 9 | AI, combat, reactions | Phases 2-8 | Partial | `Code`, `Compile` | Ships now have escort / standby behavior, owner assist targeting, hostile-vs-friendly combat targeting, restored legacy stat rebuilding, legacy-style melee / light / heavy / air attack routing, and a first compatibility projectile layer for heavy / air attacks. Full projectile systems, per-ship weapon specials, and reaction pages are still missing. |
| 10 | Worldgen, advanced recipes, inter-mod | Phases 1-9 | Not started | none | Only basic resource/data reuse exists today. |

## Migrated Systems Checklist

| Area | Scope | Status | Self-check | Main entry points | Notes |
| --- | --- | --- | --- | --- | --- |
| Mod bootstrap | Entry, event bus, client setup | Done | `Code`, `Compile`, `Runtime` | `ShinColle.java` | Current root for all modern registrations and client hooks. |
| Creative tab | Modern creative tab | Done | `Code`, `Compile`, `Runtime` | `registry/ModCreativeModeTabs.java` | Usable in-game. |
| Item registry coverage | Legacy item IDs registered | Partial | `Code`, `Compile`, `Runtime` | `registry/ModItems.java` | Utility items, equipment lines, and spawn eggs all exist in the modern registry. Remaining gaps are deeper entity gameplay, not missing item IDs. |
| Block registry coverage | Legacy block IDs registered | Partial | `Code`, `Compile`, `Runtime` | `registry/ModBlocks.java` | All core IDs exist. `desk`, `waypoint`, and `crane` now have real block logic; several heavy blocks are still placeholders. |
| Sound registry | Legacy voice/event bridge | Partial | `Code`, `Compile` | `registry/ModSoundEvents.java`, `sound/ShinColleSoundHelper.java` | Good enough for current item/block UX, not enough for entity parity. |
| Recipe Paper | Item NBT + menu + screen | Done | `Code`, `Compile`, `Runtime` | `item/RecipePaperItem.java`, `menu/RecipePaperMenu.java`, `client/screen/RecipePaperScreen.java` | First complete modern item GUI chain. |
| Desk reference items | Portable book/radar placeholder UIs | Done | `Code`, `Compile`, `Runtime` | `item/DeskReferenceItem.java`, `menu/DeskReferenceMenu.java`, `client/screen/DeskReferenceScreen.java` | Keeps item-side GUI path alive. |
| Desk block | Block entity + block menu + persisted mode | Done | `Code`, `Compile`, `Runtime` | `block/DeskBlock.java`, `blockentity/DeskBlockEntity.java`, `menu/DeskTerminalMenu.java`, `client/screen/DeskTerminalScreen.java` | User-tested in dev client; model/orientation has also been corrected. |
| Waypoint block | Owner/stay/route/container persistence + terminal UI | Partial | `Code`, `Compile` | `block/WaypointBlock.java`, `blockentity/WaypointBlockEntity.java`, `menu/WaypointTerminalMenu.java`, `client/screen/WaypointTerminalScreen.java` | Core routing data and block-side controls are modernized, but ship AI has not been reattached yet. |
| Crane block | Owner/route/container persistence + filter terminal | Partial | `Code`, `Compile` | `block/CraneBlock.java`, `blockentity/CraneBlockEntity.java`, `menu/CraneTerminalMenu.java`, `client/screen/CraneTerminalScreen.java` | Replaces the placeholder block with a configurable crane baseline, including persistent ghost filter slots and legacy-style GUI layout. Transfer loops are still pending. |
| Small shipyard | Material slots + fuel + ship/equip build loop + legacy build egg NBT | Partial | `Code`, `Compile` | `block/SmallShipyardBlock.java`, `blockentity/SmallShipyardBlockEntity.java`, `menu/SmallShipyardMenu.java`, `client/screen/SmallShipyardScreen.java`, `crafting/SmallShipyardRecipes.java` | The small hydrothermal vent now restores the old `4 materials + 1 fuel + 1 output` flow, including loop modes, instant construction support, and shipyard-built eggs carrying legacy material data. Large shipyard structure logic and richer fuel automation are still missing. |
| Ownership foundation | Shared UUID/name/UID owner model for modern persistent content | Partial | `Code`, `Compile` | `ownership/PlayerOwnerData.java`, `blockentity/WaypointBlockEntity.java`, `blockentity/CraneBlockEntity.java`, `blockentity/SmallShipyardBlockEntity.java`, `entity/ship/LegacyShipEntity.java` | Owner-tagged blocks and ships now share one owner serialization/edit-check path instead of each system carrying custom code. The model has been expanded from `UUID + name` to `UUID + name + playerUID` so later team/formation systems can keep following the old design. |
| Admiral capability baseline | Old `CapaTeitoku` field slice + server UID registry + sync packet | Partial | `Code`, `Compile` | `teitoku/TeitokuData.java`, `teitoku/TeitokuSavedData.java`, `teitoku/TeitokuEvents.java`, `teitoku/TeitokuHelper.java`, `network/ModNetwork.java` | The first old-version-driven admiral data layer is back: player name/UID, ring state, marriage count, boss/team cooldowns, collected ship/equipment lists, target-class storage, and client sync on login/respawn/dimension change. Team data, formation state, target-class editing UI, and the old GUI packet families are still missing. |
| Block models | Desk/crane/waypoint custom silhouettes | Partial | `Code`, `Compile`, `Runtime` | `assets/shincolle/models/block/blockdesk.json`, `assets/shincolle/models/block/blockcrane.json`, `assets/shincolle/models/block/blockwaypoint.json` | Desk has been smoke-tested already; crane and waypoint custom models are now in place but not yet manually rechecked in client. |
| Target Wrench | Selected-target NBT + route-node pairing | Partial | `Code`, `Compile` | `item/TargetWrenchItem.java` | Works server-side for waypoint/crane route-node pairing and container links, but old packet-driven extensions are not back. |
| Waypoint item placement | In-air targeted placement flow | Partial | `Code`, `Compile` | `item/WaypointBlockItem.java` | Restores old placement style for waypoint blocks. |
| Ship tank | Fluid capability container | Partial | `Code`, `Compile` | `item/ShipTankItem.java` | Basic fill/place loop works; deeper ship logistics remain pending. |
| Pointer item | Mode storage + model predicate | Partial | `Code`, `Compile` | `item/PointerItem.java` | Item-side mode switching works; fleet command logic is still pending. |
| Owner paper | Dual-signature item NBT | Partial | `Code`, `Compile` | `item/OwnerPaperItem.java` | Self-contained item logic restored. |
| Marriage ring | Toggle state + foil state + admiral ring sync | Partial | `Code`, `Compile` | `item/MarriageRingItem.java`, `teitoku/TeitokuHelper.java` | Item UX is restored and now writes back into the Teitoku capability (`hasRing`, active/flying state). Full old marriage bonus chains are still absent. |
| Kaitai hammer | Reusable crafting tool behavior | Partial | `Code`, `Compile` | `item/KaitaiHammerItem.java` | Tool durability path exists; legacy dismantle system is still not back. |
| Combat ration | Food/item-side morale baseline | Partial | `Code`, `Compile` | `item/CombatRationItem.java` | Standalone consumption works; ship-specific legacy hooks remain missing. |
| Repair goddess | Simple rescue item | Partial | `Code`, `Compile` | `item/RepairGoddessItem.java` | Minimal standalone behavior only. |
| Legacy equipment lines | Equipment stats, ordering, tooltip data, and models | Done | `Code`, `Compile` | `item/equipment/LegacyEquipmentItem.java`, `item/equipment/LegacyEquipmentStatsRepository.java`, `registry/ModItems.java` | All legacy equipment variants now use modern item classes backed by extracted legacy stat tables, restored tooltip data, and modern item models. Tooltip-side equipment restoration is no longer isolated from ship gameplay. |
| Ship entity baseline | Generic ship entity + legacy variant mapping + ownership baseline | Partial | `Code`, `Compile` | `entity/ship/LegacyShipEntity.java`, `entity/ship/ShipEntitySpecs.java`, `registry/ModEntityTypes.java` | One modern entity type now carries legacy egg-meta / class-id variants, basic owner claim / standby interaction, shared texture routing, first escort-follow behavior, owner-assist / faction targeting, active escort-vs-`Enemy` mob targeting, a persistent 6+18 slot ship inventory, runtime state (`level`, `morale`, `marriage`, `modernization`, `rescue`), restored legacy stats, the first legacy-style ranged attack routing layer, and a compatibility projectile entity for heavy / air attacks. Full per-ship combat/runtime logic is still absent. |
| Ship inventory GUI | Owned ship menu + entity-backed slots + status screen | Partial | `Code`, `Compile` | `menu/ShipInventoryMenu.java`, `menu/ShipEquipmentSlot.java`, `client/screen/ShipInventoryScreen.java` | Sneak-right-click now opens a real ship-side GUI backed by entity NBT inventory. Equipment slots now enforce restored carrier restriction rules, but AI pages, combat pages, and packet-heavy controls from the old GUI are still pending. |
| Ship equipment integration | Equipment-slot stat application and restored attack math | Partial | `Code`, `Compile` | `entity/ship/ShipEquipmentProfile.java`, `entity/ship/LegacyShipEntity.java`, `entity/ship/LegacyShipStats.java`, `menu/ShipEquipmentSlot.java` | Mounted legacy equipment now feeds back into the restored 1.12.2 stat flow instead of a custom modern approximation. Equipment, morale, potion, and formation placeholders are combined in legacy order, ship UI now reads buffed legacy values, and ranged attack routing consumes the restored light / heavy / air damage families. Full projectile and per-weapon behavior parity is still pending. |
| Ship support items | Direct item-to-ship interactions for recovery, growth, and ownership flows | Partial | `Code`, `Compile` | `entity/ship/LegacyShipEntity.java`, `item/BucketRepairItem.java`, `item/CombatRationItem.java`, `item/MarriageRingItem.java`, `item/ModernKitItem.java`, `item/OwnerPaperItem.java`, `item/RepairGoddessItem.java`, `item/TrainingBookItem.java`, `item/PointerItem.java`, `item/KaitaiHammerItem.java` | Friendly ships now respond again to repair buckets, combat rations, training books, modernization kits, wedding rings, ownership papers, repair goddess storage, pointer caress mode, and kaitai dismantle. This is a focused support slice, not full legacy item parity. |
| Ship spawn eggs | Real spawn deployment + random small/large pools + hostile mob variants | Partial | `Code`, `Compile` | `item/LegacyShipSpawnEggItem.java`, `registry/ModItems.java`, `crafting/LegacyShipConstructionHelper.java`, `teitoku/TeitokuHelper.java` | The full current egg catalog now deploys real entities, including random primary/advanced pools and hostile `(Mob)` variants. Shipyard-produced `smallegg` now also restores legacy material-weighted ship rolls instead of using a flat random pool, and friendly ship deployments now record first collection state into Teitoku data. |
| Entity renderer baseline | Generic humanoid renderer + lowercase-safe legacy texture bridge | Partial | `Code`, `Compile` | `client/renderer/entity/LegacyShipRenderer.java`, `textures/entity/modern/*` | This is enough to see spawned ships in-client, but not enough for model-accurate legacy visuals. |
| Placeholder interaction items | Bucket/modern kit/training book etc. | Partial | `Code`, `Compile` | `item/LegacyUseFeedbackItem.java`, `item/LegacyPlaceholderItem.java` | They are intentionally usable placeholders, not full ports. |
| Placeholder interactive blocks | Volcore/polymetal/heavy grudge | Partial | `Code`, `Compile`, `Runtime` | `block/LegacyInteractiveBlock.java` | Explicit migration-status feedback exists so they are no longer silent dead content. |

## What Is Fully Checked Right Now

- `compileJava` and `processResources` pass on Java 17.
- Dev client can launch.
- Desk block interaction path has been smoke-tested in client.
- Registry-driven content loads without blocking startup.

## What Is Still Only Code-Checked

- Waypoint placement and pairing flow
- Crane terminal interaction flow
- Waypoint terminal interaction flow
- Target wrench pairing loop
- Ship tank interactions in the current batch
- Most placeholder item and block interaction messages
- Equipment rendering and tooltip data after this batch still need one in-client smoke test
- New ship entity spawn / render / interaction path still needs one in-client smoke test
- New ship inventory GUI path still needs one in-client smoke test
- New ship support-item interaction path still needs one in-client smoke test

## Immediate Next Targets

1. Extend the new Teitoku/UID foundation into real team data, formation state, and target-class sync instead of ad-hoc owner lookups.
2. Replace the current generic ship baseline with real ship runtime state: inventory, task state, combat state, and per-ship behavior hooks.
3. Introduce dedicated ship-side GUI/command packets on top of the current Teitoku sync channel.

## Modern Source Snapshot

- Modern Java files: `94`
- Major slices:
  - `registry`: 7
  - `item`: 18
  - `item/equipment`: 3
  - `block`: 5
  - `blockentity`: 5
  - `entity/ship`: 14
  - `entity/projectile`: 1
  - `ownership`: 1
  - `menu`: 10
  - `client/screen`: 7
  - `client/model`: 4
  - `client/renderer`: 4
  - `crafting`: 5
  - `network`: 2
  - `sound`: 2
  - `teitoku`: 5
