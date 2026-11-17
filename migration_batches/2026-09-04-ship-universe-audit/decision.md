# Decision

## Decision

- 当前 1.12 活动舰种全集以 `ModEntity.regShipEntity` 为准，而不是以 `ID.java` 的完整名称映射为准。注册表有 64 个实体行，包含 45 个 friendly 基础舰种和 19 个 hostile 变体；所有注册类文件均存在。
- 现代 `ShipEntitySpecs` 有 64 个对应规格行，按 class ID/hostile 维度与旧版活动集合完全匹配；舰种矩阵的 45 个基础行也与活动集合完全匹配。没有活动实体漏入或漏出。
- 旧版 `EntitySubmU511Mob` 的注册调用和状态字段保留基础 `SSU511` class ID，这是旧版 hostile 类自身的写法；现代规格以 hostile `+2000` 变体表达敌我维度，属于已有的最小承载归一化，不构成新舰种缺口。
- `ID.java` 另有 40 个 class ID 只有 `NameMap`/常量名称，没有对应实体类、`ModEntity` 注册调用，也没有本地历史、可见上游分支或上游标签中的实现。它们被记录为 `HISTORICAL_SOURCE_GAP`，不得根据名称猜测行为或生成现代占位实体。

## Completion boundary

本批证明了当前 1.12 源树中所有“有实现且有注册”的舰种已进入现代规格和矩阵；它不证明 40 个没有旧版实现的历史名称已经迁移。V0 原生客户端验收仍由 `SP-CLOSE` 队列单独负责。

## Evidence

- [protocol.md](protocol.md)
- [STATUS.md](STATUS.md)
- [results/ship-universe-audit.json](results/ship-universe-audit.json)
