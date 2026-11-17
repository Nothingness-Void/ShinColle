# SHIP-03 Frozen Protocol

## Objective

对照旧版 `EntityTransportWa`、`EntitySubmKa`、`EntitySubmYo`、`EntitySubmSo`、`EntityCarrierHime` 的战斗行为入口，确认现代共享实体是否保留同样的攻击选择、攻击节奏、命中结果、资源消耗、召唤/恢复和掉落语义。只做现代侧最小 API 适配和已被断言证明的修正。

## Frozen scope

### Legacy semantic sources

- `src/main/java/com/lulan/shincolle/entity/transport/EntityTransportWa.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmKa.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmYo.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmSo.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCarrierHime.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostileCV.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipAttackOnCollide.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipCarrierAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipAircraftAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipSkillAttack.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeTarget.java`
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/handler/ShipInventoryHandler.java`
- `src/main/java/com/lulan/shincolle/config/ConfigLoot.java`
- `src/main/java/com/lulan/shincolle/worldgen/ChestLootTable.java`

If method-level comparison proves a directly called legacy helper is required, add only that helper to this protocol and record the reason in `STATUS.md` before reading it. Do not scan unrelated legacy directories.

### Modern implementation sources

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackKind.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAirplaneEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/HostileRuntimeState.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangedAttackGoal.java`
- `src/modern/java/com/lulan/shincolle/world/ChestLootInjector.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

Only add a modern file when a frozen source directly owns the behavior or a test is needed to prove it. Keep per-ship dispatch in `LegacyShipBehaviorCatalog` or an existing same-level helper.

## Required comparisons

- Transport Wa: attack eligibility, supply/attack interaction, cooldown/resource consumption, death/drop path and runtime save fields relevant to the mainline.
- Submarine Ka/Yo/So: underwater/range attack selection, attack delay, miss/crit calculation, ammunition/grudge consumption, target validity, death/drop path and runtime save fields.
- Carrier Hime: melee/ranged or aircraft attack selection, aircraft/servant release and recovery, boss profile, cooldown/resource consumption, miss/crit result and drops.
- Shared boundaries: no silent fallback to a different attack family, no extra modern-only resource cost, no player-facing hostile combat feedback when the legacy path suppresses it, and heavy special misses must remain misses when the old attack kind is projectile-style.

## Acceptance assertions

- Each of IDs `16`, `17`, `18`, `19`, and `20` has a `MATCHED`, `CHANGED`, or `BLOCKED` record for every required comparison.
- Any changed behavior is implemented through the existing catalog/helper path and has a focused GameTest assertion for attack selection, cadence, result, resource, recovery/summon, or loot.
- Tests cover at least one hit and one miss boundary, cooldown/resource rejection, valid and invalid target, and the applicable transport/submarine/carrier drop or recovery path.
- `git diff --check`, JDK 17 `compileJava`, `processResources`, and `runGameTestServer` pass. Resource/model/client gates are required only if this batch actually changes those files.
- No legacy source or resource file is modified.

## Frozen non-goals

- Do not alter command/route/guard behavior already closed by `SHIP-02`.
- Do not change the hostile Boss far-chase policy in this batch.
- Do not perform a client smoke test unless client/resource/renderer/particle/screen files enter the diff.
- Do not claim Phase 5-10 or any unselected ship row complete from this batch.
