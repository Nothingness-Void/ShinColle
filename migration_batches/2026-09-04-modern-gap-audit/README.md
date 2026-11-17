# Batch: 2026-09-04-modern-gap-audit

## Question

这个批次只回答：活动旧版 45 个基础舰种在现代承载层是否仍存在主线阻塞性的占位实现、缺失分派或规格/攻击档案缺口。

## Scope

- 舰种/class ID：活动实体全集的 45 个基础 class ID。
- 行为族/模块：现代规格、攻击档案、行为 catalog、实体协调层的静态完整性。
- 旧版入口：活动舰种的旧版实体目录和 `BasicEntityShip*` 基类。
- modern 入口：`ShipEntitySpecs`、`LegacyShipAttackProfile`、`LegacyShipBehaviorCatalog`、`LegacyShipEntity`、`LegacyShipStatTables`。

## Out of scope

- 不改变行为、不补猜测逻辑、不修改 legacy。
- 不重复运行编译、GameTest 或客户端；本批只做静态扫描并记录需要另开实现批次的确切入口。
- 不把历史 `ID.java` 残留名称当成活动舰种。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/modern-gap-audit.json](results/modern-gap-audit.json)
