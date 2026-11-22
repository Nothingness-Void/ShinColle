# 当前状态

本文件只保存当前事实与交接快照；历史入口见 [迁移变更索引](MIGRATION-CHANGELOG.md)，下一步只看 [ROADMAP.md](ROADMAP.md)。

<a id="当前任务"></a>
## 当前集成与任务边界

- 分支：`devin/mc-1.21.1-neoforge`；固定基线 `5b95cfe`；本轮 merge Forge 船坞修复 `e234d5b` 和最终证据 `cf08c01`。旧九批集成历史从 [索引](MIGRATION-CHANGELOG.md#九批-parity-集成收尾) 进入。
- Minecraft **1.21.1**、NeoForge **21.1.267**、ModDevGradle **2.0.148**、Gradle **8.8**、JDK **21**；模组版本 **0.2.0-alpha**。
- PR #11 复核末次代码提交：`3a06c51`；回收蛋旧 NBT 与宽泛附魔标签匹配已分别修复，伤害记录滞留已证实为 NeoForge 上游问题。初次完整构建及本轮证据见 [1.21.1 批次结果](migration_batches/neoforge-1.21.1/status.md#pr-11-review-follow-up)。
- 当前任务：GUI/audio 回归复核已完成；仅恢复船坞旧布局，雷达长名与冻结整点报时按旧版保留。双端门禁、NeoForge scale-8 GUI 与报时 A/B 已验证；推送本次 merge 后停止，不创建 PR 或自动开始后续批次。
- 工作区边界：实现只在 `src/modern/java`、`src/modern/resources`，另有构建配置与迁移文档。`src/main/**` 继续只读，相对固定 parity 基线无差异。
- 唯一下一步入口：[ROADMAP.md](ROADMAP.md)。无服务端验证阻塞；下述未验证项/遗留不因此消失。

## 最近证据与权威入口

- 本轮 GUI/audio 复核完成：仅恢复大型船坞旧布局/错误提示；雷达长名字和冻结整点报时按旧版保留，报时 A/B 已复现。双端门禁和 scale-8 GUI 见[复核记录](migration_batches/2026-11-22-gui-audio-regressions/STATUS.md)。最终同步推送后停止，不启动历史 NEXT。

- 最新 Destroyer Hime 取舍已获批准：仅陆上开腿，水中原样，显式 bit4 始终优先。最终使用既有模型 depth > 0（vanilla isInWater 漏掉水面悬浮薄层，已由实测排除）。Forge build 与 **344/344** GameTests、NeoForge build 与 **352/352** 均通过；两端 CPU 姿态各 1536 项，最终 VM 指定画面通过。[Forge 证据](migration_batches/rendering-backport-1.20.1/STATUS.md)、[NeoForge 本轮记录](migration_batches/neoforge-1.21.1/status.md#rendering-backport-and-approved-land-legs)。

- 此前渲染修复后的 JDK 21 `compileJava` 成功，全量 `runGameTestServer` **351/351 通过、0 失败**；资源检查为 0 个大小写路径冲突。舰娘站立/坐下与 hitbox、办公桌/起重机/小船坞/航点/火山核心的针对性截图与根因见 [客户端修复](migration_batches/neoforge-1.21.1/status.md#client-rendering-follow-up-ship-placement)。本轮未重跑完整 `build`，以下 PR #11 的 348 项结果是历史证据。

- PR #11 最后生产/测试代码修改后：JDK 21 的 `compileJava` 与完整 `runGameTestServer` 通过，`All 348 required tests passed :)`（原 340 项与 8 项兼容/诊断测试；0 失败）。本轮未重跑 `build`；初次移植的构建结果保留在批次记录。准确输出、范围与日志校验和见 [复核证据](migration_batches/neoforge-1.21.1/status.md#pr-11-review-follow-up)。1.20.1 的 340 项结果保留在 [历史集成证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md)，不替代本轮结果。
- 重复实现的归并约束与编号变更只见 [D-018 集成决定](MIGRATION-DECISIONS.md#九批集成的重复实现归并)：菜单网络 D-015、客户端 D-016、crafting D-017、Teitoku D-018，不覆盖 D-012～D-014。
- 既有起重机关联与夹具证据：[物品方块](migration_batches/2026-11-22-parity-fix-items-blocks/results/README.md#integration-merge)、[crafting 集成验证](migration_batches/2026-11-22-parity-fix-crafting-world-config/results/verification.md#integration-merge)。
- 舰种身份与状态只见 [舰种矩阵](MIGRATION-SHIP-MATRIX.md)；本次不扩大逐舰或客户端验收口径。

## 未验证与当前遗留

- NeoForge 21.1.267 在 `LivingIncomingDamageEvent` 被取消后直接返回而未弹出 damage container；普通原版实体的独立取消监听器、戒指及坐骑免疫均实际复现同样的记录滞留。保留正确的伤害前取消语义，不在 ShinColle 监听器里擅自修改引擎栈；详见复核记录。
- 仅完成本轮舰娘位置与指定方块/物品的针对性截图检查；全舰型/骑乘组合、Glow、GUI 热区、HUD/粒子仍未全面视觉验收，不能以编译或服务端通过替代。旧桌子所在区块可能需一次方块/光照更新，详见批次记录。
- 真实 Curios、真实多人网络互通、跨重启票据与完整旧世界升级未验。ForgeCaps、嵌套 ItemStack、流体与坐标已有合成旧数据测试；这不是用户实际存档升级证据，实际升级前仍应备份。
- 除用户批准的 Destroyer Hime 陆上开腿视觉例外，未发现必须接受的玩家可见行为差异；这不是所有客户端/第三方模组场景已完全等价的声明。旧基线遗留仍如下。
- [客户端粒子审计](migration_batches/2026-11-22-parity-fix-client/PARTICLE_PARITY_AUDIT.md) 的 17 类自定义粒子仍未完整移植；部分作业/女神实现不代表整类完成。阵型 `HEALING_SPARKLE` 仍用原版近似。
- crafting 未接回的配置仍包括 `Attrs_*`、`Consume_*`、`Attack_*`、`Exp_*`、`Slots_*`、`Death_Time`、`Mod_IC2`、`Polymetal_Ore` 等，详见 [crafting 决定](migration_batches/2026-11-22-parity-fix-crafting-world-config/decision.md)。
- 飞机/坐骑未接入舰船 3D 寻路，见 [AI 审计](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md)。
- 变身没有玩家可用的外部激活入口；不恢复自创激活/档案切换。MorphProfile 缺击杀数与航空机库存；已有变身舰载机计数的补充/持久化未接入航空攻击。见 [客户端激活边界](migration_batches/2026-11-22-parity-fix-client/decision.md#变身激活入口核查) 与 [最终批次验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits)。
- 非目标：本次不补完这些遗留，不以服务端全绿宣称全面 parity，不新增旧版无依据的玩法/开关。
