# Worldgen Audit Decision

> 已撤销（2026-11-22）：本批对 `polymetal_gravel` disk 近似与 `ChestLootInjector` 无需变更的结论已被 [PARITY-FIX-CRAFTING-WORLD-CONFIG](../2026-11-22-parity-fix-crafting-world-config/decision.md) 推翻，见 MIGRATION-DECISIONS D-017。

- 结论：`DONE`（资源已于 2026-09-16 对齐；B4 配套 GameTest 断言已于 `GAMETEST-REPAIR-B4` 同步修正并通过 `91/91`）
- 影响模块：M9 世界内容
- 代码变更：仅修正 `polymetal_gravel.json` 的目标方块列表；配套测试 `worldgenResourcesKeepLegacyPolymetalPlacement` 已在 [GAMETEST-REPAIR-B4](../2026-11-16-gametest-repair/decision.md) 同步更新并通过。

## Facts established

### Polymetal Ore（✅ 已完整实现）

| 参数 | 旧版 (ConfigHandler 默认) | 现代 (placed_feature JSON) | 匹配 |
|---|---|---|---|
| 生成次数 | `polyOreBaseRate = 7` | `count: 7` | ✅ |
| 矿脉大小 | `4 + rand.nextInt(4)` = 4-7 | `size: 7` | ≈（上限，API 差异可接受）|
| Y 范围 | `minY=3, maxY=50` | `min_inclusive: 3, max_inclusive: 50` | ✅ |
| 维度 | Overworld + default (case 0 + default) | `#minecraft:is_overworld` | ✅ |
| Ocean 加成 | `spawnN *= 3` (3× rate) | base 7 + ocean_bonus 14 = 21 total | ✅ (7×3=21) |
| Nether/End | 注释掉，不生成 | biome modifier 不包含 | ✅ |
| 替换目标 | `base_stone_overworld` (WorldGenMinable 默认) | `tag: minecraft:base_stone_overworld` | ✅ |

### Polymetal Gravel（🔧 修正目标方块）

| 参数 | 旧版默认 | 现代修正前 | 现代修正后 | 匹配 |
|---|---|---|---|---|
| 半高度 | `b0 = 1` | `half_height: 1` | 不变 | ✅ |
| 半径 | `2 + rand.nextInt(2)` = 2-3 | `radius: 1-2` | 不变 | ≈（disk API 差异）|
| 生成次数 | `polyGravelBaseRate = 4` | `count: 4` | 不变 | ✅ |
| Y 范围 | `y <= 55` | `max_inclusive: 55` | 不变 | ✅ |
| 限海洋 | `BiomeDictionary.OCEAN` | `#minecraft:is_ocean` | 不变 | ✅ |
| 水中 | `Material.WATER` 检查 | `matching_fluids: water` | 不变 | ✅ |
| 目标方块 | `{true,true,false,false}` = stone, gravel | stone, gravel, sand, dirt | **stone, gravel** | 🔧→✅ |

**修正**：`polymetal_gravel.json` 的 `target.blocks` 从 `[stone, gravel, sand, dirt]` 修正为 `[stone, gravel]`，匹配旧版 `ConfigHandler.polyGravelBaseBlock` 默认值 `{true, true, false, false}`。

### Chest Loot Injector（✅ 已完整实现）

| 箱子类型 | 旧版 chest ID | 现代 ResourceLocation | 条目数匹配 |
|---|---|---|---|
| Spawn Bonus | 0 | `chests/spawn_bonus_chest` | ✅ 3 entries |
| Igloo | 1 | `chests/igloo_chest` | ✅ 4 entries |
| Dungeon | 2 | `chests/simple_dungeon` | ✅ 6 entries |
| Village Blacksmith | 3 | `chests/village/village_weaponsmith` | ✅ 4 entries |
| Mineshaft | 4 | `chests/abandoned_mineshaft` | ✅ shared |
| Pyramid | 5 | `chests/desert_pyramid` | ✅ shared |
| Jungle Temple | 6 | `chests/jungle_temple` | ✅ 4 entries |
| Nether Bridge | 7 | `chests/nether_bridge` | ✅ 5 entries |
| Stronghold | 8 | `chests/stronghold_*` (3 tables) | ✅ shared |
| End City | 9 | `chests/end_city_treasure` | ✅ shared |

Pool 参数：`rolls = 1 ~ N/2+1`、`bonus_rolls = 1`，与旧版 `addNewPoolToTable` 匹配。

旧版 `SetMetadata(-1)` 随机 meta → 现代使用 `legacyRandomMeta()` 展开为独立注册物品，权重按比例分配。

## Code changes

- `src/modern/resources/data/shincolle/worldgen/configured_feature/polymetal_gravel.json`：移除 `minecraft:sand` 和 `minecraft:dirt`，保留旧版默认的 `minecraft:stone` 和 `minecraft:gravel`

## No-change audits

- Polymetal ore configured/placed feature 和 biome modifier 无需变更
- ChestLootInjector.java 无需变更
- 所有 placed feature 的生成参数和 biome modifier 的 biome 筛选均与旧版默认一致

## Follow-up

- 旧版 `ConfigHandler` 的 `polyOreBaseRate`、`polyGravelBaseRate`、`polyGravelBaseBlock` 运行时 config 在 1.20 由 data pack override 替代，不再提供运行时 GUI 配置；如需恢复用户可配置性，可在后续 config 批次中添加 `ForgeConfigSpec` 选项
- 矿脉大小（旧版 4-7 随机 vs 现代固定 7）是 1.20 ore feature API 不支持 per-chunk 随机 size 的已知 API 差异，不构成行为修改
- `GameplayParityGameTests.worldgenResourcesKeepLegacyPolymetalPlacement` 已于 2026-11-16 `GAMETEST-REPAIR-B4` 同步改为校验 `{true, true, false, false}` 默认值（`stone`, `gravel`）并排除 `sand`/`dirt`/`clay`，实测通过 `All 91 required tests passed`。
- `ModOres` 与 `InventoryHelper` 的 OreDictionary → Forge Tag 逻辑层核对继续归入 `B5-ORE-TAGS`。
