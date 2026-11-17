# Batch: B5-ORE-TAGS

## Question

这个批次只回答：对照旧版 1.12.2 的 `ModOres`、`ModRecipes`、38 个含 `ore_dict` 的 JSON 配方、`InventoryHelper`、`TileEntityCrane`、`EventHandler` 与 `ShinColle`，现代 1.20.1 的 `forge:tags` / `shincolle:tags`、合成配方与运行期标签匹配（含起重机与任务物品匹配）存在哪些遗漏，以及如何严格按 1.12.2 语义补全并完成 GameTest 闭环。

## Scope

- 舰种/class ID：无（物品/方块 Tag、合成配方与 Crane / Task 运行期匹配，M4 / M9）
- 行为族/模块：OreDictionary → Forge Tags、配方标签输入、Crane 过滤切换与运行期 Tag 匹配
- 旧版入口：见 [protocol.md](protocol.md)
- modern 入口：见 [protocol.md](protocol.md)

## Out of scope

- `GAMETEST-BACKFILL`（补回早期缺失的 per-ship 回归用例）
- B1 GUI/Menu 其余页面交互、`B2` 全量网络包审计、`B3` 多人队伍、`B6` 跨 mod API（含 1.12 `polyAsMn` 动态配置 UI，若涉及仅作静态/最小适配决策说明）

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
