# Batch: 2026-09-04-ship-universe-audit

## Question

这个批次只回答：旧版当前实际注册并可生成的舰种，是否都已经进入现代 `ShipEntitySpecs` 和舰种矩阵；`ID.java` 中没有实体实现的历史名称如何分类。

## Scope

- 舰种/class ID：旧版 `ModEntity.regShipEntity` 注册的具体舰船与 hostile 变体。
- 行为族/模块：实体全集、egg/class ID 映射、现代规格登记。
- 旧版入口：`ModEntity.java`、`ID.java`、已注册实体类文件。
- modern 入口：`ShipEntitySpecs.java`、`ModEntityTypes.java`、`MIGRATION-SHIP-MATRIX.md`。

## Out of scope

- 不为 `ID.java` 中没有旧版实体类或注册入口的历史字符串新造实体。
- 不修改 legacy、现代运行时代码、资源或注册逻辑。
- 不重复运行客户端或已有 GameTest；本批只做静态全集核对。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/ship-universe-audit.json](results/ship-universe-audit.json)
