# Frozen Protocol

## Legacy references

只列出为回答本批次问题必须读取的旧版文件、类、方法或资源路径：

- 基类与接口：`src/main/java/com/lulan/shincolle/entity/{BasicEntityShip.java,BasicEntityShipSmall.java,BasicEntityShipCV.java,BasicEntityShipHostile.java,BasicEntityShipHostileCV.java,IShipInvisible.java,IShipEmotion.java,IShipAttackBase.java,IShipMorph.java}`
- AI 与寻路：`src/main/java/com/lulan/shincolle/ai/*`
- 全部 64 个舰船实体类：`src/main/java/com/lulan/shincolle/entity/{destroyer,cruiser,battleship,carrier,submarine,transport,hime}/*`
- 坐骑、召唤物与投射物：`src/main/java/com/lulan/shincolle/entity/{mounts,other}/*`
- 注册、刷怪蛋、建造与战斗工具：`src/main/java/com/lulan/shincolle/{init/ModEntity.java,item/ShipSpawnEgg.java,crafting/ShipCalc.java,utility/CombatHelper.java,utility/CalcHelper.java,utility/BuffHelper.java,handler/EventHandler.java}`
- 客户端渲染与模型状态：`src/main/java/com/lulan/shincolle/client/render/*`、`src/main/java/com/lulan/shincolle/client/model/*`

## Modern references

只列出预计修改或验证的现代文件：

- `src/modern/java/com/lulan/shincolle/entity/ship/{LegacyShipEntity.java,LegacyShipBehaviorCatalog.java,LegacyShipCombatHelper.java,ShipStatsCalculator.java,ShipEntitySpec.java,ShipEntitySpecs.java,ShipArchetype.java,LegacyShipModelStateCatalog.java,LegacyShipModelTransformCatalog.java}`
- `src/modern/java/com/lulan/shincolle/entity/mount/{LegacyShipMountEntity.java,LegacyMountEntity.java}`
- `src/modern/java/com/lulan/shincolle/entity/projectile/*`、`src/modern/java/com/lulan/shincolle/entity/other/*`
- `src/modern/java/com/lulan/shincolle/item/LegacyShipSpawnEggItem.java`、`src/modern/java/com/lulan/shincolle/worldgen/{HostileEncounterTable.java,HostileEncounterSpawner.java}`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/*`、`src/modern/java/com/lulan/shincolle/client/model/**`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [ ] 全 64 个活动舰船变体与基类的行为、数值、冷却、状态位、隐身/发光、坐骑与投射物逐项对照 1.12.2 源码，所有真实偏差全部修复或记录最小 1.20.1 API 适配。
- [ ] 资源消耗、冷却、目标有效性、潜艇隐身/发光、坐骑渲染与模型状态有明确回归断言覆盖。
- [ ] `git status -s -- src/main/` 为空，`git diff --check` 通过。
- [ ] 按 [MIGRATION-TEST-MATRIX.md](../../MIGRATION-TEST-MATRIX.md) 运行 `.\gradlew.bat build verifyItemModelCoverage runGameTestServer --no-daemon` 全部通过。

## Frozen non-goals

- 不改动 `src/main/` 下任何旧版源码或资源。
- 不虚构 `ID.java` 中无 1.12.2 实体源码的 40 个 `HISTORICAL_SOURCE_GAP` 角色。
- 未经用户明确指令，不自动执行 `git commit`、`git push` 或 GitHub Release 更新。
