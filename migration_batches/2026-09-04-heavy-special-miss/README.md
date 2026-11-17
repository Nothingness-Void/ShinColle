# Batch: 2026-09-04-heavy-special-miss

## Question

特殊重攻击同时具有 `justLaunch` 和 `canMiss` 时，现代实现是否保持旧版的 miss 语义？

## Scope

- 舰种/class ID：所有通过 `LegacyShipAttackKind.HEAVY` 进入 catalog 特殊攻击的舰种。
- 行为族/模块：重攻击 miss 判定、投射物式发射和 GameTest 回归。
- 旧版入口：`BasicEntityShip`、`BasicEntityShipHostile` 的重攻击/投射物命中路径。
- modern 入口：`LegacyShipEntity`、`LegacyShipBehaviorCatalog`、`LegacyShipCombatHelper`、`GameplayParityGameTests`。

## Out of scope

- 不重写舰种 catalog、伤害公式、投射物移动、网络协议或客户端 FX。
- 不改变旧版默认随机概率；只修正 miss 分支被跳过的问题。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
