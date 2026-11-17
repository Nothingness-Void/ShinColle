# Batch: 2026-09-03-ship-01-destroyer-ri

## Question

四类基础驱逐（I/Ro/Ha/Ni）与重巡 Ri 的剩余 combat cadence、资源边界和保存恢复路径，是否已经按 1.12 语义迁移到现代公共实体入口？

## Scope

- class ID：`0`、`1`、`2`、`3`、`9`
- 行为族：`destroyer`、`cruiser`
- 模块：M2、M4、M5、M6
- 目标：补齐或确认 light attack cadence、资源消耗/不足、目标有效性、runtime/save 字段和战斗后状态。

## Out of scope

- 不修改模型、渲染、GUI、网络协议或世界内容。
- 不重新审计已记录的 owner passive、pickup、mounted 和基础 constructor 行为。
- 不把静态“已有实现”当作完成；没有对应 legacy 断言的域必须保持未完成。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
