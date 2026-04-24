# ShinColle 1.20.1 Migration Flow Checklist

For the newest implementation handoff, read `AI-HANDOFF.md`.

Audit date: 2026-04-24

This file tracks the port in dependency order and checks what is genuinely migrated today.
It is stricter than `PORTING-1.20.1.md`: code that is only registered as a placeholder is not marked as fully migrated.
Before continuing entity, resource, or renderer work, re-read `PORTING-MISTAKES.md`.

## Hard Rule

- Only the 1.20.1 modern port line is writable for migration work: `src/modern/java`, `src/modern/resources`, and the related migration docs.
- `src/main/java`, `src/main/resources`, and branch `mc-1.12.2` are read-only legacy reference truth. Do not merge modern migration commits into them.
- The correct working branch / PR line for this port is `codex/mc-1.20.1`. `mc-1.12.2` is never the migration target branch.
- Legacy 1.12 behavior remains the semantic source of truth, but all actual implementation changes belong on the 1.20.1 modern side.
- If the local `mc-1.12.2` branch is accidentally moved or polluted, reset it back to `upstream/mc-1.12.2` before doing anything else.

## Self-check Rules

| Check item | Meaning |
| --- | --- |
| `Code` | Modern code exists under `src/modern/java` and is wired into the current registries. |
| `Compile` | `./gradlew compileJava processResources` passes with Java 17. |
| `Runtime` | Manually smoke-tested in a dev client. |
| `Partial` | Usable baseline exists, but a legacy subsystem is still missing. |
| `Verified baseline` | Code plus GameTest/resource smoke coverage exists, while exact legacy edge parity or manual multiplayer/client depth may still be tracked separately. |

## Phase Flow

| Phase | System | Dependency | Current status | Self-check | Notes |
| --- | --- | --- | --- | --- | --- |
| 0 | Build, toolchain, mod entry | none | Done | `Code`, `Compile`, `Runtime` | ForgeGradle 6, Java 17, modern `@Mod` bootstrap, client can boot. |
| 1 | Core registries and static assets | Phase 0 | Done | `Code`, `Compile` | Modern item/block/menu/tab/sound/block-entity registries exist and GameTest now locks registered blockstates, model references, item models, block loot tables, menu GUI textures, and `sounds.json` packaged OGG references. Client visual smoke remains separate. |
| 2 | Sound layer | Phase 1 | Done | `Code`, `Compile` | Modern sound registry and helper are in place. Ship idle/hurt/death voice routing plus light/heavy/air/melee combat sound events resolve, and friendly/hostile ships use the expected sound sources. |
| 3 | Persistent block entities | Phases 1-2 | Done | `Code`, `Compile` | `desk`, `waypoint`, `crane`, small shipyard, VolCore, polymetal servants, and the heavy-grudge multiblock large shipyard run on modern block entities. Route transfer is ship-arrival driven, waypoint waits are ship-local, and the 1.12 large shipyard structure/fuel/energy/output path is GameTest-covered. |
| 4 | Menus and screens | Phases 1-3 | Done | `Code`, `Compile` | `recipepaper`, desk reference items, desk terminal, crane terminal, waypoint terminal, small/large shipyard, legacy core, and ship inventory menus/screens have a Phase 4 server-button/readout baseline. |
| 5 | Networking (`SimpleChannel`) | Phases 1-4 | Done (verified baseline) | `Code`, `Compile` | `SimpleChannel` carries Teitoku sync, gameplay command/state sync, combat FX, player skill casts, desk/team/ally commands, morph commands, and a dedicated `ServerboundShipCommandPacket` family for ship commands. Old gameplay ship commands and typed packets share `ShipCommandService`; failure feedback is player-visible for invalid target, cooldown, unavailable skill, range, and supply shortage. Exact old GUI packet-page parity remains a residual manual parity risk. |
| 6 | Saved data, attachments, capabilities | Phases 1-5 | Done (verified baseline) | `Code`, `Compile` | Teitoku/team/formation data plus ship runtime command state now persist AI flags, follow range, route energy/wait, command pos, guard target, current team slots, selected slots, formation id, morale/runtime skill state, fuel, light/heavy ammo, grudge, ship UID lookup, and ship cache `online / offline / dead` lifecycle. Legacy `TeitokuExtProps`, `hasRing`, `FormatID`, `TeamListN`, `SelectStateN`, `unameN`, owner UID/name, and target-class compatibility are GameTest-covered. |
| 7 | Entity types and spawn logic | Phases 1-6 | Done (verified baseline) | `Code`, `Compile` | A single modern 1.20 entity adapter carries legacy egg-meta / class-id variants, real spawn-egg deployment, owner gate, recovered-egg redeploy, route/guard/runtime state, support items, and catalog-routed combat. `LegacyShipBehaviorCatalog` owns attack routing, marriage/ring passives, hostile/elite/boss state, high-value combat hooks, boss phase profiles, hostile loot metadata, AI priority order, and registered-roster coverage. Exact rare per-ship behavior parity remains a residual manual parity risk. |
| 8 | Client renderers and visual glue | Phases 2-7 | Done (verified baseline) | `Code`, `Compile`, `Runtime` | `LegacyShipRenderCatalog` maps every registered ship spec to a legacy model source, texture, shadow radius, and hostile flag; registered model sources and textures are GameTest-covered. Combat/reaction/particle bus, projectile visuals, summon/mount static model sources, and Desk/Small/Large Shipyard BER baselines are covered. A dev-client boot/resource smoke reached atlas load without ShinColle crash/fallback errors. Exact legacy pose/layer renderer parity remains a residual manual parity risk. |
| 9 | AI, combat, reactions | Phases 2-8 | Done (verified baseline) | `Code`, `Compile` | Ships now have escort / standby behavior, owner assist targeting, hostile-vs-friendly targeting, fixed AI priority order (`death/stop/sit > explicit command > combat target > auto supply > pickup > idle/follow`), restored legacy stat rebuilding, melee/light/heavy/air routing, resource gates, accepted-attack cooldown/resource feedback, heavy/air projectile families with explicit profiles, high-value special hooks, boss summons, loot, and supply failure messaging. Full old reaction pages and every rare weapon edge case remain residual manual parity risk. |
| 10 | Worldgen, advanced recipes, inter-mod | Phases 1-9 | Done (verified baseline) | `Code`, `Compile` | The non-intermod resource loop is documented and tested through actual packaged data plus DeskReference resource-source pages: polymetal, abyssmetal, grudge, ammo, combat ration, fuel, shipyard/build materials, and related guidance each have a discoverable world/recipe/loot/hostile/advancement source. 1.12 `ConfigLoot` default chest injection, polymetal ore/gravel placement constants, and default `ModOres` ore-dict-to-tag mappings are now GameTest-covered. Remaining exact-parity risk is worldgen edge behavior that needs live terrain validation plus optional external-compat aliases such as `Polymetal_as_Mn`. Real external inter-mod APIs stay framework-only. |

## Migrated Systems Checklist

| Area | Scope | Status | Self-check | Main entry points | Notes |
| --- | --- | --- | --- | --- | --- |
| Mod bootstrap | Entry, event bus, client setup | Done | `Code`, `Compile`, `Runtime` | `ShinColle.java` | Current root for all modern registrations and client hooks. |
| Creative tab | Modern creative tab | Done | `Code`, `Compile`, `Runtime` | `registry/ModCreativeModeTabs.java` | Usable in-game. |
| Item registry coverage | Legacy item IDs registered | Done | `Code`, `Compile` | `registry/ModItems.java` | Utility items, equipment lines, spawn eggs, and block items all exist in the modern registry, with item-model coverage locked by GameTest. Remaining gaps are deeper gameplay parity, not missing item IDs. |
| Block registry coverage | Legacy block IDs registered | Done | `Code`, `Compile` | `registry/ModBlocks.java` | Legacy block IDs are registered; the modern-only standalone `blocklargeshipyard` ID has been removed for strict 1.12 parity. Registered blockstate/model/item-model/block-loot resources are GameTest-covered. Internal light helpers and special-NBT `blockgrudgeheavy` drops are explicit no-drop loot-table exceptions. Gameplay depth still varies by block family. |
| Sound registry | Legacy voice/event bridge | Done | `Code`, `Compile` | `registry/ModSoundEvents.java`, `sound/ShinColleSoundHelper.java`, `entity/ship/LegacyShipEntity.java` | Current Phase 2 sound layer is wired: ship idle/hurt/death voices resolve, combat fire/light/heavy/air/melee events resolve, and friendly/hostile sound sources are split. |
| Recipe Paper | Item NBT + menu + screen | Done | `Code`, `Compile`, `Runtime` | `item/RecipePaperItem.java`, `menu/RecipePaperMenu.java`, `client/screen/RecipePaperScreen.java` | First complete modern item GUI chain. |
| Desk reference items | Portable book/radar reference UIs | Done | `Code`, `Compile`, `Runtime` | `item/DeskReferenceItem.java`, `menu/DeskReferenceMenu.java`, `client/screen/DeskReferenceScreen.java` | Keeps item-side GUI path alive and exposes the tested resource-source guide. |
| Desk block | Block entity + block menu + persisted mode | Done | `Code`, `Compile`, `Runtime` | `block/DeskBlock.java`, `blockentity/DeskBlockEntity.java`, `menu/DeskTerminalMenu.java`, `client/screen/DeskTerminalScreen.java` | User-tested in dev client; model/orientation has also been corrected. |
| Waypoint block | Owner/stay/route/container persistence + terminal UI | Done | `Code`, `Compile` | `block/WaypointBlock.java`, `blockentity/WaypointBlockEntity.java`, `menu/WaypointTerminalMenu.java`, `client/screen/WaypointTerminalScreen.java`, `entity/ship/LegacyShipEntity.java` | Core routing data and block-side controls are modernized. Ship route handling now waits per ship and advances to the next route node without storing shared countdown state in the waypoint BE. |
| Crane block | Owner/route/container persistence + filter terminal | Done | `Code`, `Compile` | `block/CraneBlock.java`, `blockentity/CraneBlockEntity.java`, `menu/CraneTerminalMenu.java`, `client/screen/CraneTerminalScreen.java`, `entity/ship/LegacyShipEntity.java` | Crane stores configuration/filter/route state only. Item, fluid, and energy transfers execute when a ship reaches the crane route node, avoiding targetless chest-drain ticks. |
| Small shipyard | Material slots + fuel + ship/equip build loop + legacy build egg NBT | Done | `Code`, `Compile` | `block/SmallShipyardBlock.java`, `blockentity/SmallShipyardBlockEntity.java`, `menu/SmallShipyardMenu.java`, `client/screen/SmallShipyardScreen.java`, `crafting/SmallShipyardRecipes.java` | The small hydrothermal vent restores the old `4 materials + 1 fuel + 1 output` flow, including loop modes, instant construction support, and shipyard-built eggs carrying legacy material data. |
| Large shipyard | 1.12 Heavy Grudge multiblock + fuel/energy pool + large ship output | Done | `Code`, `Compile` | `block/HeavyGrudgeBlock.java`, `block/PolymetalServantBlock.java`, `blockentity/HeavyGrudgeBlockEntity.java`, `blockentity/PolymetalServantBlockEntity.java`, `blockentity/LargeShipyardStructureHelper.java`, `menu/LargeShipyardMenu.java`, `client/screen/LargeShipyardScreen.java`, `crafting/LargeShipyardRecipes.java` | The standalone modern `blocklargeshipyard` ID/resources were removed. Right-clicking a valid 3x3x3 `blockgrudgeheavy` + `blockpolymetal` structure forms the 1.12 large shipyard; unformed heavy grudge does not open a core GUI. Servant proxying, old `matsBuild + matsStock`, fuel/route-energy storage, selected build materials, special NBT block drop/place, and large ship egg output are covered by GameTest. |
| Ownership foundation | Shared UUID/name/UID owner model for modern persistent content | Done (verified baseline) | `Code`, `Compile` | `ownership/PlayerOwnerData.java`, `blockentity/WaypointBlockEntity.java`, `blockentity/CraneBlockEntity.java`, `blockentity/SmallShipyardBlockEntity.java`, `entity/ship/LegacyShipEntity.java`, `item/LegacyShipSpawnEggItem.java` | Owner-tagged blocks, ships, and recovered eggs share one owner serialization/edit-check path. The model covers `UUID + name + playerUID`, and owner-only recovered egg pickup/redeploy is GameTest-covered. |
| Admiral capability baseline | Old `CapaTeitoku` field slice + server UID registry + sync packet | Done (verified baseline) | `Code`, `Compile` | `teitoku/TeitokuData.java`, `teitoku/TeitokuSavedData.java`, `teitoku/TeitokuEvents.java`, `teitoku/TeitokuHelper.java`, `network/ModNetwork.java` | Player name/UID, ring state, marriage count, boss/team cooldowns, collected ship/equipment lists, target-class storage, current team, team slots, slot selection, formation id, and client sync are restored. Legacy `TeitokuExtProps`, `hasRing`, `FormatID`, `TeamListN`, `SelectStateN`, `unameN`, and nested target classes are GameTest-covered. |
| Block models | Desk/crane/waypoint custom silhouettes | Done (resource baseline) | `Code`, `Compile`, `Runtime` | `assets/shincolle/models/block/blockdesk.json`, `assets/shincolle/models/block/blockcrane.json`, `assets/shincolle/models/block/blockwaypoint.json` | Desk was previously smoke-tested; crane and waypoint custom models are packaged and included in the `runClient` resource smoke. Real in-world block visual review remains in the manual smoke list. |
| Target Wrench | Selected-target NBT + route-node pairing | Done (verified baseline) | `Code`, `Compile` | `item/TargetWrenchItem.java` | Waypoint/crane route-node pairing, container links, owner checks, distance checks, and morph selection are implemented on the modern interaction path. Exact old packet UI extensions remain a residual parity risk only if required by a legacy screen. |
| Waypoint item placement | In-air targeted placement flow | Done | `Code`, `Compile` | `item/WaypointBlockItem.java` | Restores old placement style for waypoint blocks on the modern placement API. |
| Ship tank | Fluid capability container | Done (verified baseline) | `Code`, `Compile` | `item/ShipTankItem.java` | Fill, drain, place-liquid, shipyard fuel, and crane route liquid transfer are restored through Forge fluid capabilities and GameTest-covered route logistics. |
| Pointer item | Command-scepter mode handling | Done (verified baseline) | `Code`, `Compile` | `item/PointerItem.java`, `network/ServerboundShipCommandPacket.java`, `network/ShipCommandService.java`, `teitoku/TeitokuHelper.java` | Ship move, guard, attack, stop, sit toggle, open-inventory, selected-team dispatch, current-team dispatch, and formation offsets use the dedicated ship command packet or shared gameplay command bus and are GameTest-covered. |
| Owner paper | Dual-signature item NBT | Done | `Code`, `Compile` | `item/OwnerPaperItem.java`, `entity/ship/LegacyShipEntity.java` | Dual signer NBT, UID resolution, and ship ownership transfer entry are restored. |
| Marriage ring | Toggle state + foil state + admiral ring sync | Done (verified baseline) | `Code`, `Compile` | `item/MarriageRingItem.java`, `teitoku/TeitokuHelper.java`, `entity/ship/LegacyShipEntity.java`, `menu/ShipInventoryMenu.java`, `client/screen/ShipInventoryScreen.java`, `network/ServerboundShipCommandPacket.java` | Item UX writes back into Teitoku data (`hasRing`, active/flying state). Ship-side legacy `WedEffect` toggle, NBT persistence, typed command transport, and catalog-routed 1.12 marriage passive slice are restored and tested. Rare edge-case bonus chains remain residual exact parity risk. |
| Kaitai hammer | Reusable crafting tool + owner-gated ship dismantle | Done (verified baseline) | `Code`, `Compile` | `item/KaitaiHammerItem.java`, `entity/ship/LegacyShipEntity.java` | Crafting remainder durability and owner-gated left-click ship dismantle now route through `performKaitai`, with feedback on failed ownership checks. |
| Combat ration | Food/item-side morale/fuel baseline | Done (verified baseline) | `Code`, `Compile` | `item/CombatRationItem.java`, `entity/ship/LegacyShipEntity.java` | Standalone consumption works, and friendly ship feeding restores morale plus ship fuel for the runtime supply loop. |
| Repair goddess | Simple rescue item | Done | `Code`, `Compile` | `item/RepairGoddessItem.java`, `entity/ship/LegacyShipEntity.java` | The item is back to its 1.12 foil-plus-tooltip scope, and friendly ships now consume stored repair goddess cargo to cancel lethal damage, restore full HP, and enter the legacy rescue invulnerability window. |
| Legacy equipment lines | Equipment stats, ordering, tooltip data, and models | Done | `Code`, `Compile` | `item/equipment/LegacyEquipmentItem.java`, `item/equipment/LegacyEquipmentStatsRepository.java`, `registry/ModItems.java` | All legacy equipment variants now use modern item classes backed by extracted legacy stat tables, restored tooltip data, and modern item models. Tooltip-side equipment restoration is no longer isolated from ship gameplay. |
| Ship entity baseline | Modern carrier for legacy variant/runtime behavior | Done (verified baseline) | `Code`, `Compile` | `entity/ship/LegacyShipEntity.java`, `entity/ship/LegacyShipBehaviorCatalog.java`, `entity/ship/ShipEntitySpecs.java`, `registry/ModEntityTypes.java` | One modern entity type carries legacy egg-meta / class-id variants, owner claim, standby, escort/follow, owner assist, faction targeting, 6+18 inventory, runtime state, restored legacy stats, catalog-routed melee/light/heavy/air attacks, explicit projectile profiles, support items, recovered death egg, and online/offline/dead cache handling. Exact rare per-ship runtime details remain residual parity risk. |
| Ship inventory GUI | Owned ship menu + entity-backed slots + status screen | Done (verified baseline) | `Code`, `Compile` | `menu/ShipInventoryMenu.java`, `menu/ShipEquipmentSlot.java`, `client/screen/ShipInventoryScreen.java`, `network/ServerboundShipCommandPacket.java` | Sneak-right-click opens a real ship-side GUI backed by entity NBT inventory. Equipment slots enforce restored carrier restriction rules; server data exposes AI flags, route energy, combat stats, supply status, and server mode; stop/mode/AI/follow range commands use the shared ship-command service. Detached `shipUid + ship cache` fallback preserves owner/readout access when the live entity is gone. Exact old GUI page layout remains residual manual parity risk. |
| Ship equipment integration | Equipment-slot stat application and restored attack math | Done (verified baseline) | `Code`, `Compile` | `entity/ship/ShipEquipmentProfile.java`, `entity/ship/LegacyShipEntity.java`, `entity/ship/LegacyShipStats.java`, `menu/ShipEquipmentSlot.java` | Mounted legacy equipment feeds into restored 1.12.2 stat flow. Equipment, morale, potion, and formation modifiers are combined in legacy order; ship UI reads buffed legacy values; ranged attack routing consumes restored light/heavy/air damage families and explicit projectile profiles. Exact per-weapon visual/layer parity remains residual manual risk. |
| Ship support items | Direct item-to-ship interactions for recovery, growth, supply, and ownership flows | Done (verified baseline) | `Code`, `Compile` | `entity/ship/LegacyShipEntity.java`, `item/BucketRepairItem.java`, `item/CombatRationItem.java`, `item/MarriageRingItem.java`, `item/ModernKitItem.java`, `item/OwnerPaperItem.java`, `item/RepairGoddessItem.java`, `item/TrainingBookItem.java`, `item/PointerItem.java`, `item/KaitaiHammerItem.java` | Friendly ships respond to repair buckets, combat rations, grudge/ammo supply items, training books, modernization kits, wedding rings, ownership papers, cargo-stored repair goddess rescue, pointer caress mode, and kaitai dismantle. Non-intermod placeholder item feedback has been removed from the normal source scan. |
| Ship spawn eggs | Real spawn deployment + random small/large pools + hostile mob variants | Done (verified baseline) | `Code`, `Compile` | `item/LegacyShipSpawnEggItem.java`, `registry/ModItems.java`, `crafting/LegacyShipConstructionHelper.java`, `teitoku/TeitokuHelper.java` | The full current egg catalog deploys real entities, including random primary/advanced pools and hostile `(Mob)` variants. Shipyard-produced `smallegg` restores legacy material-weighted rolls; friendly deployments record first collection into Teitoku data; recovered `shipegg*` stacks with `RecoveredShip` NBT redeploy preserved owner/variant/cargo and now enforce owner-only pickup/redeploy. |
| Entity renderer baseline | Registered legacy-model renderer catalog + lowercase-safe texture bridge | Done (verified baseline) | `Code`, `Compile`, `Runtime` | `client/renderer/entity/LegacyShipRenderer.java`, `client/renderer/entity/LegacyShipRenderCatalog.java`, `client/renderer/blockentity/HeavyGrudgeBlockEntityRenderer.java`, `textures/entity/modern/*`, `legacy_model_sources/*` | Every registered ship spec resolves through `LegacyShipRenderCatalog` to a legacy model source and texture; registered sources parse and assets exist under GameTest. The large shipyard BER is now registered on formed `HeavyGrudgeBlockEntity`, matching the 1.12 TESR entry point while reusing the legacy model sources/textures. Runtime rendering still uses the modern generic renderer shell, so exact 1.12 pose/layer parity remains residual manual risk. |
| Legacy core heavy blocks | VolCore old fuel/power/aura baseline | Done (verified baseline) | `Code`, `Compile` | `block/LegacyCoreBlock.java`, `blockentity/LegacyCoreBlockEntity.java`, `menu/LegacyCoreMenu.java`, `client/screen/LegacyCoreScreen.java`, `registry/ModBlockEntities.java` | VolCore keeps the 1.12 fuel slots, active button, stored power pool, nearby-liquid healing, and dry-area burning behavior. Polymetal is only the large-shipyard servant block, and heavy grudge is only the multiblock master; neither remains a standalone legacy core. Route energy maps to the old VolCore power pool without adding a player-visible charge/drain mode. |

## What Is Fully Checked Right Now

- `compileJava`, `processResources`, and `runGameTestServer` pass on Java 17.
- Current GameTest baseline: `All 91 required tests passed`.
- Phase 1 registered blockstate/model/item-model/block-loot/menu texture/sound resource coverage is locked by tests; `blocklightair`, `blocklightliquid`, and special-NBT `blockgrudgeheavy` are explicit no-drop loot-table exceptions.
- Phase 2 ship sound routing is locked by tests for friendly/hostile sound source semantics and combat event resolution.
- Phase 3 waypoint wait, crane route item/fluid/energy transfer, and large shipyard structure/fuel/energy/output loops are locked by tests.
- Phase 4 Crane, Waypoint, Large Shipyard, LegacyCore, and ShipInventory menu button/data paths are locked by tests.
- Phase 5 ship command packet encode/decode, old/new command equivalence, toggle-sit parity, owner/distance/dead-target gates, single-ship command loop, selected/current team dispatch, formation offsets, detached ship-inventory cache refresh, and gameplay-state client mirror fallback are locked by tests.
- Phase 6 AI flags, follow range, team slots, slot selection, formation id, ship command runtime NBT persistence, ship fuel/light ammo/heavy ammo/grudge NBT persistence, legacy ship runtime tag migration, legacy `CapaTeitoku` wrapper/field migration, and ship cache online/offline/dead persistence are locked by tests.
- Phase 7 per-ship behavior catalog coverage is locked by tests for marriage/ring passive class-id dispatch, `WedEffect` toggle persistence, and attack profile routing.
- Phase 7 hostile boss spawn runtime and route/guard command boundary behavior are locked by tests.
- Phase 7 hostile spawn scaling is locked by tests for boss level, morale, and full-health initialization.
- Phase 7 first special combat hooks are locked by tests for Shimakaze/Nagato/Yamato/Tenryuu/Tatsuta/Atago/Takao/Kongou-class class-id dispatch, hostile mirrors, heavy cooldown application, and heavy miss no-damage semantics.
- Phase 7 boss phase and hostile loot catalog metadata are locked by tests for action cycle, escort summon egg, base cooldowns, boss bonus drops, abyss metal drops, and egg drop chance.
- Phase 7/9 reachable-roster coverage is locked by tests: every single-player reachable friendly, hostile, and boss ship has an explicit behavior coverage tier, every archetype is represented, and the AI priority order is fixed.
- Phase 7 pickup priority boundaries are locked by tests so auto pickup yields to route/move, guard, combat target, sit, and disabled auto-supply state.
- Phase 5/6 player-facing skill failure feedback is locked by tests for invalid target, unavailable skill, out-of-range target, fuel/grudge/ammo shortage, cooldown, and accepted direct attacks that already entered cooldown.
- Single-player mainline smoke coverage is locked by tests for ship support-item use, runtime supply refill, ranged combat resource gates/consumption, typed ship command AI flags/follow range/move/attack/stop, boss gate/spawn, and heavy combat hook cooldown dispatch.
- Repair goddess rescue is locked by tests for cargo consumption, full-health recovery, legacy invulnerability, non-clearing of unrelated effects, and the single-line foil tooltip path.
- MorphInventory legacy support buttons are locked by tests for light/heavy ammo refill, grudge refill, aura-effect toggle, show-held-item toggle, item consumption, and legacy exp/readout exposure on the restored menu.
- Phase 10 resource closure is locked by tests through packaged resource checks and DeskReference pages: every required single-player resource has a discoverable source, every referenced data-pack file is packaged, DeskReference exposes the same source lines, 1.12 default chest loot injection is locked to `ConfigLoot`, polymetal worldgen data keeps the old base/ocean counts plus gravel radius/targets, and default `ModOres` ore dictionary entries are represented as Forge tags.
- Phase 8 registered ship render catalog coverage, registered ship texture existence, registered ship model-source parsing, and summon/mount static model sources are locked by tests to parse into nonempty renderable legacy model definitions.
- Phase 8 Large Shipyard BER assets are locked by tests for legacy model-source parseability and lowercase-safe block texture packaging.
- Friendly ship death recovery is locked by tests: dead/dying ships do not open ShipInventory, cargo stays inside the recovered egg, the dropped egg is owner-targeted, non-owner pickup/redeploy is rejected, and owner redeploy restores owner/variant/cargo/health.
- Projectile profile coverage is locked by tests so registered heavy/air-capable ships no longer use a normal-path fallback projectile profile.
- Dev-client boot/resource smoke on 2026-04-23 can reach resource reload, sound engine startup, and texture atlas creation without ShinColle-specific crash, missing-texture, or model-fallback errors in the checked logs. Desk block interaction path has previously been smoke-tested in client, and the user reported the latest manual client pass found no obvious issues after the death/recovery fixes.

## Modern Self-Created Logic Audit

| Surface | 1.12 truth source | Modern action |
| --- | --- | --- |
| Ship egg/class-id lookup | `ID.ShipClass`, `ShipSpawnEgg`, `ShipCalc.rollShipType`, hostile drop egg meta `class + 2` | Unknown egg meta and unknown egg item paths now fail fast instead of silently resolving to a default spec. `smallegg`, `largeegg`, and shipyard construction eggs use the 1.12 small/large roll tables. |
| Friendly counterpart mapping | 1.12 ship class name map plus recovered/drop ownership flow | Hostile-to-friendly counterpart resolution is explicit only; the old archetype-based modern fallback has been removed and is guarded by GameTest. |
| Attack profile routing | `ShipStateHandler` default attack flags, `AttackHandler`, `BasicEntityShipCV`, `BasicEntityShipHostileCV`, `MissileData` | Archetype-only modern attack splits were removed. Registered ships keep 1.12 light/heavy defaults; air attacks are limited to legacy CV inheritance; heavy missile data uses 1.12 speed, arc, height, and lifetime values. |
| Build roll failure path | `ShipCalc` / `EquipCalc` `sumProb = 0.0125F` probability loops | Normal probability tables must select from the 1.12 candidates. Modern random/first-candidate fallbacks were replaced with data-corruption exceptions. |
| Equipment tooltip text | `BasicEquip.addInformation` | The modern-only equipment-status tooltip and language keys were removed from modern resources. |
| Model/render source loading | 1.12 `Model*.java`, legacy renderer resource names | Registered models and static renderer models now throw on missing/unparseable 1.12 sources instead of returning synthetic geometry. |
| External compat | 1.12 optional Metamorph/Baubles/IC2 integration boundaries | Kept framework-only by policy; no third-party API behavior is filled in and no hard dependency is added. |

## What Is Still Only Code-Checked

- Fresh in-world client-side smoke for the new Phase 5 typed ship commands: Pointer move/guard/attack/stop/sit/open-inventory and ShipInventory stop/AI flags/follow range.
- Deeper client-side visual smoke for rider/morph/mount skill HUD and `1~5` / `Z/X/C` input under longer sessions.
- Deeper client-side visual smoke for heavy/air projectile FX, first Phase 7 special combat hook FX, reaction presentation, registered ship model assets, and BERs under longer sessions.
- Real multiplayer-style client smoke for recovered egg pickup/redeploy. The owner gate is server-side GameTest-covered, but a two-client/manual pickup pass is still not recorded.
- Target wrench pairing loop and ship tank interactions in a real client. The route/logistics/server behavior is GameTest-covered.
- No known non-intermod placeholder item/block interaction messages remain in the source scan; keep a manual client text pass on the smoke checklist.
- Equipment rendering and tooltip presentation in a real client.
- ShipInventory supply readout and direct support-item refill feedback in a real client.
- MorphInventory support-item refill feedback, aura/show-held toggles, and exp readout in a real client.

## Residual Exact-Parity / Manual Validation Backlog

- Multiplayer ally UI and old team alliance edge cases still need a real multiplayer-style smoke pass. Server-side team/formation/owner gates are covered by tests.
- Full legacy GUI packet pages and every old ship/desk packet-heavy screen still need an exact 1.12 screen-by-screen audit beyond the current modern Desk, Formation, ShipInventory, route, morph, and shipyard pages.
- Exact per-ship rare runtime/combat gimmicks beyond the current catalog coverage still need a final 1.12 class-by-class audit.
- Model-accurate per-ship poses/layers, full legacy BER fidelity, old reaction pages, and every historical projectile/weapon visual edge case still need deep client validation.
- Remaining legacy worldgen edge behavior, optional ore-dictionary aliases such as `Polymetal_as_Mn`, and historical resource compatibility details still need an exact data-pack audit and live terrain validation. The old default dungeon/chest loot distribution and default `ModOres` tag mappings are now code- and GameTest-covered.
- Real external inter-mod API integrations are intentionally excluded; `MorphCompatBridge` and compat loaders remain soft framework/degradation paths only.

## Immediate Next Targets

1. Run a real in-world client smoke pass focused on spawning one friendly ship and one hostile/boss enemy, then verifying Pointer commands, ShipInventory stop / AI flags / follow range, rider/morph inputs, recovered egg owner gate, and visible combat FX.
2. Run a local multiplayer-style permission smoke for ally/team relation, owner-only recovered egg pickup, and ship command authority.
3. Keep external mod integrations at framework-only status unless a separate task explicitly approves adding real third-party API bindings.

## Modern Source Snapshot

- Modern Java files: `174`
- Major slices:
  - `advancement`: 2
  - `registry`: 7
  - `item`: 23
  - `block`: 8
  - `blockentity`: 13
  - `entity`: 27
  - `ownership`: 1
  - `menu`: 16
  - `client`: 26
  - `crafting`: 6
  - `network`: 14
  - `sound`: 2
  - `teitoku`: 7
  - `team`: 2
  - `formation`: 1
  - `morph`: 9
  - `playerskill`: 3
  - `world`: 5
  - `combat`: 1
  - `gametest`: 1
