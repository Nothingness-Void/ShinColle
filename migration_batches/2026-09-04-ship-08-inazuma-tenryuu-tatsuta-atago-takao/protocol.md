# SHIP-08 Frozen Protocol

## Objective

以旧版 Inazuma、Tenryuu、Tatsuta、Atago 和 Takao 的实体、AI、攻击、骑乘、状态和模型入口为语义真源，确认现代单人主线保留五行的 Raiden 解体与重组、巡洋舰技能阶段、夜战/提督效果、重炮攻击、冷却/资源、目标边界、runtime save 和可见反馈。只做现代 MC API 的最小适配和被对照证明的修正，不创建第二套行为分派体系。

## Legacy semantic sources

只列出回答本批次问题必须读取的旧版文件、类、方法或资源路径：

- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerInazuma.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerInazumaMob.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerAkatsuki.java` (Inazuma 的 Akatsuki rider/dismount 依赖)
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTenryuu.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTenryuuMob.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTatsuta.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTatsutaMob.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCAAtago.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCAAtagoMob.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCATakao.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCATakaoMob.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipAttackOnCollide.java`
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`
- `src/main/java/com/lulan/shincolle/client/render/RenderShipEntity.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelRenderer.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBase.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBaseAdv.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelDestroyerInazuma.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCruiserTenryuu.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCruiserTatsuta.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCruiserAtago.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCruiserTakao.java`

If method-level comparison proves that a directly called legacy projectile, effect, equipment or hostile variant is required, append only that file here and record the reason in `STATUS.md` before reading it. Do not scan unrelated legacy directories.

## Modern implementation sources

只列出预计修改或验证的现代文件：

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStats.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipModelStateCatalog.java`
- `src/modern/java/com/lulan/shincolle/client/model/entity/LegacyShipModel.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelSourceParser.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangedAttackGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFollowOwnerGoal.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

Only add modern resource files after a source-to-resource lookup proves a missing or incorrect path. Resource changes trigger the resource gate; renderer/model/particle/client changes trigger the client gate.

## Required comparisons

- Inazuma: Raiden host/rider state, Akatsuki gattai ordering, no-fuel and damage dismount, fixed eye height, attack cadence and runtime state.
- Tenryuu: dash phase/timer, night critical/dodge refresh, owner effect, target/resource boundary, hostile variant and model state.
- Tatsuta: control phase/timer, area target filtering/effect, night/boss boundary, resource/cooldown and model state.
- Atago: heavy barrage phase, night reset, mounted offsets, hostile scale/boss profile and model state.
- Takao: heavy barrage/reaction effect, mounted offsets, hostile scale/boss profile and model state.
- Shared boundaries: invalid/dead target, cooldown/resource failure, hit/miss/crit, owner/hostile damage isolation, rider cleanup, source bake and no silent fallback.

## Acceptance assertions

- Each of IDs `54`, `56`, `57`, `58`, and `59` has a `MATCHED`, `CHANGED`, or `BLOCKED` record for runtime/save, attack/cadence, target/resource boundary and render/feedback.
- Focused tests cover Inazuma/Raiden rider cleanup, Tenryuu/Tatsuta phase and night effects, Atago/Takao heavy barrage, hostile profiles, target validity and model source/state/bake.
- At least one test covers invalid/dead target, one covers resource or cooldown failure, and one covers rider or auxiliary cleanup.
- `git diff --check`, JDK 17 `compileJava`, `processResources`, and `runGameTestServer` pass; resource/model/renderer/client gates are required if those files enter the diff.
- If model, renderer, particle or client code changes, one dev client smoke after the complete batch must confirm the selected friendly/hostile/Boss entities and effects load without ShinColle crash, missing texture, missing model or silent fallback logs.
- No legacy source or resource file is modified.

## Frozen non-goals

- Do not alter SHIP-01 through SHIP-07 locked behavior unless a selected source proves a direct regression.
- Do not generalize the five rows into a new AI architecture or move per-ship dispatch into unrelated lifecycle code.
- Do not change the hostile Boss far-chase/player-notification policy in this batch without a direct selected-row regression.
- Do not claim other `SLICE` rows or Phase 5-10 complete from this batch.
