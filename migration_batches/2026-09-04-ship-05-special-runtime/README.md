# Batch: 2026-09-04-ship-05-special-runtime

## Status

`IN_PROGRESS`

## Question

Midway Hime、Northern Hime、Carrier WD、Shimakaze 与 Battleship NGT 的旧版特殊实体运行时、召唤/装备、目标选择和战斗节奏，是否已由现代实现按 1.12 语义接通，并且不会把特殊实体行为误套到普通舰种？

## Scope

- class ID：`30`、`31`、`33`、`36`、`37`
- 行为族：`boss runtime`、`servant lifecycle`、`carrier recovery`、`target/cadence`
- 模块：M2、M4、M5、M6、M7、M8
- 目标：按旧版实体入口逐项确认特殊属性、召唤物生命周期、攻击/冷却、目标边界、资源恢复和可见反馈；只修复被对照和测试证明的现代侧缺口。

## Out of scope

- 已锁定的 SHIP-01 至 SHIP-04 行为，不重新审计或重写。
- 完整多人协作 UI、全旧版 GUI packet 页、完整 inter-mod、全部历史 worldgen 和非主线语音/边角动画。
- 敌对 Boss 的统一过远追击策略和玩家提示策略；除非本批次五行的旧版入口证明存在直接回归，否则继续由独立审计跟踪。
- 未列入本批的其他 `SLICE` 舰种，不从共享行为 helper 推断为完成。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
