# Batch: WORLDGEN-AUDIT

## Question

这个批次只回答：旧版 `ShinColleWorldGen`、`WorldGenPolyGravel`、`ChestLootTable`、`ConfigLoot` 与 `ModWorldGen` 的默认生成和战利品语义，是否已在现代 data pack worldgen 与 `ChestLootInjector` 中正确覆盖。

## Scope

- 舰种/class ID：无（世界生成与战利品系统，M9）
- 行为族/模块：worldgen、chest loot（M9）
- 旧版入口：见 [protocol.md](protocol.md)
- modern 入口：见 [protocol.md](protocol.md)

## Out of scope

- 运行时 GUI 配置界面与旧版 `ConfigHandler` worldgen 动态开关
- `ModOres` / `InventoryHelper` 的代码侧 OreDictionary → Forge Tag 审计（留待 `B5-ORE-TAGS`）

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
