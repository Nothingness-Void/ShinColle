# Batch: 2026-09-04-hostile-target-range

## Question

敌对 Boss 是否会严格遵循 1.12 的寻敌距离、创造模式过滤和已有目标失效规则，并且不向玩家暴露敌方攻击提示？

## Scope

- 舰种/class ID：所有 hostile ship/Boss 变体。
- 行为族/模块：目标获取、目标持续性、距离失效、玩家状态过滤和 hostile combat feedback。
- 旧版入口：`EntityAIShipRangeTarget`、`TargetHelper.SelectorForHostile`、hostile ship attack feedback 分支。
- modern 入口：`LegacyShipEntity`、`LegacyShipBehaviorCatalog`、`LegacyShipRangeTargetGoal`、`LegacyShipRangedAttackGoal`、`ClientboundCombatReactPacket`。

## Out of scope

- 不扩大旧版攻击范围，不改变默认 `mobAttackPlayer` 语义，不重写 AI priority 或攻击系统。
- 不修改客户端资源、模型、投射物移动或无关舰种行为。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
