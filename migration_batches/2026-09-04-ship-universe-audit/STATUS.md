# Batch Status

- 状态：`COMPLETE; ACTIVE UNIVERSE MATCHED`
- 开始：2026-09-04
- 最近更新：2026-09-04
- 当前阻塞：无

## Completed work

- 已冻结旧版注册表、历史名称映射、现代规格和矩阵的最小读取范围。
- 旧版 `ModEntity.regShipEntity` 共 64 个注册行：45 个 friendly 行和 19 个 hostile 行；全部对应实体源文件存在。
- 现代 `ShipEntitySpecs` 共 64 个规格行，覆盖同一组 45 个基础 class ID 和 19 个 hostile 变体；矩阵 45 行与两者差集均为空。
- `ID.java` 中 40 个只有名称映射、没有实体文件或注册入口的 class ID 已单独列为历史来源缺口；Git 可见历史中也没有这些实体实现。

## Remaining work

- 没有活动旧版实体集合的剩余核对项。40 个历史名称需要外部/恢复的旧版实现来源后，才能另开迁移批次。

## Evidence links

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/ship-universe-audit.json](results/ship-universe-audit.json)
