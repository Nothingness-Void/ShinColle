# SHIP-04 Frozen Protocol

## Objective

以旧版 Airfield Hime、Battleship Hime、Destroyer Hime、Harbour Hime、Isolated Hime 的实体、模型和攻击入口为语义真源，确认现代单人主线能正确选择对应模型、bake 对应旧版模型源、使用正确的投射物/静态效果族，并通过现有 combat/reaction/particle bus 反馈命中和未命中。只做现代侧最小 API 适配和被断言证明的修正。

## Frozen scope

### Legacy semantic sources

- `src/main/java/com/lulan/shincolle/entity/hime/EntityAirfieldHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityBattleshipHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityDestroyerHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityHarbourHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityIsolatedHime.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java` (direct parent required for inherited aircraft selection and launch rules)
- `src/main/java/com/lulan/shincolle/entity/other/EntityProjectileStatic.java` (direct heavy-attack entry for Isolated Hime's type-5 black-hole field)
- `src/main/java/com/lulan/shincolle/entity/other/EntityAbyssMissile.java` (direct heavy-attack entry for Battleship Hime's type-3 cluster payload)
- `src/main/java/com/lulan/shincolle/client/model/ModelAirfieldHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelBattleshipHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelDestroyerHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelHarbourHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelIsolatedHime.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBase.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBaseAdv.java`
- `src/main/java/com/lulan/shincolle/entity/IShipEmotion@Dep.java`
- `src/main/java/com/lulan/shincolle/utility/EmotionHelper.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelRenderer.java`
- `src/main/java/com/lulan/shincolle/client/render/RenderShipEntity.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipCarrierAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipAttackOnCollide.java`
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`
- `src/main/java/com/lulan/shincolle/utility/ParticleHelper.java`
- `src/main/java/com/lulan/shincolle/utility/RenderHelper.java`
- `src/main/java/com/lulan/shincolle/worldgen/ChestLootTable.java`

If method-level comparison proves that a directly called legacy projectile entity or model helper is required, add only that file here and record the reason in `STATUS.md` before reading it. Do not scan unrelated legacy directories.

### Modern implementation sources

- `build.gradle` (the resource-copy task that packages read-only legacy model source files into modern runtime resources)
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAirplaneEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipTakoyakiEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileVisual.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipBeamEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipFloatingFortEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipStaticEffectEntity.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelDefinition.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelSourceParser.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyStaticModel.java`
- `src/modern/java/com/lulan/shincolle/client/model/entity/LegacyShipModel.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderCatalog.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipProjectileRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipAircraftRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipStaticEffectRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java`
- `src/modern/java/com/lulan/shincolle/network/CombatFxDispatcher.java`
- `src/modern/java/com/lulan/shincolle/network/ClientboundSpawnParticlePacket.java`
- `src/modern/java/com/lulan/shincolle/network/GameplayParticleType.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

Only add modern resource files after a source-to-resource lookup proves a missing or incorrect path. Resource changes trigger the resource gate; renderer/model/particle changes trigger the client gate.

## Required comparisons

- Airfield Hime: legacy model geometry/state, aircraft selection and launch visual family, heal/airfield feedback, model bake and renderer lookup.
- Battleship Hime: cluster-heavy missile family, launch order and impact visual, model geometry/state, miss/crit feedback and renderer lookup.
- Destroyer Hime: range/melee attack family, blindness/attack feedback, model geometry/state and renderer lookup.
- Harbour Hime: railgun/static projectile family, attack effect and heal feedback, model geometry/state and renderer lookup.
- Isolated Hime: black-hole/static effect family, dual attack-effect feedback, model geometry/state and renderer lookup.
- Shared boundaries: no silent model fallback, no projectile-family substitution, projectile-style misses remain non-damaging, hostile feedback remains suppressed where the legacy path suppresses it, and client resources resolve to existing modern paths.

## Acceptance assertions

- Each of IDs `21`, `26`, `27`, `28`, and `29` has a `MATCHED`, `CHANGED`, or `BLOCKED` record for model source/bake, attack/projectile family, feedback, and runtime/render boundary.
- Model tests prove each selected `ShipEntitySpec` resolves to the expected legacy model source and that the source parser/bake produces a non-empty model definition without fallback.
- Projectile tests cover the selected heavy, aircraft, railgun, and static-effect paths, including at least one miss boundary and one non-damaging miss result.
- `git diff --check`, JDK 17 `compileJava`, `processResources`, and `runGameTestServer` pass. Resource/model/renderer/client gates are required when those files enter the diff.
- One dev client smoke test after the complete batch confirms friendly/hostile/boss models, selected projectiles, particles and feedback render without crash, missing texture, missing model, or silent fallback logs.
- No legacy source or resource file is modified.

## Frozen non-goals

- Do not alter SHIP-02 command/route/target behavior or SHIP-03 resource mappings.
- Do not change the hostile Boss far-chase policy or player-facing hostile notification policy in this batch unless a selected visual test proves a direct regression.
- Do not widen model/resource reads beyond the listed files and direct source-to-resource lookups.
- Do not claim other `SLICE` rows or Phase 5-10 complete from this batch.
