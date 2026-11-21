# Batch Decision

- 结论：`DONE`（12 项审计全部复核为真实偏离；其中第 10 项“配置项缺失”只完成与本模块运行时直接相关的子集，其余进入 backlog）
- 影响矩阵：不影响 [MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md) 逐舰行；影响 M9 世界内容、掉落与配方。
- 关联模块：crafting / world / config / advancement。

## Facts established

- 1.12.2 `LargeRecipes.getBuildResultEquip` 直接 `rollEquipType(1)` + `rollEquipsOfTheType(...,1)`，没有装备/弹药概率门；`rollEquipsOfTheType` 仅在小建时给 `totalMat` 赋值，大建 `totalMat = 0`，同一方法内按材料总量求 `enchLv` 并调用 `EnchantHelper.applyRandomEnchantToEquip`。类型为 `-1` 时返回 `null`（modern 返回 `ItemStack.EMPTY`）。
- 1.12.2 `LargeRecipes.addMaterialStock` 只在任一 `matStock > MAX_STOCK` 时拒绝；`IShipResourceItem`（资源物品、全部 `BasicEquip`，即使价值为 0 也接受）、`ShipSpawnEgg`（`getKaitaiItems(meta-2)`，`-2`/`-1` 为小/大型蛋，switch 无对应 case 时返回 `false`）与重怨念块均可投入，easyMode 全部 ×10；`TileMultiGrudgeHeavy` easyMode 放出 90/10。
- 1.12.2 宝箱条目来自 `config/shincolle/loottable.cfg`（`ConfigLoot`），`Item.getByNameOrId` 经 `ResourceLocation` 小写化，默认 `shincolle:Torpedo` 解析为 `null`（注册名为 `EquipTorpedo`）被跳过；rolls `RandomValueRange(1, size/2+1)` 为整数除法，`size` 为成功解析的条目数；`chance` 为百分比 ×0.01。
- 1.12.2 `spawnMobShip`：`Check_Ring` 开关、`mobSpawn[0..4]`（上限/概率/组数/每组最少/最多），找到水面锚点即消耗一组；`spawnBossShip` 护卫 `initAttrs(rng.nextInt(2))`（50% 精英），Boss 始终要求婚戒。
- 1.12.2 `generateSea`：每个海洋区块一次 `new WorldGenPolyGravel(2 + rand(2))`，尝试 `polyGravelBaseRate` 次，`getTopSolidOrLiquidBlock` 定位海底 +1；冰封海洋向下找底；半径 `rand(num-1)+1`；替换目标按 `polyGravelBaseBlock`。
- 1.12.2 `EventHandler.onDrop`：`IMob`/`EntitySlime`/`EntityGolem`（含 `BasicEntityShipHostile`）在 `doMobLoot` 下按 `DropRate_Grudge` 掉怨念；`BasicEntityItem` 船蛋掉落物 `Despawn_Egg` tick 后消失。
- 1.12.2 无任何 advancement/achievement。
- 本批涉及的 legacy 代码均可编译，未需参照 `0aad501`。

## Code changes

- [LegacyEquipmentBuildHelper.java](../../src/modern/java/com/lulan/shincolle/crafting/LegacyEquipmentBuildHelper.java)：删除大建 `equipRate` 与 AMMO2/AMMO3 兜底；大建实例 roll 均值使用 0；新增 `enchantLevel`/`enchantType`/`applyRandomEnchant`（旧版 EnchantTable 数字 ID 映射到 1.20.1 附魔）；小建类型 `-1` 时返回空（与旧版 `null` 一致）。
- [LargeShipyardRecipes.java](../../src/modern/java/com/lulan/shincolle/crafting/LargeShipyardRecipes.java)、[HeavyGrudgeBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/HeavyGrudgeBlockEntity.java)、[LegacyShipBehaviorCatalog.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java)：`addMaterialStock(stock, stack, random)` 按旧版顺序处理装备回收（逐类 `getResourceValue` 随机表）、船蛋解体（`createLegacyShipyardKaitaiDrops`）、资源物品与重怨念、easyMode ×10、`current > MAX_STOCK` 判定；放出数量 easyMode 90/10。
- [LegacyLootConfig.java](../../src/modern/java/com/lulan/shincolle/config/LegacyLootConfig.java)（新）、[ChestLootInjector.java](../../src/modern/java/com/lulan/shincolle/world/ChestLootInjector.java)：恢复 `loottable.cfg`（旧版路径、格式与默认内容）；按小写 legacy 名称 + meta 解析为 1.20.1 注册物品，解析失败跳过；rolls 整数除法；随机 meta 拆分为现代变体时对整池权重乘以变体数的最小公倍数，保持旧版单条目概率与组内均匀分布；鱼雷条目因旧版无法解析而不再出现。
- [HostileEncounterSpawner.java](../../src/modern/java/com/lulan/shincolle/world/HostileEncounterSpawner.java)、[HostileEncounterTable.java](../../src/modern/java/com/lulan/shincolle/world/HostileEncounterTable.java)：`Check_Ring`、`Limit_MobSpawnNumber`（含每组 `min~max`）接入；新增 `escortProfile`（50% 精英）。
- [ShinColleConfig.java](../../src/modern/java/com/lulan/shincolle/config/ShinColleConfig.java)：新增 `Check_Ring`、`Limit_MobSpawnNumber`、`DropRate_Grudge`、`Despawn_Egg`、`Tile_SmallShipyard`、`Tile_LargeShipyard`、`Tile_VolCore`、`Polymetal_Gravel`、`Polymetal_Gravel_Replace`（键名沿用 1.12.2）。
- [SmallShipyardBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/SmallShipyardBlockEntity.java)、[HeavyGrudgeBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/HeavyGrudgeBlockEntity.java)、[LegacyCoreBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/LegacyCoreBlockEntity.java) 与两个 shipyard menu：最大燃料、建造速度、燃料倍率（大建物品燃料与岩浆同样乘倍率，对齐 `TileEntityHelper`）、火山核心容量/消耗/单怨念燃料值改为读取配置。
- [MobGrudgeDropEvents.java](../../src/modern/java/com/lulan/shincolle/world/MobGrudgeDropEvents.java)（新）：移植 `EventHandler.onDrop` 怨念掉落。
- [LegacyShipEntity.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java)：敌舰船蛋掉落物 `lifespan` 使用 `Despawn_Egg`（`-1` 不消失）。
- [PolymetalGravelFeature.java](../../src/modern/java/com/lulan/shincolle/world/PolymetalGravelFeature.java)、[ModFeatures.java](../../src/modern/java/com/lulan/shincolle/registry/ModFeatures.java)（新）及 `polymetal_gravel` configured/placed feature JSON：以 1.20.1 `Feature` 移植 `generateSea` + `WorldGenPolyGravel`（`OCEAN_FLOOR_WG` 对应 `getTopSolidOrLiquidBlock`；扁平化前 stone/sand/dirt 的 meta 变体一并计入）。
- 删除 `advancement/ModCriteriaTriggers.java`、`advancement/FriendlyShipDeployedTrigger.java`、`data/shincolle/advancements/progression/*.json`（10 个）与 en_us/zh_cn 中 20 个 `advancement.shincolle.*` 键，清理 `ShinColle`、`LegacyShipSpawnEggItem` 调用。
- [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java)：删除 `progressionAdvancementsLoadAndFriendlyShipTriggerAwards`；宝箱测试改为断言跳过 `shincolle:Torpedo` 与整数 rolls；多金属砂砾测试改为断言移植 feature 与配置默认值。

## 撤销的旧决定

- [WORLDGEN-AUDIT](../2026-09-16-worldgen-audit/decision.md) 中“`minecraft:disk` + `height_range 3..55` 近似 `WorldGenPolyGravel`（半径 `1-2` 视为 ≈）”与“`ChestLootInjector` 无需变更”的结论：已撤销，见 [MIGRATION-DECISIONS.md](../../MIGRATION-DECISIONS.md) D-012。
- 宝箱测试中“随机 meta 平分权重并拆分条目”“保留 `shincolle:Torpedo` 映射”的断言：已撤销。

## Follow-up

- 其余 1.12.2 配置项仍未接回：`Attrs_Scale`/`Attrs_Limit`/`Attrs_Hostile_*`、`Consume_*`、`Attack_Base_Speed`/`Attack_Fixed_Delay`、`Exp_Gain`/`Exp_Gain_Task`、`Slots_*`、`Mode_ChunkLoader`、`Death_Time`、`Mod_IC2`/`Drum_EU`、`Item_Grudge`/`Item_Ammo`、`Polymetal_Ore`（多金属矿目前仍为 placed feature 固定计数 7/+14）。这些项分散在舰船属性/战斗/经验/容器与 inter-mod 模块，超出本模块单批可验证范围，保持 D-008 backlog。
- `BasicEntityItem` 其余行为（漂浮、免疫等）未在本批移植，仅接回 `Despawn_Egg`。
