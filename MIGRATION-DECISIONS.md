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

**GUI 延期/简化作为等价收口依据的部分已撤销（见 D-013）**：多人 UI、完整旧版 GUI packet pages 不得以简版替代后标为完整一致。原条目的全 inter-mod、完整 worldgen 历史矩阵和非主线边角行为仍需独立证据，不伪装成主线完成。

## D-009：活动舰种全集以旧版注册为准

当前可迁移舰种以 `mc-1.12.2` 的 `ModEntity.regShipEntity` 和实际实体源文件为准。`ID.java` 中只有常量/NameMap、没有实体源文件和注册入口的历史名称记录为 `HISTORICAL_SOURCE_GAP`；没有恢复旧版实现前，不根据名称猜测或新造现代实体。精确核对见 [SHIP-UNIVERSE-AUDIT](migration_batches/2026-09-04-ship-universe-audit/)。

## D-010：敌我规格身份与公共资源路径严格对齐旧版继承层级

`ShipEntitySpec.hostile` 现与旧版 `BasicEntityShipHostile`（仅 `eggMeta > 2000` 的 19 个 `*Mob` 注册变体）严格一一对应；全部 45 个基础舰种（含 11 个继承 `BasicEntityShipSmall/CV` 的深海栖姬 `eggMeta` `22..74`）均为 `hostile = false` 的可驯服友方舰船，并统一执行 `BasicEntityShip` 公共待机消耗、自动补给、士气恢复与回血路径。详细证据见 [ALL-SHIP-IDLE-SUPPLY](migration_batches/2026-09-05-all-ship-idle-supply/) 与 [FULL-SHIP-BEHAVIOR-REAUDIT](migration_batches/2026-11-18-full-ship-behavior-reaudit/)。

## D-011：B6 收官与外置 mod / 运行时开关收口边界

- 9 条 `/ship*` 指令在 1.20.1 通过 Brigadier `RegisterCommandsEvent` 统一注册，并在射线与玩家查询路径提供无连接假玩家回退。
- `ConfigHandler.polyAsMn` 的默认 `false` 静态包契约不变（不改动 `data/forge/tags/items/` 静态 JSON），运行时由 `PolyAsMnCondition`（`shincolle:poly_as_mn`）与 `LegacyItemMatchHelper.matchesTag` 承担动态切换；`SystemConfig` 随 `TeitokuHelper` 状态包向客户端同步。
- 1.12.2 外置依赖按 1.20.1 原生能力与零编译期强依赖原则收口：Metamorph 使用原生 `MorphHelper` + `MorphCompatBridge.NOOP`/`SoftDependencyBridge`；IC2 由内部 `RouteEnergyAccess` 与 `ForgeCapabilities.ITEM_HANDLER`/`FLUID_HANDLER` 替代；Curios 仅通过 `ModList` + 反射触发结婚戒指佩戴槽周期效果。
- 40 个仅存在于 `ID.java` 的历史名称（含 `Raiden` 作为合体形态而非独立注册实体）继续按 D-009 保持 `HISTORICAL_SOURCE_GAP` 记录，不虚构实现。详细证据见 [B6-INTERMOD-DETAILS](migration_batches/2026-11-17-b6-intermod-details/)。

D-011 中 Metamorph 原生承载/空桥若被解释为已提供完整外部选择与激活，**该等价解释已撤销（见 D-013）**；不撤销零编译期强依赖原则，也不扩大到 IC2/Curios。

## D-012：舰船 GUI 按钮、AI 标志、跟随距离与 `/ship*` 权限回到 1.12.2（撤销此前偏离）

以下此前现代实现中的偏离 **已撤销**，以 [PARITY-FIX-MENU-NETWORK-COMMAND](migration_batches/2026-11-22-parity-fix-menu-network-command/decision.md) 为准：

- ~~舰船 AI 用 `AUTO_TARGET/ALLOW_PVP/AUTO_SUPPLY/ROUTE_STAY/PICK_ITEM` 位掩码（`SET_AI_FLAGS`）~~ → **已撤销**：改为旧版 `PID.ShipBtn` 43 个按钮（`ShipCommandAction.SHIP_GUI_BUTTON`），标志为 PassiveAI/PVPFirst/OnSightChase/AntiAir/AntiSS/PickItem 等。
- ~~单值跟随距离 4..64、默认 14（`SET_FOLLOW_RANGE`）~~ → **已撤销**：FollowMin/FollowMax 两值，默认 3/12，旧版联动规则。
- ~~舰船背包固定 24 槽、无翻页；Shift 点击货仓一律进玩家背包并在热栏/主背包间互转，装备按舰种 `canEquip`~~ → **已撤销**：60 槽 3 页（DrumState 解锁）与 `0aad501` `transferStackInSlot` 顺序。
- ~~`/ship*` 使用 `hasPermission(2)`；`/shipcleardrop` 清除全部 `ItemEntity`；`/ship get` 生成时满血并清除坐守/跟随/守卫~~ → **已撤销**：旧版 `checkOP`（玩家发送者，创造即 OP）、只清 `BasicEntityItem` 语义掉落（整列世界高度，旧版 Y 0..256 即 1.12 全高）、`createShipEntity` 血量与 `CanDrop` 语义。

## D-013：客户端行为严格还原，撤销历史简化等价声明

- 客户端坐标、贴图、热区、数值和条件以 1.12.2 为准，只做必要 API 适配；旧版注释掉的显示不能借现代界面恢复，自创快捷键、双击窗口和开关不能冒充旧交互。
- D-008 的 GUI 延期/简化口径已撤销；其它非 GUI 范围不受影响。
- [B1 决定](migration_batches/2026-11-16-b1-gui-parity/decision.md) §2.5 第 3/4/5/6 项的“完全等价”声明已撤销；§2.3 电源绘制/提示和 §2.4 被替代桌面入口也加限定。航点终端、现代简化 GUI、250ms 重复行窗口、配置档切换/激活开关均不得保留为旧版替代。
- 旧 `LegacyShipModel` 解析几何加通用动画，以及舰船专用 Transform/State 目录作为完整模型/死亡/情绪/Glow 等价路径的结论已撤销；[全舰复审原条目](migration_batches/2026-11-18-full-ship-behavior-reaudit/decision.md) 和相关模型决定保留原数值审计、标明实现路径已被替代。其它渲染器仍用的源解析器不因此删除。
- 原版粒子近似不得作为旧自定义粒子的完整一致性依据；尚未移植的类别保持未完成，不用“已收口”覆盖缺口。
- 删除自创激活开关后不得自动激活或复建其它自创入口。D-011 背景中的外部 Metamorph 选择/激活必须由实际可达适配链证明，空桥和存档恢复不等价于玩家新激活入口。
- 本批次具体 19 项、真源用法、激活证据与限制只见 [客户端还原决定](migration_batches/2026-11-22-parity-fix-client/decision.md)，方法只见 [测试矩阵](MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)，不改写历史日志与历史通过数。
