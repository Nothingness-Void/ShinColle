# Batch: PARITY-FIX-CRAFTING-WORLD-CONFIG

## Question

这个批次只回答：1.20.1 的大建装备、大建投料、宝箱 loot、敌舰生成、多金属砂砾世界生成、配置项与进度是否严格按 1.12.2 语义实现；审计列出的偏离按旧版还原。

## Scope

- 舰种/class ID：不涉及逐舰行为（仅大建投料船蛋解体复用既有 `ShipCalc.getKaitaiItems` 对照表）。
- 行为族/模块：crafting（大建装备、附魔、投料回收、easyMode）、world（宝箱 loot、敌舰生成、多金属砂砾）、config（Check_Ring、Limit_MobSpawnNumber、DropRate_Grudge、Despawn_Egg、Tile_*、Polymetal_Gravel*）、advancement（删除）。
- 旧版入口：`crafting/LargeRecipes.java`、`crafting/SmallRecipes.java`、`crafting/EquipCalc.java`、`crafting/ShipCalc.java`、`utility/EnchantHelper.java`、`tileentity/TileMultiGrudgeHeavy.java`、`tileentity/TileEntitySmallShipyard.java`、`tileentity/TileEntityVolCore.java`、`config/ConfigLoot.java`、`worldgen/ChestLootTable.java`、`worldgen/ShinColleWorldGen.java`、`worldgen/WorldGenPolyGravel.java`、`utility/EntityHelper.java`（`spawnMobShip`/`spawnBossShip`）、`handler/ConfigHandler.java`、`handler/EventHandler.java`（`onDrop`）、`item/Equip*.java#getResourceValue`、`item/BasicEntityItem.java`。
- modern 入口：见 [decision.md](decision.md) 的 Code changes。

## Out of scope

- 舰船属性缩放、消耗、经验、槽位、ChunkLoader、Death_Time、IC2/Drum_EU、Polymetal_Ore 数量等其余 `ConfigHandler` 配置项（见 decision 的 Follow-up）。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
