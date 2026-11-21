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

- **已撤销**：将“保留 miss 结果”落地为投射物零伤害、禁止命中或跳过撞击结算的方式。
- 旧版投射物语义：miss 只偏移落点（XZ ±5、Y +0~5），撞击照常在范围内逐目标结算；不得将发射脱靶等同于撞击免伤。复核与落实见 [战斗与投射物批次](migration_batches/2026-11-22-parity-fix-combat-projectile/)。

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

## D-013：AI移动与目标的现代偏离已撤销

以下现代偏离实现/历史认可均 **已撤销**，不再作为后续迁移依据。逐条旧版证据、纠正结果和未实现边界只保存在 [AI移动批次](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md) 与 [13项审计](migration_batches/2026-11-22-parity-fix-ai-movement/evidence/audit-13.md)，不在根文档复制算法结论。

| 已撤销项 | 历史记录核查与替代入口 |
| --- | --- |
| 单一aiFollowRange及其等价默认值推断 | [hostile-target-range决定](migration_batches/2026-09-04-hostile-target-range/decision.md)中明确认可已撤销；审计1 |
| 狼式跟随、外圈传送落点、额外指令门槛与装备速度公式 | 撤销旧现代实现的迁移口径；审计3；未发现单独明确批准“狼式跟随”的旧decision条目，不虚构历史编号 |
| 守卫每tick moveTo、固定半径、Y+0.1与commandSpeed | 撤销旧现代移动规则；[SHIP-02历史记录](migration_batches/2026-09-03-ship-02-command-route-target/decision.md)不再能证明完整守卫等价；审计5 |
| CLIENT canTeleport | 撤销客户端配置归属；[全舰再审的canTeleport接线记录](migration_batches/2026-11-18-full-ship-behavior-reaudit/decision.md)不能继续作为配置归属已等价的证明；审计2 |
| vanilla GroundPathNavigation足以替代舰船水/空导航 | 撤销该实现假设；审计13，保留旧版明确的vanilla路径清理用途，不等于删除整个原版navigator |
| 主人联动OwnerHurtBy/OwnerHurt目标Goal与伤害记忆捷径 | [B3多人批次Goal接线记录](migration_batches/2026-11-17-b3-multiplayer-team/decision.md)的保留口径已撤销；审计7/8 |
| 独立玩家优先Goal | [SP-CLOSE目标等价认可](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md#hostile-target-and-feedback-audit)的该部分已撤销；审计9 |
| MeleeAttackGoal与舰种近战距离表 | 撤销旧现代替代实现；审计10 |
| RandomStrollGoal及现代触发/速度 | 撤销旧现代替代实现；审计11 |
| 守卫20tick预热、瞄准下限与装备缩减公式 | 撤销旧现代公式；审计6 |
| 拾取的combat/无指令/无目标额外门槛 | 撤销旧现代限制；审计12；FollowMax范围复核已匹配，不制造重复变更 |

没有在旧decision中找到明确认可的项目按“撤销现代实现假设”记录，不将协议read-set中的类名列举误说成设计决定。旧批次过程与结果保留历史语境，冲突部分已在原决定标注撤销并链接本批次。

## D-014：舰种个体行为的现代偏离已撤销

- **已撤销**：Boss 专用阶段与动作循环、额外物品掉落、舰种推导精英或 Boss 以及敌舰等级和士气注入。历史认可见 [SHIP-03](migration_batches/2026-09-03-ship-03-combat-behavior/decision.md) 与 [SHIP-06](migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/decision.md) 的对应部分。
- **已撤销**：特殊攻击钩子的独有倍率与冷却下限，以及为爱宕、高雄及金刚四舰保留专属变身技能的现代做法。历史记录见 [SHIP-06](migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/decision.md)、[SHIP-09](migration_batches/2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new/decision.md) 与 [GameTest 修复批次](migration_batches/2026-11-16-gametest-repair/decision.md) 的对应部分。
- 旧版依据和处理结论见 [舰种个体行为批次决定](migration_batches/2026-11-22-parity-fix-ship-variants/decision.md)。
