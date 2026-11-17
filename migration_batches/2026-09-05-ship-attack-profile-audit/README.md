# Batch: SHIP-ATTACK-PROFILE-AUDIT

## Question

`LegacyShipAttackProfile` 是否逐项保持了已注册 45 个 1.12 基础舰种构造函数中的轻攻、重攻和航空攻击开关，并且没有让特殊 hook 越过旧版入口边界？

## Outcome

已完成。45 个基础 class、64 个 modern 注册变体和 19 个 hostile Mob 变体完成来源对照；全注册攻击通道 GameTest 通过，最终 `All 271 required tests passed`。没有发现需要新增生产行为的差异。

## Scope

- 45 个已注册基础舰种及其 hostile 变体的攻击开关。
- 旧版构造函数到现代 profile 的最小映射。
- 已有特殊攻击 hook 的 channel boundary 核对。

## Out of scope

- 不恢复没有旧版实体源/注册入口的历史 ID。
- 不修改 projectile 参数、AI、GUI、渲染或客户端流程。
- 不重复启动客户端；本批没有客户端、资源或渲染改动。

## Records

- [protocol.md](protocol.md)
- [STATUS.md](STATUS.md)
- [decision.md](decision.md)
- [results/](results/)
