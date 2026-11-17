# Worldgen Audit Protocol

- 批次 ID：`WORLDGEN-AUDIT`
- 开始：2026-09-16
- 目标：对照旧版 `ShinColleWorldGen`、`WorldGenPolyGravel`、`ChestLootTable`、`ModWorldGen` 的完整 worldgen 语义，确认现代侧 configured/placed feature、biome modifier 和 ChestLootInjector 的覆盖度。

## 旧版入口

- `ShinColleWorldGen.java`：`IWorldGenerator`，维度 0 和 default 生成 Polymetal ore 和 PolyGravel
- `WorldGenPolyGravel.java`：海底 disk 生成，default 替换 stone+gravel（sand/dirt 默认关闭）
- `ChestLootTable.java`：10 个箱子类型的战利品注入
- `ConfigHandler`：`polyOreBaseRate=7`，`polyGravelBaseRate=4`，`polyGravelBaseBlock={true,true,false,false}`
- `ConfigLoot.java`：外部配置文件解析（默认值硬编码）
- `ModWorldGen.java`：`GameRegistry.registerWorldGenerator`

## 现代入口

- `configured_feature/polymetal_ore.json`：`minecraft:ore`，size=7
- `configured_feature/polymetal_gravel.json`：`minecraft:disk`，half_height=1，radius 1-2
- `placed_feature/polymetal_ore.json`：count=7，Y 3-50
- `placed_feature/polymetal_ore_ocean_bonus.json`：count=14，Y 3-50
- `placed_feature/polymetal_gravel.json`：count=4，Y 3-55，water filter
- `forge/biome_modifier/add_polymetal_ore.json`：`#minecraft:is_overworld`
- `forge/biome_modifier/add_polymetal_ore_ocean_bonus.json`：`#minecraft:is_ocean`
- `forge/biome_modifier/add_polymetal_gravel.json`：`#minecraft:is_ocean`
- `ChestLootInjector.java`：10 个箱子类型，全部默认 loot entry

## 验收断言

- Polymetal ore 参数匹配旧版 ConfigHandler 默认值
- Ocean 3× 语义通过 base + ocean_bonus 两个 placed feature 覆盖
- PolyGravel 目标方块列表只包含旧版默认启用的 stone 和 gravel
- ChestLootInjector 的 10 个箱子 ID 与旧版 ConfigLoot 默认数据一一对应
- Nether/End 不生成（biome modifier 限制 overworld/ocean）

## 非目标

- 运行时 config 文件解析（1.20 使用 data pack 替代）
- 旧版 `polyGravelBaseBlock` 运行时开关（data pack 可覆盖）
