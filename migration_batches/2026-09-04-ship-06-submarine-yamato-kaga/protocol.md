# SHIP-06 Frozen Protocol

## Objective

以旧版 class 38、39、44、46、47 的实体、继承链、攻击、AI、模型和辅助实体入口为语义真源，确认现代单人路径保留潜艇敌对战斗/存档、潜艇姬 Boss 运行时、Yamato 目标与 beam 阶段、Kaga 飞机选择/回收和对应视觉边界。只做现代侧最小 MC API 适配，以及由逐项对照和断言证明的缺口修正。

## Frozen scope

### Legacy semantic sources

- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmU511.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmU511Mob.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmRo500.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmRo500Mob.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySubmHime.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipYMT.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipYMTMob.java`
- `src/main/java/com/lulan/shincolle/entity/carrier/EntityCarrierKaga.java`
- `src/main/java/com/lulan/shincolle/entity/carrier/EntityCarrierKagaMob.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostileCV.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipCarrierAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipAttackOnCollide.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipPickItem.java` (U-511/Ro-500/Submarine Hime direct pickup goal)
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`
- `src/main/java/com/lulan/shincolle/utility/ParticleHelper.java`
- `src/main/java/com/lulan/shincolle/utility/RenderHelper.java`
- `src/main/java/com/lulan/shincolle/utility/CalcHelper.java` (submarine launch-offset and Yamato visual coordinate math)
- `src/main/java/com/lulan/shincolle/utility/EntityHelper.java` (projectile host exclusion)
- `src/main/java/com/lulan/shincolle/utility/TargetHelper.java` (projectile invulnerability boundary)
- `src/main/java/com/lulan/shincolle/utility/TeamHelper.java` (same-owner and friendly-fire boundary)
- `src/main/java/com/lulan/shincolle/entity/other/EntityAbyssMissile.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityProjectileBeam.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityProjectileStatic.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplane.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneZero.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneZeroMob.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneT.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneTMob.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplaneTakoyaki.java`
- `src/main/java/com/lulan/shincolle/entity/mounts/EntityMountSuH.java` (Submarine Hime direct mount)
- `src/main/java/com/lulan/shincolle/reference/dataclass/MissileData.java` (direct missile tuning fields)
- `src/main/java/com/lulan/shincolle/client/model/ModelSubmU511.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelSubmRo500.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelSubmHime.java` (class 44's direct model source)
- `src/main/java/com/lulan/shincolle/client/model/ModelSSNH.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelBattleshipYamato.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelCarrierKaga.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBase.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelBaseAdv.java`
- `src/main/java/com/lulan/shincolle/client/model/ShipModelRenderer.java`
- `src/main/java/com/lulan/shincolle/client/render/RenderShipEntity.java`
- `src/main/resources/assets/shincolle/textures/entity/EntitySubmU511.png`
- `src/main/resources/assets/shincolle/textures/entity/EntitySubmRo500.png`
- `src/main/resources/assets/shincolle/textures/entity/EntitySubmHime.png`
- `src/main/resources/assets/shincolle/textures/entity/EntitySubmHimeNew.png`
- `src/main/resources/assets/shincolle/textures/entity/EntityBattleshipYamato.png`
- `src/main/resources/assets/shincolle/textures/entity/EntityCarrierKaga.png`

If method-level comparison proves that another directly called legacy class is required, append only that path and the reason to this protocol and `STATUS.md` before reading it. Do not scan unrelated legacy directories.

### Modern implementation sources

- `build.gradle`
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
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileMoveType.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipBeamEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipFloatingFortEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipStaticEffectEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipModelStateCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipModelTransformCatalog.java` (minimal runtime projection of selected 1.12 scale-level render branches)
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelDefinition.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelSourceParser.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyStaticModel.java`
- `src/modern/java/com/lulan/shincolle/client/model/entity/LegacyShipModel.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderCatalog.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipProjectileRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipBeamRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipAircraftRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipFloatingFortRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipStaticEffectRenderer.java`
- `src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java`
- `src/modern/java/com/lulan/shincolle/network/CombatFxDispatcher.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`
- `src/modern/resources/assets/shincolle/textures/entity/EntitySubmU511.png`
- `src/modern/resources/assets/shincolle/textures/entity/EntitySubmRo500.png`
- `src/modern/resources/assets/shincolle/textures/entity/EntitySubmHime.png`
- `src/modern/resources/assets/shincolle/textures/entity/EntitySubmHimeNew.png`
- `src/modern/resources/assets/shincolle/textures/entity/EntityBattleshipYamato.png`
- `src/modern/resources/assets/shincolle/textures/entity/EntityCarrierKaga.png`

Only modify modern resource files if the frozen source-to-resource comparison proves a missing or incorrect path. Resource changes trigger the resource gate; model, renderer, particle, or client changes trigger the client gate.

## Required comparisons

- U-511: friendly/hostile inheritance, submarine path and speed, invisibility boundary, hostile combat result, and save/runtime defaults.
- Ro-500: U-511-derived rules plus Garuru/auxiliary state, hostile combat result, and save/runtime defaults.
- Submarine Hime: hime submarine movement/path, dual missile selection and launch order, mount/runtime state, boss target validity and failure boundary.
- Yamato: beam family, engine state, hostile cannon path, valid/dead target handling, phase cadence and resource/cooldown result.
- Kaga: carrier inventory, Zero/T selection, aircraft capacity and recovery, owner/hostile boundary, model source and aircraft visual family.
- Shared boundaries: no silent model fallback, no projectile-family substitution, projectile misses remain non-damaging, save/load preserves relevant defaults, invalid/dead targets do not consume resources, and hostile feedback remains isolated wherever the old path suppresses it.

## Acceptance assertions

- Each of IDs `38`, `39`, `44`, `46`, and `47` has a `MATCHED`, `CHANGED`, or `BLOCKED` record for runtime/save, attack/target/cadence, and render/feedback boundary.
- Focused tests cover U-511/Ro-500 hostile combat and defaults, Submarine Hime dual missile and boss edge cases, Yamato target/phase cadence, and Kaga Zero/T capacity/recovery and visual lookup.
- At least one test covers invalid/dead target, one covers resource/cooldown boundary, one covers save/load defaults, and one covers a non-damaging projectile miss.
- Model tests prove the six selected legacy model sources (U-511, Ro-500, Submarine Hime, SSNH, Yamato, Kaga) parse/bake to non-empty definitions and resolve to existing modern texture paths without fallback.
- `git diff --check`, JDK 17 `compileJava`, `cleanProcessResources processResources`, and `runGameTestServer` pass. Because model/renderer/client paths are in scope, one dev client smoke is required after the complete batch.
- No legacy source or resource file is modified.

## Frozen non-goals

- Do not alter SHIP-01 through SHIP-05 locked behavior unless a selected source proves a direct regression.
- Do not generalize these five rows into a new AI architecture or move per-ship dispatch into unrelated lifecycle code.
- Do not change the global hostile Boss far-chase/player-notification policy in this batch without a direct selected-row regression.
- Do not widen legacy reads beyond this list and any explicitly appended direct dependency.
- Do not claim other `SLICE` rows or Phase 5-10 complete from this batch.
