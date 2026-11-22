# 当前状态

本文件只保存当前事实与交接快照；历史入口见 [迁移变更索引](MIGRATION-CHANGELOG.md)，下一步只看 [ROADMAP.md](ROADMAP.md)。

<a id="当前任务"></a>
## 当前集成与任务边界

- 分支：`devin/parity-fix-integration`，基于 `origin/codex/mc-1.20.1`。
- 已集成九个 parity 修复批次：ship-core、combat-projectile、ai-movement、ship-variants、menu-network-command、client、items-blocks、crafting-world-config、teitoku-morph-skill。各批次过程和历史验收从 [索引](MIGRATION-CHANGELOG.md#九批-parity-集成收尾) 进入，不以专题历史分支/通过数覆盖当前集成状态。
- 最终 merge：`042a3696bef65c712614ea19ba4ae76ddec3f8af`；前序物品方块 merge 为 `ba0288157aa67cbad12c65b229f1d66106328e82`，crafting/world/config merge 为 `351e3ab5ca02ccf1cc1940cc019e5e99876a2d75`。
- 当前任务：将 NeoForge 后续渲染修复回移 Forge 47 / JDK17；包含获批 Destroyer Hime 陆上开腿（不改存储 bit）、水中保留旧切换。推送 integration 后仅将更新基线 merge 回 NeoForge 并按用户要求验证；不修改 `codex/mc-1.20.1` 或 src/main。
- 工作区边界：实现只在 `src/modern/java`、`src/modern/resources`；本轮其余改动仅迁移文档。`src/main/**` 继续只读，相对 `origin/codex/mc-1.20.1` 无差异。
- 唯一下一步入口：[ROADMAP.md](ROADMAP.md)。无服务端验证阻塞；下述未验证项/遗留不因此消失。

## 最近证据与权威入口

- 本轮 GUI/audio 复核：仅恢复大型船坞旧布局/错误提示；雷达长名字和冻结整点报时先按旧版保留。[复核记录](migration_batches/2026-11-22-gui-audio-regressions/README.md)。本轮仅按授权同步 NeoForge、验证截图并推送，不启动历史 NEXT。

- 渲染回移：JDK17 build 成功，全量 **344/344** GameTests 通过；资源零大小写冲突，45 模型源码核对（仅精确豁免获批陆上开腿）及 64 变体/1536 姿态 CPU 测量通过。源提交、API 适配、review 不适用结论与视觉边界见 [本批记录](migration_batches/rendering-backport-1.20.1/STATUS.md)。

- 最后生产/测试代码修改后：JDK 17 独立编译与全量 GameTest 均通过，`All 340 required tests passed :)`。最终集成 GameTest 首轮全绿，未重跑；详细输出、首轮编译修复与日志校验和见 [最终集成证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md)，命令/覆盖/门禁只见 [测试矩阵](MIGRATION-TEST-MATRIX.md#九批-parity-integration-最终门禁)。
- 重复实现的归并约束与编号变更只见 [D-018 集成决定](MIGRATION-DECISIONS.md#九批集成的重复实现归并)：菜单网络 D-015、客户端 D-016、crafting D-017、Teitoku D-018，不覆盖 D-012～D-014。
- 既有起重机关联与夹具证据：[物品方块](migration_batches/2026-11-22-parity-fix-items-blocks/results/README.md#integration-merge)、[crafting 集成验证](migration_batches/2026-11-22-parity-fix-crafting-world-config/results/verification.md#integration-merge)。
- 舰种身份与状态只见 [舰种矩阵](MIGRATION-SHIP-MATRIX.md)；本次不扩大逐舰或客户端验收口径。

## 未验证与当前遗留

- 客户端画面/GUI 热区、模型/Glow/持物、HUD/粒子视觉未在本次集成渲染验证；无截图证据。
- 真实 Curios、真实多人网络互通、重启清票据与旧世界迁移未验。运行时票据 GameTest / NBT round-trip 不是实际服务器重启证据。
- [客户端粒子审计](migration_batches/2026-11-22-parity-fix-client/PARTICLE_PARITY_AUDIT.md) 的 17 类自定义粒子仍未完整移植；部分作业/女神实现不代表整类完成。阵型 `HEALING_SPARKLE` 仍用原版近似。
- crafting 未接回的配置仍包括 `Attrs_*`、`Consume_*`、`Attack_*`、`Exp_*`、`Slots_*`、`Death_Time`、`Mod_IC2`、`Polymetal_Ore` 等，详见 [crafting 决定](migration_batches/2026-11-22-parity-fix-crafting-world-config/decision.md)。
- 飞机/坐骑未接入舰船 3D 寻路，见 [AI 审计](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md)。
- 变身没有玩家可用的外部激活入口；不恢复自创激活/档案切换。MorphProfile 缺击杀数与航空机库存；已有变身舰载机计数的补充/持久化未接入航空攻击。见 [客户端激活边界](migration_batches/2026-11-22-parity-fix-client/decision.md#变身激活入口核查) 与 [最终批次验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits)。
- 非目标：本次不补完这些遗留，不以服务端全绿宣称全面 parity，不新增旧版无依据的玩法/开关。
