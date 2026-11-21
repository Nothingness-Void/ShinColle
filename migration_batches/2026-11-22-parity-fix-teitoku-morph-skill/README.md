# Batch: 2026-11-22-parity-fix-teitoku-morph-skill

## Question

在保留原生替代 Metamorph 的前提下，严格恢复旧版提督/戒指/变身/技能宿主/编队与舰娘区块加载语义。

## Scope

- 全局行为族，不新增舰种；legacy 只读，仅 modern 与文档改动。
- 用户全部分步请求归并为 decision 中的13项，含 classId=0 与编队补修。

## Navigation

- [Protocol](protocol.md)
- [Status](STATUS.md)
- [Decision / 13项结论](decision.md)
- [Results / 精确证据](results/README.md)
- [测试矩阵](../../MIGRATION-TEST-MATRIX.md#parity-fix-teitoku-morph-skill310310)
