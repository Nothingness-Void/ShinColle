# Migration Decisions

## Destroyer Hime land-leg visual override (user approved)

Only Destroyer Hime (egg 29) uses visible legs on land even when saved bit 4
is clear. In water, retain the legacy bit-4 visibility/hover behavior; an
explicitly enabled leg bit always wins. Use existing model ship-depth > 0
only for rendering, including surface-floating positions excluded by vanilla's
isInWater fluid-height check. Never change saved state, dimensions or gameplay.
This is an explicit user-approved exception to strict 1.12.2 visual parity on
both Forge 1.20.1 and NeoForge 1.21.1. Existing sitting/no-fuel poses and other
models are unchanged. [Implementation and evidence](migration_batches/rendering-backport-1.20.1/decision.md).

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

**GUI 延期/简化作为等价收口依据的部分已撤销（见 D-016）**：多人 UI、完整旧版 GUI packet pages 不得以简版替代后标为完整一致。原条目的全 inter-mod、完整 worldgen 历史矩阵和非主线边角行为仍需独立证据，不伪装成主线完成。

> 部分撤销（2026-11-22，D-017）：“配置项可以写死为旧版默认值”不再作为可接受的现状；`Check_Ring`、`Limit_MobSpawnNumber`、`DropRate_Grudge`、`Despawn_Egg`、`Tile_*`、`Polymetal_Gravel*` 已接回，其余缺失配置项仍是待补的偏离，而不是已认可的设计。

## D-009：活动舰种全集以旧版注册为准

当前可迁移舰种以 `mc-1.12.2` 的 `ModEntity.regShipEntity` 和实际实体源文件为准。`ID.java` 中只有常量/NameMap、没有实体源文件和注册入口的历史名称记录为 `HISTORICAL_SOURCE_GAP`；没有恢复旧版实现前，不根据名称猜测或新造现代实体。精确核对见 [SHIP-UNIVERSE-AUDIT](migration_batches/2026-09-04-ship-universe-audit/)。

## D-010：敌我规格身份与公共资源路径严格对齐旧版继承层级

`ShipEntitySpec.hostile` 现与旧版 `BasicEntityShipHostile`（仅 `eggMeta > 2000` 的 19 个 `*Mob` 注册变体）严格一一对应；全部 45 个基础舰种（含 11 个继承 `BasicEntityShipSmall/CV` 的深海栖姬 `eggMeta` `22..74`）均为 `hostile = false` 的可驯服友方舰船，并统一执行 `BasicEntityShip` 公共待机消耗、自动补给、士气恢复与回血路径。详细证据见 [ALL-SHIP-IDLE-SUPPLY](migration_batches/2026-09-05-all-ship-idle-supply/) 与 [FULL-SHIP-BEHAVIOR-REAUDIT](migration_batches/2026-11-18-full-ship-behavior-reaudit/)。

## D-011：B6 收官与外置 mod / 运行时开关收口边界

- 9 条 `/ship*` 指令在 1.20.1 通过 Brigadier `RegisterCommandsEvent` 统一注册，并在射线与玩家查询路径提供无连接假玩家回退。
- `ConfigHandler.polyAsMn` 的默认 `false` 静态包契约不变（不改动 `data/forge/tags/items/` 静态 JSON），运行时由 `PolyAsMnCondition`（`shincolle:poly_as_mn`）与 `LegacyItemMatchHelper.matchesTag` 承担动态切换；`SystemConfig` 随 `TeitokuHelper` 状态包向客户端同步。
- 1.12.2 外置依赖按 1.20.1 原生能力与零编译期强依赖原则收口：Metamorph 使用原生 `MorphHelper` + `MorphCompatBridge.NOOP`/`SoftDependencyBridge`；Curios 仅通过 `ModList` + 反射触发结婚戒指佩戴槽周期效果。
- **已撤销（2026-11-22 parity-fix-items-blocks）**：原“IC2 由内部 `RouteEnergyAccess` 与 `ForgeCapabilities.ITEM_HANDLER`/`FLUID_HANDLER` 替代”决定，见 [方块与物品纠偏批次](migration_batches/2026-11-22-parity-fix-items-blocks/)。此撤销仅针对该替代决定，不撤销上述 Metamorph／Curios 边界。
- **部分已撤销（D-018）**：原生替代 Metamorph 本身保留；将该替代解释为许可保留 Morph 专属坐骑/Shift+G、扳手自动选中、玩家全额舰娘HP或额外移动/攻击/追踪/击退属性、玩家骑舰娘宿主或 Morph 专属坐骑宿主的范围，现明确撤销。D-011 原文未逐项批准这些行为，不将旧实现误说成已经有逐条语义依据。旧 B6“morph HP公式经核对保持不变”的结论亦已撤销，详见 [本批次决定](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md)。
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

D-011 中 Metamorph 原生承载/空桥若被解释为已提供完整外部选择与激活，**该等价解释已撤销（见 D-016）**；不撤销零编译期强依赖原则，也不扩大到 IC2/Curios。

## D-015：舰船 GUI 按钮、AI 标志、跟随距离与 `/ship*` 权限回到 1.12.2（撤销此前偏离）

以下此前现代实现中的偏离 **已撤销**，以 [PARITY-FIX-MENU-NETWORK-COMMAND](migration_batches/2026-11-22-parity-fix-menu-network-command/decision.md) 为准：

- ~~舰船 AI 用 `AUTO_TARGET/ALLOW_PVP/AUTO_SUPPLY/ROUTE_STAY/PICK_ITEM` 位掩码（`SET_AI_FLAGS`）~~ → **已撤销**：改为旧版 `PID.ShipBtn` 43 个按钮（`ShipCommandAction.SHIP_GUI_BUTTON`），标志为 PassiveAI/PVPFirst/OnSightChase/AntiAir/AntiSS/PickItem 等。
- ~~单值跟随距离 4..64、默认 14（`SET_FOLLOW_RANGE`）~~ → **已撤销**：FollowMin/FollowMax 两值，默认 3/12，旧版联动规则。
- ~~舰船背包固定 24 槽、无翻页；Shift 点击货仓一律进玩家背包并在热栏/主背包间互转，装备按舰种 `canEquip`~~ → **已撤销**：60 槽 3 页（DrumState 解锁）与 `0aad501` `transferStackInSlot` 顺序。
- ~~`/ship*` 使用 `hasPermission(2)`；`/shipcleardrop` 清除全部 `ItemEntity`；`/ship get` 生成时满血并清除坐守/跟随/守卫~~ → **已撤销**：旧版 `checkOP`（玩家发送者，创造即 OP）、只清 `BasicEntityItem` 语义掉落（整列世界高度，旧版 Y 0..256 即 1.12 全高）、`createShipEntity` 血量与 `CanDrop` 语义。

## D-016：客户端行为严格还原，撤销历史简化等价声明

- 客户端坐标、贴图、热区、数值和条件以 1.12.2 为准，只做必要 API 适配；旧版注释掉的显示不能借现代界面恢复，自创快捷键、双击窗口和开关不能冒充旧交互。
- D-008 的 GUI 延期/简化口径已撤销；其它非 GUI 范围不受影响。
- [B1 决定](migration_batches/2026-11-16-b1-gui-parity/decision.md) §2.5 第 3/4/5/6 项的“完全等价”声明已撤销；§2.3 电源绘制/提示和 §2.4 被替代桌面入口也加限定。航点终端、现代简化 GUI、250ms 重复行窗口、配置档切换/激活开关均不得保留为旧版替代。
- 旧 `LegacyShipModel` 解析几何加通用动画，以及舰船专用 Transform/State 目录作为完整模型/死亡/情绪/Glow 等价路径的结论已撤销；[全舰复审原条目](migration_batches/2026-11-18-full-ship-behavior-reaudit/decision.md) 和相关模型决定保留原数值审计、标明实现路径已被替代。其它渲染器仍用的源解析器不因此删除。
- 原版粒子近似不得作为旧自定义粒子的完整一致性依据；尚未移植的类别保持未完成，不用“已收口”覆盖缺口。
- 删除自创激活开关后不得自动激活或复建其它自创入口。D-011 背景中的外部 Metamorph 选择/激活必须由实际可达适配链证明，空桥和存档恢复不等价于玩家新激活入口。
- 本批次具体 19 项、真源用法、激活证据与限制只见 [客户端还原决定](migration_batches/2026-11-22-parity-fix-client/decision.md)，方法只见 [测试矩阵](MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)，不改写历史日志与历史通过数。

## D-017：crafting / world / config 严格按 1.12.2 还原（2026-11-22）

- 已撤销：[WORLDGEN-AUDIT](migration_batches/2026-09-16-worldgen-audit/decision.md) 用 `minecraft:disk` + `height_range 3..55` 近似 `WorldGenPolyGravel`、视半径/放置差异为“≈”的决定；改为移植 `generateSea` + `WorldGenPolyGravel` 的 `shincolle:polymetal_gravel` feature。
- 已撤销：宝箱 loot 写死在 Java、随机 meta 平分权重拆分、把旧版无法解析的 `shincolle:Torpedo` 映射为真实鱼雷的做法；恢复 `config/shincolle/loottable.cfg`，未解析条目跳过，rolls 整数除法，随机 meta 以最小公倍数缩放权重保持旧版概率。
- 已撤销：大建装备 `equipRate` + 弹药兜底、大建实例 roll 代入总材料、建造装备无附魔、大建投料只认材料的现代实现。
- 已撤销：普通敌舰写死 50/10/1 且每组 1 只、护卫沿用 20% 精英的做法。
- 已撤销：旧版不存在的 10 个 progression advancements 与 `shincolle:friendly_ship_deployed` 触发器，已删除。
- 详细证据见 [PARITY-FIX-CRAFTING-WORLD-CONFIG](migration_batches/2026-11-22-parity-fix-crafting-world-config/)。

<a id="d-018"></a>
## D-018：Teitoku / Morph / 玩家技能与编队严格回归旧版

- 状态：有效。D-001 的语义真源约束优先于早期“原生等价/完整闭环/100% parity”等未覆盖本批边界的概括。
- **保留** D-011 的原生 Metamorph 替代技术方案；**撤销**以此为理由添加或保留无旧版依据的游戏规则。Morph 专属坐骑及入口删除；普通舰娘坐骑、共享资源和原生 Morph 能力仍保留。
- UID分配从100开始且<100无效；现代 profile 的合法 classId=0 不得兼作空值，无选择统一-1并有明确旧存档迁移边界。不能因“UID数字0无效”推论“舰种classId0无效”。
- owner UID 相等不等于团队/指挥编辑权限。技能宿主只允许旧三类及旧顺序；原生 Morph 技能配置不应误封普通坐骑/乘客技能。
- 指挥棒9×6编队允许跨队伍同舰；加入/切换/删除按当前队伍和旧游标语义执行，不增加全队去重或生命周期全队清理。此处不是多人阵营 TeamSavedData，不撤销B3的阵营冷却/同盟规则。
- 戒指状态、周期任务和 chunk-loader 范围/生命周期按旧版；Forge API不可用时用实际47.4.18提供的票据API最小适配，不创造新配置。
- 已检索 D-001..D-011 及历史批次 decision 的 UID初值/宿主/编队/Morph/坐骑/HP相关条目：未发现单独批准“UID从1”或“全队去重”的决定。这两者作为错误旧实现撤销，不杜撰旧D编号。B2宿主扩展/专属坐骑、B6 HP不变、B1 Morph完整parity及REAUDIT专属坐骑保留前提已在原记录标记撤销。
- 删除 MORPH_TOGGLE_MOUNT 枚举项后，不再承诺与旧现代版本的命令序号兼容；旧B2追加枚举保序的概括不能当作本批跨版本互通保证。客户端与服务端须同步更新；不额外承诺旧原生专属坐骑实体迁移。
- [完整13项结论/旧版依据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md)、[精确证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/README.md)、[未验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits)。不以服务端全绿声称视觉、Curios联机或真实重启已验收。

### 九批集成的重复实现归并

- 决定编号保持唯一：菜单/网络/指令为 D-015，客户端为 D-016，crafting/world/config 为 D-017，Teitoku/Morph/技能为 D-018；各专题原先碰撞的编号不覆盖 D-012～D-014，引用随编号更新。
- FollowMin/FollowMax 只有一套字段、NBT `FMin/FMax`、同步和运行时路径，默认 3/12，起重机设值 2 可保存/同步；断粮继续使用旧怨念语义，不恢复独立 ShipFuel。
- 起重机扫描、工作循环、菜单共用同一作业舰；CraneTime 只在工作态递增并于完成清零。船厂/VolCore 同名旧配置只保留一套字段和读取，不恢复 RouteEnergyAccess。
- 扳手方块选择只保留客户端单例内存与配对包；获取变身须同 owner UID，获取/重复获取均不选择或激活。MorphProfile 的修复桶/喂食舰载机计数保留，但不解释为已接入航空攻击。
- DeskScreen 是唯一桌面入口；被删除 DeskReferenceScreen 的 incoming 修改只是现代附录的两句行为说明，没有旧 GuiDesk 对应内容，不迁移现代附录。保留旧三页变身背包、旧技能 HUD 与文字粒子；增加女神粒子不回退为简化覆盖层。
- 删除 Morph-only 实体/注册/渲染器/命令以及残留模型、OpTool 引用；普通舰娘坐骑和共享 LegacyMountControls 保留。不恢复 D-016 已撤销的激活开关/档案切换，外部激活入口仍未接入。
- 自创进度/trigger 维持删除；此前合法的舰种、战斗、AI、客户端和机器行为不因最后批次而回退。具体集成过程与证据见 [最终批次结果](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md)。
