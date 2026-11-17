# GAMETEST-REPAIR-B4 Protocol

- 批次 ID：`GAMETEST-REPAIR-B4`
- 开始：2026-11-16
- 目标：逐个对照旧版 1.12.2 源码真源，核实并修复当前 `GameplayParityGameTests.java` 失败的 9 个 GameTest（如果是旧测试写错/过时则按 1.12 语义修正测试；如果现代生产代码偏离 1.12 源码则按 1.12 修正现代代码），完成 B4（Worldgen）完整测试闭环。

## 失败清单（范围固定为 9 项）

1. `worldgenResourcesKeepLegacyPolymetalPlacement`（B4 Worldgen 收口）
2. `hostileSpecFlagsMatchLegacySemantics`
3. `shipCommandServiceReportsFailureReasons`
4. `shipCommandServiceRejectsUnauthorizedFarAndDeadShips`
5. `perShipBehaviorCatalogRoutesAttackProfiles`
6. `perShipBehaviorCatalogRoutesSpecialCombatHooks`
7. `hostileBossUsesLegacyPlayerTargetRules`
8. `shipPickupGoalStoresNearbyDrops`
9. `shipPickupGoalYieldsToOrdersCombatAndSupplyFlag`

## 旧版入口（只读真源）

1. Worldgen（B4）：
   - `src/main/java/com/lulan/shincolle/worldgen/ShinColleWorldGen.java`
   - `src/main/java/com/lulan/shincolle/worldgen/WorldGenPolyGravel.java`
   - `src/main/java/com/lulan/shincolle/worldgen/ChestLootTable.java`
   - `src/main/java/com/lulan/shincolle/handler/ConfigHandler.java`（worldgen defaults）
2. Hostile Spec Flags：
   - `src/main/java/com/lulan/shincolle/init/ModEntity.java`
   - `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
   - `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
   - `src/main/java/com/lulan/shincolle/entity/hime/` 对应具体 class 继承关系
3. Pointer / Ship Command Distance & Reasons：
   - `src/main/java/com/lulan/shincolle/item/PointerItem.java`
   - `src/main/java/com/lulan/shincolle/utility/EntityHelper.java`
   - `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`（command distance / move / guard bounds）
4. Attack Profiles & Special Hooks（Transport Wa / CAAtago）：
   - `src/main/java/com/lulan/shincolle/entity/transport/EntityTransportWa.java`
   - `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCAAtago.java`
   - `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java` / `BasicEntityShipSmall.java`
   - `src/main/java/com/lulan/shincolle/handler/ShipStateHandler.java`（if applicable for `StateFlag` defaults）
5. Hostile Boss Target Rules：
   - `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeTarget.java`
   - `src/main/java/com/lulan/shincolle/utility/TargetHelper.java`
   - `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
6. Ship Pickup Goal：
   - `src/main/java/com/lulan/shincolle/ai/EntityAIShipPickItem.java`
   - `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`

## 现代入口

- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`
- `src/modern/resources/data/shincolle/worldgen/` & `src/modern/resources/data/shincolle/forge/biome_modifier/`
- `src/modern/java/com/lulan/shincolle/world/ChestLootInjector.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandService.java`
- `src/modern/java/com/lulan/shincolle/item/PointerItem.java` & `PointerCommandEvents.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipHostilePlayerTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangeTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipPickItemGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`

## 验收断言

- 上述 9 个失败项逐一拥有旧版 1.12.2 源码行级依据，不允许无依据“改测试凑过”。
- B4 Worldgen 的矿脉、海底砾石、生物群系条件及 ChestLootInjector 均有通过的 GameTest 验证。
- `git diff --check`、`compileJava`、`processResources`、`verifyItemModelCoverage`、`runGameTestServer` 全部通过且 0 失败。

## 非目标

- 暂不重构测试以外的无关生产模块。
- 暂不开始 B5（OreDict/Tags）及 B1/B2/B3/B6 实现；B5 在本批通过后紧接着冻结启动。
