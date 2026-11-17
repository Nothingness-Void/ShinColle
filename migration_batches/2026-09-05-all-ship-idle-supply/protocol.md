# Frozen Protocol

## Question

旧版 `BasicEntityShip.updateConsumeItem()` 对所有继承 `BasicEntityShip` 的具体注册类统一生效；现代是否错误地只让部分 class ID 进入 128 tick 待机/移动补给路径？

## Frozen legacy references

- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostileCV.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityBattleshipHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCarrierHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCAHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityDestroyerHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySubmHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySSNH.java`
- `src/main/java/com/lulan/shincolle/entity/ModEntity.java`
- `src/main/java/com/lulan/shincolle/reference/ID.java`

## Frozen modern references

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`
- `MIGRATION-SHIP-MATRIX.md`

## Scope

- 45 active BasicEntityShip class IDs already listed in the ship matrix, including Hime classes that inherit `BasicEntityShipSmall/CV`.
- The separately registered `BasicEntityShipHostile` Mob variants remain excluded even when their modern combat spec is hostile.
- The old 128 tick movement distance, morale cost, idle grudge cost, normal/container ammo recovery, and one-item grudge recovery that belong to the common friendly base path.
- A focused regression that proves every active BasicEntityShip ID has an old-source-derived idle cost and reaches the common path.

## Acceptance

- [ ] Old base logic is read at the exact method and its friendly/hostile gates are recorded.
- [ ] Every active BasicEntityShip ID has an old-source-derived idle cost; hostile Mob variants remain excluded.
- [ ] Modern code has one shared implementation with no per-ship duplicate.
- [ ] Regression covers all 45 active BasicEntityShip IDs plus hostile Mob exclusion and supply fallback order.
- [ ] `git diff --check` passes.
- [ ] After the whole batch: `compileJava`, `processResources`, and `runGameTestServer` pass once.

## Non-goals

- No changes to legacy sources/resources.
- No new GUI, network protocol, renderer, projectile, hostile target policy, or worldgen behavior.
- No client startup; this batch has no client/resource changes.
- Do not alter per-ship attack costs or special combat hooks.
