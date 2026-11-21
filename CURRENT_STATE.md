# Current State

本文件是迁移项目的当前事实唯一来源，也是压缩或换人后的最小交接快照。历史结论见 [MIGRATION-CHANGELOG.md](MIGRATION-CHANGELOG.md)，下一步只看 [ROADMAP.md](ROADMAP.md)。发生冲突时，以本文件为准。

## 快照

- 分支：`devin/parity-fix-items-blocks`（仅推送该分支，不修改 `codex/mc-1.20.1`）。
- 基线提交：`3c9fe42d`，`v0.0.2-alpha`
- 工作区边界：本次仅收口文档；已有实现和证据见 [方块与物品纠偏批次](migration_batches/2026-11-22-parity-fix-items-blocks/)。不清理或覆盖其他改动，不触碰 `/home/ubuntu/baseline`。
- legacy：`src/main/java`、`src/main/resources` 未作为实现目标，继续只读。
- 当前完成口径、阻塞与非目标：以 [本批次状态](migration_batches/2026-11-22-parity-fix-items-blocks/STATUS.md) 为准，不沿用历史批次的全绿／全量等价声明。

## 当前任务

- 当前任务：按 [本批次状态](migration_batches/2026-11-22-parity-fix-items-blocks/STATUS.md) 完成文档与指定分支推送后停止；不创建合并请求。以下既有模块与批次链接保留为历史入口，不代表本次重新验收。
- 已完成 backlog 批次：`WORLDGEN-AUDIT`（[WORLDGEN-AUDIT](migration_batches/2026-09-16-worldgen-audit/)）、`GAMETEST-REPAIR-B4`（[GAMETEST-REPAIR-B4](migration_batches/2026-11-16-gametest-repair/)）、`B5-ORE-TAGS`（[B5-ORE-TAGS](migration_batches/2026-11-16-b5-ore-tags/)）、`GAMETEST-BACKFILL`（[GAMETEST-BACKFILL](migration_batches/2026-11-16-gametest-backfill/)）、`B1-GUI-PARITY`（[B1-GUI-PARITY](migration_batches/2026-11-16-b1-gui-parity/)）、`B2-PACKET-PARITY`（[B2-PACKET-PARITY](migration_batches/2026-11-16-b2-packet-parity/)）、`B3-MULTIPLAYER-TEAM`（[B3-MULTIPLAYER-TEAM](migration_batches/2026-11-17-b3-multiplayer-team/)）与 `B6-INTERMOD-DETAILS`（[B6-INTERMOD-DETAILS](migration_batches/2026-11-17-b6-intermod-details/)）。
- 唯一下一步入口见 [ROADMAP.md](ROADMAP.md)：`NONE`（队列已清零）。
- [MIGRATION-SHIP-MATRIX.md](MIGRATION-SHIP-MATRIX.md) 全 45 行主线域均为 `LOCKED`，不再存在待处理 `NEXT` 舰种行；V0 收口批次证据见 [V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/)。

## 迁移统计

- 基础舰种矩阵共 45 个唯一 class ID，现代规格实际包含 64 个注册变体（45 个 friendly 基础舰种、19 个 `>2000` hostile `*Mob` 变体，已在 [FULL-SHIP-BEHAVIOR-REAUDIT](migration_batches/2026-11-18-full-ship-behavior-reaudit/) 与 1.12.2 `ModEntity` 完全对齐）；逐舰状态以 [MIGRATION-SHIP-MATRIX.md](MIGRATION-SHIP-MATRIX.md) 为准，不在这里复制逐行结论。

模块总览：

| 模块 | 当前状态 | 权威入口 |
| --- | --- | --- |
| M0 基础与注册 | Done (SP mainline) | [SP-CLOSE decision](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md) |
| M1 实体生命周期 | Done (SP mainline) | [SP-CLOSE decision](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md) |
| M2 存档与 runtime state | Done (SP mainline) | [SP-CLOSE decision](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md) |
| M3 命令、网络与菜单 | Done (SP mainline) | [SP-CLOSE decision](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md) |
| M4 AI、路线与队伍 | Done (SP mainline) | [SP-CLOSE decision](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md) |
| M5 舰种行为 | Done (SP mainline) | [SP-CLOSE decision](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md) |
| M6 战斗与资源 | Done (SP mainline; global regression green; override/profile audits green) | [SHIP-ATTACK-PROFILE-AUDIT decision](migration_batches/2026-09-05-ship-attack-profile-audit/decision.md) |
| M7 投射物与反馈 | Done (SP mainline; source-type audit green) | [ALL-SHIP-OVERRIDE-AUDIT decision](migration_batches/2026-09-05-all-ship-override-audit/decision.md) |
| M8 客户端渲染与 GUI | Done (SP mainline; B1 GUI parity completed) | [B1-GUI-PARITY decision](migration_batches/2026-11-16-b1-gui-parity/decision.md) |
| M9 世界内容、掉落与配方 | Done (SP mainline) | [SP-CLOSE decision](migration_batches/2026-09-04-sp-close-singleplayer-mainline/decision.md) |
| V0 验收与发布 | Done (2026-09-16 用户确认) | [V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/) |

## 最近可复用证据

- 最新批次：[方块与物品纠偏证据](migration_batches/2026-11-22-parity-fix-items-blocks/results/)。

- `SHIP-01` 驱逐舰与重巡 Ri runtime 批次：见 [2026-09-03-ship-01-destroyer-ri](migration_batches/2026-09-03-ship-01-destroyer-ri/)。最近一次完整回归为 `All 250 required tests passed`。
- `SHIP-02` Ne/Wo/Ru/Ta/Re 命令、路线与目标边界批次：见 [2026-09-03-ship-02-command-route-target](migration_batches/2026-09-03-ship-02-command-route-target/)。最近一次完整回归为 `All 253 required tests passed`；本批没有客户端/资源/渲染改动，因此未触发客户端烟测。
- `SHIP-03` Transport Wa、Submarine Ka/Yo/So、Carrier Hime combat behavior 批次：见 [2026-09-03-ship-03-combat-behavior](migration_batches/2026-09-03-ship-03-combat-behavior/)。最近一次完整回归为 `All 254 required tests passed`；本批没有客户端/资源/渲染改动，因此未触发客户端烟测。
- `SHIP-04` Airfield/Battleship/Destroyer/Harbour/Isolated Hime visual/projectile 批次：见 [2026-09-03-ship-04-hime-visual-projectile](migration_batches/2026-09-03-ship-04-hime-visual-projectile/)。最近一次完整回归为 `All 256 required tests passed`；Java 17 客户端启动和资源 reload 通过，进世界人工交互留给 V0。
- `SHIP-05` Midway/Northern/Carrier WD/Shimakaze/Nagato special runtime 批次：见 [2026-09-04-ship-05-special-runtime](migration_batches/2026-09-04-ship-05-special-runtime/)。最近一次完整回归为 `All 258 required tests passed`；Java 17 客户端启动、资源 reload 和单人集成连接通过，进世界人工交互留给 V0。
- `SHIP-06` U-511/Ro-500/Submarine Hime/Yamato/Kaga runtime、模型和投射物边界批次：见 [2026-09-04-ship-06-submarine-yamato-kaga](migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/)。最近一次完整回归为 `All 261 required tests passed`；Java 17 客户端启动、资源 reload 和单人集成连接通过，进世界人工交互留给 V0。
- `SHIP-07` Akagi/CA Hime/Akatsuki/Hibiki/Ikazuchi runtime、骑乘燃料边界和模型批次：见 [2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi](migration_batches/2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi/)。最近一次完整回归为 `All 263 required tests passed`；Java 17 客户端启动、资源 reload 和模型日志闸门通过，进世界人工交互留给 V0。
- `SHIP-08` Inazuma/Tenryuu/Tatsuta/Atago/Takao runtime、骑乘边界和模型批次：见 [2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao](migration_batches/2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao/)。最近一次完整回归为 `All 264 required tests passed`；Java 17 客户端启动、OpenGL、资源 reload 和模型日志闸门通过，进世界人工交互留给 V0。
- `SHIP-09` Kongou/Hiei/Haruna/Kirishima/SSNH runtime、模型和投射物边界批次：见 [2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new](migration_batches/2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new/)。最近一次完整回归为 `All 265 required tests passed`；Java 17 客户端启动、OpenGL 4.6、资源 reload 和模型日志闸门通过，进世界人工交互留给 V0。
- `SP-CLOSE` M0-M9 跨模块收口审计：见 [2026-09-04-sp-close-singleplayer-mainline](migration_batches/2026-09-04-sp-close-singleplayer-mainline/)。本批新增 AI priority contract 回归，最终 `All 266 required tests passed`；V0 仍等待可操作的原生客户端窗口。
- `SP-CLOSE` 具体舰种特殊入口审计：见 [ship-override-audit.json](migration_batches/2026-09-04-sp-close-singleplayer-mainline/results/ship-override-audit.json)。45 个非 Mob 类和 19 个 hostile 变体已与 modern catalog/profile 入口对齐，本次无代码变更；可选历史配置开关仍属 parity backlog。
- `HEAVY-SPECIAL-MISS` 重攻击 miss 语义复核：见 [heavy-special-miss](migration_batches/2026-09-04-heavy-special-miss/)。当前 `performCatalogSpecialAttack` 与通用攻击入口均把 miss 保留在投射物路径，现有两个确定性 GameTest 已覆盖，本批无代码变更。
- `HOSTILE-TARGET-RANGE` 敌对目标范围跟进：见 [hostile-target-range](migration_batches/2026-09-04-hostile-target-range/)。低射程回退、Tenryuu/Tatsuta 固定技能搜索半径和 hostile LivingEntity 筛选已完成最小现代适配；反馈 gate 无需变更，最终回归 `All 268 required tests passed`。
- `SHIP-UNIVERSE-AUDIT` 活动旧版实体全集核对：见 [ship-universe-audit](migration_batches/2026-09-04-ship-universe-audit/)。64 个旧版注册行、64 个现代规格行、45 个矩阵基础行差集均为空；40 个仅存在于 `ID.java` 的历史名称已保留为来源缺口，不根据名称猜测实现。
- `MODERN-GAP-AUDIT` 与 `ALL-SHIP-IDLE-SUPPLY`：见 [gap audit](migration_batches/2026-09-04-modern-gap-audit/) 和 [idle supply batch](migration_batches/2026-09-05-all-ship-idle-supply/)。按旧版继承层级将 45 个 `BasicEntityShip` 注册类接入公共 128 tick 路径，Hime 不再被现代 hostile 标志误排除，19 个 hostile Mob 变体继续排除；idle-supply 专项与后续战斗回归均已通过，具体 finding 已关闭。
- `COMBAT-REGRESSION-SUITE`：见 [combat regression batch](migration_batches/2026-09-05-combat-regression-suite/)。四个固定战斗失败已按旧版 Nagato/Tenryuu/Tatsuta/Yamato 入口和潜艇姬投射物规则完成对照；只修正测试夹具/断言，未改生产战斗逻辑。最终 `All 269 required tests passed`。
- `ALL-SHIP-OVERRIDE-AUDIT`：见 [all ship override audit](migration_batches/2026-09-05-all-ship-override-audit/)。64 个注册变体、45 个基础 class 的 override 覆盖无未解释遗漏；旧版光照伤害表、显式 damage type、飞机 direct source 和 Rensouhou 类型已迁移到 modern。`compileJava`、`processResources`、`runGameTestServer` 通过，最终 `All 270 required tests passed`。
- `SHIP-ATTACK-PROFILE-AUDIT`：见 [ship attack profile audit](migration_batches/2026-09-05-ship-attack-profile-audit/)。45 个基础 class、64 个注册变体和 19 个 hostile Mob 变体完成旧版 light/heavy/air channel 与特殊 hook 边界对照；`Battleship Ta` 保留仅轻攻击的有效行为。`compileJava`、`processResources`、`runGameTestServer` 通过，最终 `All 271 required tests passed`。
- Rensouhou 行为批次：见 [2026-09-02-rensouhou](migration_batches/2026-09-02-rensouhou/)。该批次回归为 `All 248 required tests passed`。
- 潜艇审计：见 [2026-09-02-submarine-audit](migration_batches/2026-09-02-submarine-audit/)。审计结论是现有单人必要路径未发现需要新增的现代代码。
- 敌对 Boss 目标与提示审计：见 [2026-09-02-hostile-target-audit](migration_batches/2026-09-02-hostile-target-audit/)。结论是旧版目标筛选与敌方起飞/命中提示抑制路径已有对应实现。
- `GAMETEST-BACKFILL` 测试补齐批次：见 [2026-11-16-gametest-backfill](migration_batches/2026-11-16-gametest-backfill/)。从 Git dangling blob `4d70aebad7532c05ebc34a588c84caedb1bd4102` 完整找回 182 项缺失 `@GameTest`、8 项 helper 与 29 项 import，覆盖 11 个批次回归。用例总规模达到 276 项并全部通过（`All 276 required tests passed :)`，0 failed / 0 skipped）。
- `B1-GUI-PARITY` 界面与容器对齐批次：见 [2026-11-16-b1-gui-parity](migration_batches/2026-11-16-b1-gui-parity/)。逐项对照旧版 10 个 `Gui*` 与 9 个 `Container*` 源码，对齐 LargeShipyard（9 托盘槽位、材料加减面板与黄色库存文本）、LegacyCore（3x3 槽位/尺寸/开关）、Crane（开关状态/等待模式双向切循环）与 DeskReference（单人/多人防 NPE）。276/276 GameTest 全绿通过。
- 上述证据是批次快照，不替代逐舰矩阵；代码再次变化后必须重新验证受影响批次。

## 当前阻塞与边界

- SP mainline（M0-M9 + V0）及 B1–B6 完整 1.12 parity backlog（`WORLDGEN-AUDIT`、`GAMETEST-REPAIR-B4`、`B5-ORE-TAGS`、`GAMETEST-BACKFILL`、`B1-GUI-PARITY`、`B2-PACKET-PARITY`、`B3-MULTIPLAYER-TEAM`、`B6-INTERMOD-DETAILS`）均已全面完成并闭环，无当前阻塞。
- **当前自动化覆盖（2026-11-18 `FULL-SHIP-BEHAVIOR-REAUDIT` 全量两轮复审修复收口后）**：JDK `17.0.3.1` 下 `git diff --check`、`git status -s -- src/main/`（空输出）、`.\gradlew.bat build verifyItemModelCoverage runGameTestServer --no-daemon` 全部通过（**`All 298 required tests passed :)`**，0 failed，0 skipped；证据见 [Re-audit closeout](migration_batches/2026-11-18-full-ship-behavior-reaudit/results/reaudit-closeout.json)）。
- 已建档不可变边界：Metamorph 由原生 `MorphHelper` + `MorphCompatBridge.NOOP`/`SoftDependencyBridge` 承载；IC2 由内部 `RouteEnergyAccess` 与 `ForgeCapabilities.ITEM_HANDLER`/`FLUID_HANDLER` 替代；Curios 采用零硬依赖反射软钩；40 个无 1.12.2 实体源与 `ModEntity.regShipEntity` 注册入口的 `ID.java` 历史名称按 D-009 维持 `HISTORICAL_SOURCE_GAP`（暂待用户决定是否提交）。

## 更新规则

完成批次时只更新：批次记录、[MIGRATION-SHIP-MATRIX.md](MIGRATION-SHIP-MATRIX.md)、本文件的快照/模块状态、[MIGRATION-CHANGELOG.md](MIGRATION-CHANGELOG.md)。不要把测试输出、设计讨论或逐舰长表复制到这里。
