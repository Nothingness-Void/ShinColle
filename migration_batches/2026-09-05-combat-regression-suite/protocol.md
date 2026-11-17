# Frozen Protocol

Status: `COMPLETED`

## Question

当前 269 个 GameTest 中四个战斗回归失败是否由现代侧偏离 1.12 的 Nagato 士气事件、Tenryuu/Tatsuta 重型技能阶段或 Yamato 双阶段 beam 入口造成？

## Frozen legacy references

- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/handler/ReactionHandler.java`
- `src/main/java/com/lulan/shincolle/handler/ShipReactionHandler.java`
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipNGT.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipNGTMob.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipYMT.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipYMTMob.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTenryuu.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTenryuuMob.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTatsuta.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTatsutaMob.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySubmHime.java`
- `src/main/java/com/lulan/shincolle/entity/other/EntityAbyssMissile.java`

## Frozen modern references

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Scope

- Nagato nearby-target morale event: target class/filter, distance, threshold and per-target morale amount.
- Tenryuu and Tatsuta heavy skill: second-use gate, phase/timer state, level-scaled sweep count, resource and cooldown boundaries.
- Yamato heavy skill: two-phase beam entry, phase state, target validity, resource and cooldown boundaries.
- Submarine Hime light projectile: legacy move-type resolution when the miss branch replaces the target point.
- Only the four named tests and directly exercised shared helpers.

## Acceptance

- [x] Each failure has an exact 1.12 source entry and a recorded modern discrepancy or test-fixture explanation.
- [x] No modern behavior is approximated from the test name alone.
- [x] Hit/miss, phase, cooldown, target filter, morale and resource assertions remain deterministic.
- [x] `git diff --check` passes.
- [x] After the whole batch: `compileJava`, `processResources`, and `runGameTestServer` pass with no required failures.

## Non-goals

- No new combat architecture or protocol.
- No target-range, idle-supply, renderer, projectile-family, GUI, worldgen or unrelated ship changes.
- No legacy source/resource edits.
- No client startup; this batch has no client/resource changes.
