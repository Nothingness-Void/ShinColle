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
- **部分已撤销（D-012）**：原生替代 Metamorph 本身保留；将该替代解释为许可保留 Morph 专属坐骑/Shift+G、扳手自动选中、玩家全额舰娘HP或额外移动/攻击/追踪/击退属性、玩家骑舰娘宿主或 Morph 专属坐骑宿主的范围，现明确撤销。D-011 原文未逐项批准这些行为，不将旧实现误说成已经有逐条语义依据。旧 B6“morph HP公式经核对保持不变”的结论亦已撤销，详见 [本批次决定](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md)。
- 40 个仅存在于 `ID.java` 的历史名称（含 `Raiden` 作为合体形态而非独立注册实体）继续按 D-009 保持 `HISTORICAL_SOURCE_GAP` 记录，不虚构实现。详细证据见 [B6-INTERMOD-DETAILS](migration_batches/2026-11-17-b6-intermod-details/)。

<a id="d-012"></a>
## D-012：Teitoku / Morph / 玩家技能与编队严格回归旧版

- 状态：有效。D-001 的语义真源约束优先于早期“原生等价/完整闭环/100% parity”等未覆盖本批边界的概括。
- **保留** D-011 的原生 Metamorph 替代技术方案；**撤销**以此为理由添加或保留无旧版依据的游戏规则。Morph 专属坐骑及入口删除；普通舰娘坐骑、共享资源和原生 Morph 能力仍保留。
- UID分配从100开始且<100无效；现代 profile 的合法 classId=0 不得兼作空值，无选择统一-1并有明确旧存档迁移边界。不能因“UID数字0无效”推论“舰种classId0无效”。
- owner UID 相等不等于团队/指挥编辑权限。技能宿主只允许旧三类及旧顺序；原生 Morph 技能配置不应误封普通坐骑/乘客技能。
- 指挥棒9×6编队允许跨队伍同舰；加入/切换/删除按当前队伍和旧游标语义执行，不增加全队去重或生命周期全队清理。此处不是多人阵营 TeamSavedData，不撤销B3的阵营冷却/同盟规则。
- 戒指状态、周期任务和 chunk-loader 范围/生命周期按旧版；Forge API不可用时用实际47.4.18提供的票据API最小适配，不创造新配置。
- 已检索 D-001..D-011 及历史批次 decision 的 UID初值/宿主/编队/Morph/坐骑/HP相关条目：未发现单独批准“UID从1”或“全队去重”的决定。这两者作为错误旧实现撤销，不杜撰旧D编号。B2宿主扩展/专属坐骑、B6 HP不变、B1 Morph完整parity及REAUDIT专属坐骑保留前提已在原记录标记撤销。
- 删除 MORPH_TOGGLE_MOUNT 枚举项后，不再承诺与旧现代版本的命令序号兼容；旧B2追加枚举保序的概括不能当作本批跨版本互通保证。客户端与服务端须同步更新；不额外承诺旧原生专属坐骑实体迁移。
- [完整13项结论/旧版依据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md)、[精确证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/README.md)、[未验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits)。不以服务端全绿声称视觉、Curios联机或真实重启已验收。
