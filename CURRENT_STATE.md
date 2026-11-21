# 当前状态

本文件只保存当前事实与交接快照；历史入口见 [迁移变更索引](MIGRATION-CHANGELOG.md)，下一步只看 [ROADMAP.md](ROADMAP.md)。

<a id="当前任务"></a>
## 当前集成与任务边界

- 分支：`devin/mc-1.21.1-neoforge`，基于固定的 1.20.1 parity 分支 `origin/devin/parity-fix-integration`（用户指定 `5b95cfe`）。旧九批集成历史从 [索引](MIGRATION-CHANGELOG.md#九批-parity-集成收尾) 进入。
- Minecraft **1.21.1**、NeoForge **21.1.267**、ModDevGradle **2.0.148**、Gradle **8.8**、JDK **21**；模组版本 **0.2.0-alpha**。
- 已验证并推送的代码提交：`f9b814a682993d69e08692f69cc98965156f122a`。构建、服务端 GameTest 与兼容性选择见 [1.21.1 批次结果](migration_batches/neoforge-1.21.1/status.md)。
- 当前任务：版本/API 与旧数据适配完成，独立编译、完整构建及全量服务端 GameTest 通过；文档收口后停止。不创建 PR，不修改/推送其他分支，不自动开始后续批次。
- 工作区边界：实现只在 `src/modern/java`、`src/modern/resources`，另有构建配置与迁移文档。`src/main/**` 继续只读，相对固定 parity 基线无差异。
- 唯一下一步入口：[ROADMAP.md](ROADMAP.md)。无服务端验证阻塞；下述未验证项/遗留不因此消失。

## 最近证据与权威入口

- 最后生产/测试代码修改后：JDK 21 的 `compileJava`、`build` 与 `runGameTestServer` 依次通过，`All 345 required tests passed :)`（原 340 项与新增 5 项兼容测试；0 失败）。准确输出、范围与日志校验和见 [本批次证据](migration_batches/neoforge-1.21.1/status.md#verification)。1.20.1 的 340 项结果保留在 [历史集成证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md)，不替代本轮结果。
- 重复实现的归并约束与编号变更只见 [D-018 集成决定](MIGRATION-DECISIONS.md#九批集成的重复实现归并)：菜单网络 D-015、客户端 D-016、crafting D-017、Teitoku D-018，不覆盖 D-012～D-014。
- 既有起重机关联与夹具证据：[物品方块](migration_batches/2026-11-22-parity-fix-items-blocks/results/README.md#integration-merge)、[crafting 集成验证](migration_batches/2026-11-22-parity-fix-crafting-world-config/results/verification.md#integration-merge)。
- 舰种身份与状态只见 [舰种矩阵](MIGRATION-SHIP-MATRIX.md)；本次不扩大逐舰或客户端验收口径。

## 未验证与当前遗留

- 1.21.1 客户端画面/GUI 热区、模型/Glow/持物、HUD/粒子视觉未渲染验证；无截图证据，不能以编译或服务端通过替代。
- 真实 Curios、真实多人网络互通、跨重启票据与完整旧世界升级未验。ForgeCaps、嵌套 ItemStack、流体与坐标已有合成旧数据测试；这不是用户实际存档升级证据，实际升级前仍应备份。
- 本轮未发现必须接受的玩家可见行为差异；这不是所有客户端/第三方模组场景已完全等价的声明。旧基线遗留仍如下。
- [客户端粒子审计](migration_batches/2026-11-22-parity-fix-client/PARTICLE_PARITY_AUDIT.md) 的 17 类自定义粒子仍未完整移植；部分作业/女神实现不代表整类完成。阵型 `HEALING_SPARKLE` 仍用原版近似。
- crafting 未接回的配置仍包括 `Attrs_*`、`Consume_*`、`Attack_*`、`Exp_*`、`Slots_*`、`Death_Time`、`Mod_IC2`、`Polymetal_Ore` 等，详见 [crafting 决定](migration_batches/2026-11-22-parity-fix-crafting-world-config/decision.md)。
- 飞机/坐骑未接入舰船 3D 寻路，见 [AI 审计](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md)。
- 变身没有玩家可用的外部激活入口；不恢复自创激活/档案切换。MorphProfile 缺击杀数与航空机库存；已有变身舰载机计数的补充/持久化未接入航空攻击。见 [客户端激活边界](migration_batches/2026-11-22-parity-fix-client/decision.md#变身激活入口核查) 与 [最终批次验证边界](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md#validation-limits)。
- 非目标：本次不补完这些遗留，不以服务端全绿宣称全面 parity，不新增旧版无依据的玩法/开关。
