# SHIP-07 Frozen Protocol

## Objective

以旧版 Akagi、CA Hime、Akatsuki、Hibiki 和 Ikazuchi 的实体、AI、攻击、骑乘和模型入口为语义真源，确认现代单人主线保留五行的航母恢复、夜战攻击、燃料 dismount、骑乘状态、攻击节奏、目标边界、runtime save 和可见反馈。只做现代侧最小 MC API 适配和被断言证明的修正，不把普通舰种逻辑复制成新的分派体系。

## Legacy semantic sources

只列出回答本批次问题必须读取的旧版文件、类、方法或资源路径：

- `src/main/java/com/lulan/shincolle/entity/carrier/EntityCarrierAkagi.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCAHime.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerAkatsuki.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerHibiki.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerIkazuchi.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerInazuma.java` (Akatsuki directly checks its rider type, placement and dismount path)
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityAirplane.java` (Akagi inherits its aircraft construction contract through the carrier base)
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplane.java` (Zero's inherited initialization, launch, and return contract)
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneTakoyaki.java` (T's inherited initialization, launch, and return contract)
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneZero.java` (directly constructed by Akagi for light aircraft)
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneT.java` (directly constructed by Akagi for heavy aircraft)
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipCarrierAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipAttackOnCollide.java`
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`
- `src/main/java/com/lulan/shincolle/client/render/RenderShipEntity.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelRenderer.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBase.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBaseAdv.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCarrierAkagi.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCAHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelDestroyerAkatsuki.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelDestroyerHibiki.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelDestroyerIkazuchi.java`

If method-level comparison proves that a directly called legacy projectile, equipment, summon or hostile variant is required, append only that file here and record the reason in `STATUS.md` before reading it. Do not scan unrelated legacy directories.

## Modern implementation sources

只列出预计修改或验证的现代文件：

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStats.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAirplaneEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFollowOwnerGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangedAttackGoal.java`
- `src/modern/java/com/lulan/shincolle/client/model/entity/LegacyShipModel.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelSourceParser.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipModelStateCatalog.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipAircraftRenderer.java`
- `src/modern/java/com/lulan/shincolle/network/CombatFxDispatcher.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

Only add modern resource files after a source-to-resource lookup proves a missing or incorrect path. Resource changes trigger the resource gate; renderer/model/particle/client changes trigger the client gate.

## Required comparisons

- Akagi: carrier inheritance, aircraft selection/capacity/recovery, Zero/T behavior, owner relationship, voice/feedback gate and model state.
- CA Hime: night attack critical/triple path, push/impact behavior, hostile ownership boundary, cooldown/resource result and model state.
- Akatsuki: rider state, fuel-triggered dismount, hostile state, attack cadence and runtime save.
- Hibiki: rider state, fixed eye height/size, hostile scale, attack cadence and runtime save.
- Ikazuchi: rider state, fixed eye height/size, hostile scale, attack cadence and runtime save.
- Shared boundaries: no silent model fallback, invalid/dead target handling, resource/cooldown failure, projectile-style miss semantics, owner/hostile damage isolation and rider cleanup.

## Acceptance assertions

- Each of IDs `48`, `49`, `51`, `52`, and `53` has a `MATCHED`, `CHANGED`, or `BLOCKED` record for runtime/save, attack/cadence, target/resource boundary and render/feedback.
- Focused tests cover Akagi capacity/recovery, CA Hime night critical/triple behavior, and all three destroyer rider/fuel/eye-height/hostile boundaries.
- At least one test covers invalid/dead target, one covers resource or cooldown failure, and one covers rider or auxiliary cleanup.
- `git diff --check`, JDK 17 `compileJava`, `processResources`, and `runGameTestServer` pass. Resource/model/renderer/client gates are required if those files enter the diff.
- If model, renderer, particle or client code changes, one dev client smoke after the complete batch must confirm the selected friendly/hostile/boss entities and effects load without ShinColle crash, missing texture, missing model or silent fallback logs.
- No legacy source or resource file is modified.

## Frozen non-goals

- Do not alter SHIP-01 through SHIP-06 locked behavior unless a selected source proves a direct regression.
- Do not generalize these five rows into a new AI architecture or move per-ship dispatch into unrelated shared lifecycle code.
- Do not change the hostile Boss far-chase/player-notification policy in this batch without a direct selected-row regression.
- Do not claim other `SLICE` rows or Phase 5-10 complete from this batch.
