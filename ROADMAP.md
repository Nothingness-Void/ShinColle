# 后续队列

本文件只维护下一步顺序；当前事实见 [CURRENT_STATE.md](CURRENT_STATE.md)，历史批次只见 [批次目录](migration_batches/)。

## 当前指针

最新NEXT09:17：源门禁364/372绿，最后仅文档commit→普通merge→Neo push；Forge继续hold。然后暂停报告：[最终证据](migration_batches/2026-11-22-riding-aircraft-formation/followup-result.md)明确宽墙native route仍失败，待trace真实path/index/repath/host目标，不扩大或移动GT夹具。RingFly清理与legacy一致性冲突先等决策；后续Akagi/F3 cache/audio按原protocol，禁止猜测结论。

最新NEXT09:12：Forge已364GT绿，按[E3/D4结果](migration_batches/2026-11-22-riding-aircraft-formation/followup-result.md)本地commit→普通merge Neo/API适配→一次build/fullGT→Neo普通push；Forge绝不push。然后仅hot-swap已核验mount/path方法体，重建factory+静止相机完成mount墙图。F3残留RingFly为legacy-identical，先请求行为变更授权；无授权不改源码/断言。其余open：独立mount levitation API差异、Akagi heavy高靶弹道、跨维cache原生/旧版窗口时长、缺少F3 raw时间/日志。不得把四张已成图或三种ring升水counter-check重复当待做。恢复细节[FOLLOWUP](migration_batches/2026-11-22-riding-aircraft-formation/FOLLOWUP.md)。

最新NEXT08:00：源代码和两版门禁已绿，按[最终队列证据](migration_batches/2026-11-22-riding-aircraft-formation/queue-final-result.md)恢复有效客户端召唤夹具，再拍aircraft/fort/mount玻璃墙和核验Shimakaze normal/bored。当前NBT夹具多次失效已停止尝试，不得把诊断图标为通过。Forge继续禁止push，Neo允许普通merge/push。单横原session停滞仍未复现，不新增猜测修复。

最新NEXT07:44：line动态trace和OpenAL审计已完成；Forge只本地commit不可push（新指令），Neo可merge/push。剩余指定客户端aircraft/fort/mount玻璃、玩家骑乘、Shimakaze矿车normal/bored截图，然后同步最终证据/报告。原始line停滞原因仍未复现，不再调参。

最新NEXT07:30：B2双端新门禁361/369绿、seed0防溢出已验证；提交push/完成Neo merge后进行line-abreast-protocol.md，再OpenAL审计、VM六图、最终同步报告。旧预检失败已由seed0确定NaN链，不再是未解UUID猜测。

最新NEXT07:19：B2 Forge360/360提交push，merge Neo并适配EntityStorage；随后按本轮用户顺序做动态line-abreast、OpenAL、指定VM截图，最终双端绿/push。旧存档mayfly保持不动。

最新NEXT06:53：[CHECKPOINT](migration_batches/2026-11-22-riding-aircraft-formation/CHECKPOINT.md) 的D3 Neo失败隔离，禁止重复猜测修复或降低断言；再B2 /kill保存重载调查（protocol在Forge）；之后才最终门禁、缺失截图、文档和Neo push。以下NEXT旧checkpoint不再优先。

当前NEXT：F round2 / D round3 最终收口。Forge源码和真实Curios5.14.1全量357/357已绿；
push shared helper swept-box补充后，完成Neo merge及Curios9.6完整365项门禁，
最后完成waypoint/aircraft/mount/fort/岛风VM截图与状态更新，按Forge→Neo顺序push后停止。
Morph unlock-only gating不新增激活入口；OpenAL单条Stop和line-abreast实际振荡仍未归因。

当前用户授权 NEXT：完成 VM A/B/C/D/E/F 回归队列。先收口 B inventory/book
源码核对、D round2 aircraft block collision/pathing / mount right-click /
line-abreast 调查；Waypoint harvest/B world-height 改动已在工作区。然后 Forge
提交 → merge NeoForge API 适配 → 最终 build/full GT 双端 → 批次记录与核心文档
更新 → Forge first push 两端。Waypoint stone/wrench VM 截图已保存。不得把 A
新夹具 template 修正前的崩溃或旧 349/357 gates 说成当前完整通过。完成后停止，
不启动下列历史 NEXT。

本轮 GUI/audio 复核已完成双端 build/GameTests、scale-8 GUI 与报时 A/B；只恢复船坞旧 UI。最终同步并推送两端后停止，[证据](migration_batches/2026-11-22-gui-audio-regressions/STATUS.md)。历史 NEXT 不变。

本轮用户授权渲染回移和 Destroyer Hime 陆上开腿例外，完成后只同步并验证 NeoForge 分支、推送两端，然后停止；[本批边界](migration_batches/rendering-backport-1.20.1/protocol.md)。下列历史队列不自动启动。
唯一 NEXT：**剩余自定义粒子严格移植**。九批集成收尾推送后停在这里，未获用户授权不自动开工。

| 顺序 | 状态 | 工作 | 进入条件 | 退出条件 |
| --- | --- | --- | --- | --- |
| 1 | NEXT | 剩余 17 类自定义粒子及 ParticleTeam 核对，清单见 [粒子审计](migration_batches/2026-11-22-parity-fix-client/PARTICLE_PARITY_AUDIT.md) | 用户授权；先冻结独立协议；可分几何/宿主跟踪/文字等联动族 | 原类型/生命周期/API 适配及测试证据齐全；明确记录仍未完成项，不以原版近似宣称等价 |
| 2 | PENDING | 客户端针对性视觉验证 | 按 [视觉缺口](migration_batches/2026-11-22-parity-fix-client/decision.md#未完成与视觉边界) 冻结场景，相关实现稳定 | 覆盖旧坐标/热区、全模型/Glow/死亡/持物、HUD/粒子和满数据 GUI；有可复查截图与未验清单 |
| 3 | PENDING | 外部 Metamorph 激活适配、档案击杀数/航空机库存及舰载机计数接航空攻击 | 用户明确适配目标/API 和范围；参考 D-011、D-016、D-018 及 [激活证据](migration_batches/2026-11-22-parity-fix-client/decision.md#变身激活入口核查) | 有真实可达调用链与数据生命周期证据；不恢复自创开关或编造库存 |
| 4 | PENDING | 历史源码缺口 | 获得旧实体源码和注册依据 | 按 D-009 重审；未获得前维持历史源码缺口，不按名称造实体 |
| 5 | PENDING | crafting 剩余旧配置接回 | 用户授权；按 [配置缺口](migration_batches/2026-11-22-parity-fix-crafting-world-config/decision.md) 冻结 Attrs_*/Consume_*/Attack_*/Exp_*/Slots_*/Death_Time/Mod_IC2/Polymetal_Ore 等范围 | 单一配置字段与旧运行时读取对齐，正常/边界/无效值证据齐全 |
| 6 | PENDING | 飞机/坐骑 3D 寻路接线 | 用户授权；冻结 [AI 边界](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md) 对应入口 | 旧导航行为与生命周期验证完成，不只以主舰导航通过替代 |
| 7 | PENDING | 真实 Curios、重启清票据与旧世界迁移验证 | 用户授权并提供固定服务端/依赖/存档场景，按 [验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits) 冻结断言 | 真实佩戴、跨重启票据与存档证据可复查，明确多人/客户端未验项 |

## 晋级与更新顺序

九批集成事实只见 [当前状态](CURRENT_STATE.md)，重复实现与 D-015～D-018 编号只见 [集成决定](MIGRATION-DECISIONS.md#九批集成的重复实现归并)，最终验证只见 [测试矩阵](MIGRATION-TEST-MATRIX.md#九批-parity-integration-最终门禁)；本次收尾不推进上方 NEXT。

1. 任一时刻仅一个 NEXT；按用户授权冻结批次协议。
2. 完成代码后按 [测试矩阵](MIGRATION-TEST-MATRIX.md) 验收，失败不跳过。
3. 先写批次结果和决定，再更新矩阵、当前状态及变更索引，最后推进本队列。
4. 单人主线历史验收不替代本队列的严格客户端验收；不得直接重新标记 NONE。
