# SHIP-05 Frozen Protocol

## Objective

以旧版 Midway Hime、Northern Hime、Carrier WD、Shimakaze 和 Battleship NGT 的实体、AI、攻击和渲染入口为语义真源，确认现代单人主线正确保留五行的特殊运行时、召唤/装备生命周期、飞机恢复、目标选择和攻击节奏。只做现代侧最小 API 适配和被断言证明的修正，不把普通舰种逻辑复制成新的分派体系。

## Frozen scope

### Legacy semantic sources

- `src/main/java/com/lulan/shincolle/entity/hime/EntityMidwayHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityNorthernHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCarrierWD.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerShimakaze.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerShimakazeMob.java` (direct hostile variant)
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipNGT.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipNGTMob.java` (direct hostile variant)
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipCarrierAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipAttackOnCollide.java`
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`
- `src/main/java/com/lulan/shincolle/utility/ParticleHelper.java`
- `src/main/java/com/lulan/shincolle/utility/RenderHelper.java`
- `src/main/java/com/lulan/shincolle/worldgen/ChestLootTable.java`
- `src/main/java/com/lulan/shincolle/client/render/RenderShipEntity.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelRenderer.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBase.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBaseAdv.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelMidwayHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelNorthernHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCarrierWDemon.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelDestroyerShimakaze.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelBattleshipNagato.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityRensouhou.java` (direct Shimakaze summon)
- `src/main/java/com/lulan/shincolle/entity/other/EntityRensouhouS.java` (direct Shimakaze summon variant)
- `src/main/java/com/lulan/shincolle/entity/other/EntityRensouhouMob.java` (direct hostile Shimakaze summon; required by `EntityDestroyerShimakazeMob`)
- `src/main/java/com/lulan/shincolle/entity/other/EntityFloatingFort.java` (direct Northern Hime heavy summon)

If method-level comparison proves that a directly called legacy model, projectile, servant, or equipment helper is required, add only that file here and record the reason in `STATUS.md` before reading it. Do not scan unrelated legacy directories.

### Modern implementation sources

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAirplaneEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipBeamEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipFloatingFortEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipStaticEffectEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangeTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFollowOwnerGoal.java`
- `src/modern/java/com/lulan/shincolle/client/model/entity/LegacyShipModel.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderCatalog.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipAircraftRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipFloatingFortRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java`
- `src/modern/java/com/lulan/shincolle/network/CombatFxDispatcher.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

Only add modern resource files after a source-to-resource lookup proves a missing or incorrect path. Resource changes trigger the resource gate; renderer/model/particle changes trigger the client gate.

## Required comparisons

- Midway Hime: carrier inheritance, air height, heal/absorption, boss runtime and target boundary.
- Northern Hime: floating-fort lifecycle, mount/dismount, emotion/state handling, attack and removal boundary.
- Carrier WD: friendly carrier role, dual aircraft selection, aircraft capacity/recovery, owner relationship and visual family.
- Shimakaze: five-shot burst, Rensouhou/servant pool, block target and servant removal/lifecycle.
- Battleship NGT: AP strike, morale/engine state, boss color/state and attack cadence.
- Shared boundaries: no silent model fallback, no servant leaks after owner/removal, target selection remains within the old ownership/hostility rules, projectile-style misses remain non-damaging, and hostile player feedback remains isolated.

## Acceptance assertions

- Each of IDs `30`, `31`, `33`, `36`, and `37` has a `MATCHED`, `CHANGED`, or `BLOCKED` record for runtime state, attack/servant behavior, target/cadence, and render/feedback boundary.
- Focused tests cover Midway absorption/heal and air height, Northern floating-fort lifecycle, Carrier WD dual aircraft/recovery, Shimakaze five-shot and servant pool, and Battleship NGT AP/morale/engine/cadence rules.
- At least one test covers invalid/dead target, one covers resource/cooldown boundary, and one covers summoned or auxiliary entity cleanup.
- `git diff --check`, JDK 17 `compileJava`, `processResources`, and `runGameTestServer` pass. Resource/model/renderer/client gates are required if those files enter the diff.
- If model, renderer, particle, or client code changes, one dev client smoke after the complete batch must confirm the selected friendly/hostile/boss entities and auxiliary effects load without ShinColle crash, missing texture, missing model, or silent fallback logs.
- No legacy source or resource file is modified.

## Frozen non-goals

- Do not alter SHIP-01 through SHIP-04 locked behavior unless a selected source proves a direct regression.
- Do not generalize the five rows into a new AI architecture or move per-ship dispatch into unrelated shared lifecycle code.
- Do not change the hostile Boss far-chase/player-notification policy in this batch without a direct selected-row regression.
- Do not claim other `SLICE` rows or Phase 5-10 complete from this batch.
