# Migration Decisions

这里只记录跨批次、会持续约束后续实现的决定。单批次结论放在对应 `migration_batches/<batch-id>/decision.md`。

## D-001：legacy 是语义真源

行为、数值、状态、资源和 UI 语义先由 `src/main` 解释；modern 侧只做 1.20.1 API 的最小承载适配。

## D-002：行为分派集中化

逐舰和行为族分派进入 `LegacyShipBehaviorCatalog` 或同级 helper；`LegacyShipEntity` 不继续增加 class-id 分派。

## D-003：批次化推进

默认一次处理 5 个相关舰种或一个联动行为族，批内完成实现，批末统一构建/GameTest；客户端改动按批次只做一次烟测。

## D-004：状态与证据分离

当前事实、队列、矩阵、测试方法、历史和批次证据分别有唯一归属；其他文档不得复制结论。

## D-005：无差异也是结果

定向 legacy/modern 审计若没有发现差异，不制造现代代码变更；必须留下 `COMPLETED_NO_CHANGE` 批次记录，后续由影响触发重审。

## D-006：投射物 miss 语义不可丢失

所有能 miss 的攻击，即使同时是 `justLaunch` 或视觉投射路径，也必须保留 miss 结果，不能把非零滚值直接当作命中伤害。

## D-007：敌我反馈边界独立

敌对实体可以保留粒子和 reaction bus，但只有旧版允许的玩家反馈才显示 actionbar/message；目标筛选、creative/invulnerable 和同所有者门槛分别验证。

## D-008：单人主线不等于完整 parity

多人 UI、完整旧版 GUI packet pages、全 inter-mod、完整 worldgen 历史矩阵和非主线边角行为单独进入 backlog，不伪装成主线完成。

## D-009：活动舰种全集以旧版注册为准

当前可迁移舰种以 `mc-1.12.2` 的 `ModEntity.regShipEntity` 和实际实体源文件为准。`ID.java` 中只有常量/NameMap、没有实体源文件和注册入口的历史名称记录为 `HISTORICAL_SOURCE_GAP`；没有恢复旧版实现前，不根据名称猜测或新造现代实体。精确核对见 [SHIP-UNIVERSE-AUDIT](migration_batches/2026-09-04-ship-universe-audit/)。

## D-010：敌我规格身份与公共资源路径严格对齐旧版继承层级

`ShipEntitySpec.hostile` 现与旧版 `BasicEntityShipHostile`（仅 `eggMeta > 2000` 的 19 个 `*Mob` 注册变体）严格一一对应；全部 45 个基础舰种（含 11 个继承 `BasicEntityShipSmall/CV` 的深海栖姬 `eggMeta` `22..74`）均为 `hostile = false` 的可驯服友方舰船，并统一执行 `BasicEntityShip` 公共待机消耗、自动补给、士气恢复与回血路径。详细证据见 [ALL-SHIP-IDLE-SUPPLY](migration_batches/2026-09-05-all-ship-idle-supply/) 与 [FULL-SHIP-BEHAVIOR-REAUDIT](migration_batches/2026-11-18-full-ship-behavior-reaudit/)。

## D-011：B6 收官与外置 mod / 运行时开关收口边界

- 9 条 `/ship*` 指令在 1.20.1 通过 Brigadier `RegisterCommandsEvent` 统一注册，并在射线与玩家查询路径提供无连接假玩家回退。
- `ConfigHandler.polyAsMn` 的默认 `false` 静态包契约不变（不改动 `data/forge/tags/items/` 静态 JSON），运行时由 `PolyAsMnCondition`（`shincolle:poly_as_mn`）与 `LegacyItemMatchHelper.matchesTag` 承担动态切换；`SystemConfig` 随 `TeitokuHelper` 状态包向客户端同步。
- 1.12.2 外置依赖按 1.20.1 原生能力与零编译期强依赖原则收口：Metamorph 使用原生 `MorphHelper` + `MorphCompatBridge.NOOP`/`SoftDependencyBridge`；IC2 由内部 `RouteEnergyAccess` 与 `ForgeCapabilities.ITEM_HANDLER`/`FLUID_HANDLER` 替代；Curios 仅通过 `ModList` + 反射触发结婚戒指佩戴槽周期效果。
- 40 个仅存在于 `ID.java` 的历史名称（含 `Raiden` 作为合体形态而非独立注册实体）继续按 D-009 保持 `HISTORICAL_SOURCE_GAP` 记录，不虚构实现。详细证据见 [B6-INTERMOD-DETAILS](migration_batches/2026-11-17-b6-intermod-details/)。

## D-012：已撤销现代士气阈值和独立舰娘燃料

- **已撤销**：[2026-11-18 再审决定](migration_batches/2026-11-18-full-ship-behavior-reaudit/decision.md)第 3 项中的现代士气阈值。
- **已撤销**：`91a3796` 引入的独立舰娘燃料、依其判定断粮以及用现代上限判定补给档位的做法。不影响造船厂等其他模块的燃料。
- 旧版依据与落实见 [舰娘核心批次](migration_batches/2026-11-22-parity-fix-ship-core/decision.md)。
