# Batch Status

- 状态：`COMPLETE`
- 开始：2026-09-03
- 最近更新：2026-09-03
- 当前阻塞：无

## Completed work

- 已冻结本批次问题、范围、legacy 入口、modern 入口和验收断言。
- 对照 `EntityDestroyerI/Ro/Ha/Ni` 与 `EntityCARi`，补齐旧版 128 tick 的弹药补给、待机/移动 grudge、最低士气消耗和运行时位置账本。
- 按旧版 `ShipInventoryHandler.consumeGrudgeItem()` 修正 grudge 恢复优先级：普通物品之后消费 `BlockGrudge`，不把现代 `GRUDGE1` 当作旧版方块替代物。
- 将四个驱逐舰与重巡 Ri 的旧版攻击延迟公式恢复为无额外最小值钳制的版本。
- 为五个 class ID 增加攻击节奏、资源消耗和 NBT 保存/读取回归断言，并覆盖普通 grudge 与 `BlockGrudge` 的恢复路径。
- 完成本批次最终 T0、T1、T2：静态检查、编译、资源处理和 250 个 GameTest 全部通过。

## Remaining work

- 其他舰种的 128 tick 资源语义仍保持在各自矩阵行，不能由本批次的共享 gate 推断已完成。
- 下一批按 [ROADMAP.md](../../ROADMAP.md) 进入 `SHIP-02`，处理下一组舰种的命令、路线和目标边界。
- 客户端渲染、GUI 和投射物不在本批次范围，留给对应批次统一烟测。

## Evidence links

- [results/](results/)
- [decision.md](decision.md)
