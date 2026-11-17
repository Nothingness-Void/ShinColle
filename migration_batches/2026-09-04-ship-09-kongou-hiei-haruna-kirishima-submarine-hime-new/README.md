# Batch: SHIP-09

## Question

这个批次只回答：class 60、61、62、63、72 的金刚型战舰与新潜艇姬，是否已经按 1.12 语义完成单人主线所需的 runtime、战斗、目标/骑乘和模型边界。

## Scope

- 舰种/class ID：`60`、`61`、`62`、`63`、`72`
- 行为族/模块：Kongou/Hiei/Haruna/Kirishima combo 与 aura、cannon family、SSNH dual missile/depth path/morale/riding、共享 heavy miss boundary
- 旧版入口：四个 `EntityBB*` 与 `*Mob`、`EntitySSNH`/`EntitySubmHime`、对应 `Model*`、旧版基础舰船实体
- modern 入口：`LegacyShipEntity`、`LegacyShipBehaviorCatalog`、`LegacyShipCombatHelper`、`LegacyShipAttackProfile`、`ShipEntitySpecs`、模型 state/parser/bake 和 `GameplayParityGameTests`

## Out of scope

- 未列入本批的舰种、Phase 10 世界生成矩阵、完整 1.12 parity 和多人专属 UI。
- 敌对 Boss 远距离追击与玩家起飞/命中提示策略；除非本批对照证明选中实体直接错误调用了该路径。
- 重写现代架构或新增第二套网络/投射物协议。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
