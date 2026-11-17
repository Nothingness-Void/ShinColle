# Roadmap

本文件只维护下一步队列，不记录已经完成的详细事实。当前事实见 [CURRENT_STATE.md](CURRENT_STATE.md)，逐舰选择见 [MIGRATION-SHIP-MATRIX.md](MIGRATION-SHIP-MATRIX.md)。

## 当前指针

- 唯一当前队列入口：**`NONE`**（`SHIP-01`–`SHIP-09`、`SP-CLOSE`、`V0-CLIENT-GATE`、`WORLDGEN-AUDIT`、`GAMETEST-REPAIR-B4`、`B5-ORE-TAGS`、`GAMETEST-BACKFILL` 与 `B1`–`B6` 全部队列均已完成并收口，全量 `289/289` GameTest 通过）。
- 舰种矩阵 45 行全为 `LOCKED`，不再从舰种行提取 `NEXT`。
- 40 个无旧版实体源码与 `ModEntity.regShipEntity` 注册入口的 `ID.java` 历史名称按 D-009 维持 `HISTORICAL_SOURCE_GAP`。

## 队列规则

- 任何时刻只保留一个 `NEXT` 批次。
- `SHIP-01` 至 `SHIP-09` 已锁定主线舰种域；当前队列按测试恢复与 backlog 专题批次推进。
- 开工前先创建 [批次记录模板](migration_batches/_TEMPLATE/README.md) 的副本，并冻结 `protocol.md`。
- 批次完成或确认无差异后，才把下一组从 `PENDING` 提升为 `NEXT`。
- 构建失败、GameTest 失败或客户端烟测失败都留在当前批次处理，不跳到下一组。

## 当前队列

| 顺序 | 批次 | 范围 | 进入条件 | 退出条件 |
| --- | --- | --- | --- | --- |
| 1 | `SHIP-01` | 矩阵中前 5 个 `NEXT` 舰种的剩余战斗 cadence、资源消耗、存档字段 | 文档体系静态检查通过 | 已完成，证据见 [SHIP-01](migration_batches/2026-09-03-ship-01-destroyer-ri/) |
| 2 | `SHIP-02` | 矩阵中下一组 5 个 `NEXT` 舰种的命令/路线/目标边界 | `SHIP-01` 完成 | 已完成，证据见 [SHIP-02](migration_batches/2026-09-03-ship-02-command-route-target/) |
| 3 | `SHIP-03` | 矩阵中下一组 5 个 `NEXT` 舰种的行为、攻击、冷却、miss/crit、掉落缺口 | `SHIP-02` 完成 | 已完成，证据见 [SHIP-03](migration_batches/2026-09-03-ship-03-combat-behavior/) |
| 4 | `SHIP-04` | 矩阵中下一组视觉/投射物联动舰种 | `SHIP-03` 完成 | 已完成；证据见 [SHIP-04](migration_batches/2026-09-03-ship-04-hime-visual-projectile/) |
| 5 | `SHIP-05` | 矩阵中下一组 Boss/特殊实体 runtime 联动舰种 | `SHIP-04` 完成 | 已完成；证据见 [SHIP-05](migration_batches/2026-09-04-ship-05-special-runtime/) |
| 6 | `SHIP-06` | 矩阵中下一组潜艇、姬级潜艇、Yamato 和 Kaga 的 hostile/runtime 联动域 | `SHIP-05` 完成；先冻结协议 | 已完成，证据见 [SHIP-06](migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/) |
| 7 | `SHIP-07` | `EntityCarrierAkagi`、`EntityCAHime`、`EntityDestroyerAkatsuki`、`EntityDestroyerHibiki`、`EntityDestroyerIkazuchi` 的剩余行为联动域 | `SHIP-06` 完成；先冻结协议 | 已完成；证据见 [SHIP-07](migration_batches/2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi/) |
| 8 | `SHIP-08` | 矩阵下一组 `NEXT` 的 Inazuma、Tenryuu、Tatsuta、Atago、Takao 联动域 | `SHIP-07` 完成；先冻结协议 | 已完成；证据见 [SHIP-08](migration_batches/2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao/) |
| 9 | `SHIP-09` | 矩阵当前 `NEXT` 的 Kongou、Hiei、Haruna、Kirishima、Submarine Hime New 联动域 | `SHIP-08` 完成；先冻结协议 | 已完成；证据见 [SHIP-09](migration_batches/2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new/) |
| 10 | `SP-CLOSE` | Phase 5-10 单人主线剩余闭环 | `SHIP-09` 完成；协议已冻结 | 已完成；M0-M9 为 `Done (SP mainline)`，V0 已于 `V0-CLIENT-GATE` 收口 |
| 11 | `HOSTILE-TARGET-RANGE` | 用户报告驱动的敌对目标范围、Tenryuu/Tatsuta 技能替换目标和战斗提示边界跟进 | `SP-CLOSE` 静态审计后发现需要单独锁定的行为差异 | 已完成；历史证据见 [HOSTILE-TARGET-RANGE](migration_batches/2026-09-04-hostile-target-range/) |
| 12 | `SHIP-UNIVERSE-AUDIT` | 旧版活动实体注册、现代规格和舰种矩阵的全集一致性核对 | `HOSTILE-TARGET-RANGE` 完成 | 已完成；证据见 [SHIP-UNIVERSE-AUDIT](migration_batches/2026-09-04-ship-universe-audit/) |
| 13 | `MODERN-GAP-AUDIT` | 活动舰种现代共享 runtime 缺口静态扫描 | `SHIP-UNIVERSE-AUDIT` 完成；协议已冻结 | 已完成；证据见 [MODERN-GAP-AUDIT](migration_batches/2026-09-04-modern-gap-audit/) |
| 14 | `ALL-SHIP-IDLE-SUPPLY` | 45 个 `BasicEntityShip` 注册类的公共 128 tick 资源入口与 Transport 经验 | `MODERN-GAP-AUDIT` 发现真实缺口 | 已完成；证据见 [ALL-SHIP-IDLE-SUPPLY](migration_batches/2026-09-05-all-ship-idle-supply/) |
| 15 | `COMBAT-REGRESSION-SUITE` | Nagato 士气事件、Tenryuu/Tatsuta 重型技能阶段、Yamato 双阶段 beam 的失败回归 | `ALL-SHIP-IDLE-SUPPLY` 完成 | 已完成；历史证据见 [COMBAT-REGRESSION-SUITE](migration_batches/2026-09-05-combat-regression-suite/) |
| 16 | `ALL-SHIP-OVERRIDE-AUDIT` | 64 个注册变体的实体 override、攻击源 damage type、光照伤害修正与 modern 共享入口覆盖 | `COMBAT-REGRESSION-SUITE` 完成 | 已完成；历史证据见 [ALL-SHIP-OVERRIDE-AUDIT](migration_batches/2026-09-05-all-ship-override-audit/) |
| 17 | `SHIP-ATTACK-PROFILE-AUDIT` | 45 个已注册基础舰种及 hostile 变体的轻攻、重攻、航空攻击开关与特殊 hook 边界 | `ALL-SHIP-OVERRIDE-AUDIT` 完成 | 已完成；历史证据见 [SHIP-ATTACK-PROFILE-AUDIT](migration_batches/2026-09-05-ship-attack-profile-audit/) |
| 18 | `V0-CLIENT-GATE` | Java 17 客户端启动检查与 V0 单人世界人工验收 | `SP-CLOSE` 完成 | 已完成（2026-09-16 用户确认通过），证据见 [V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/) |
| 19 | `WORLDGEN-AUDIT` | `ShinColleWorldGen`、`WorldGenPolyGravel`、`ChestLootTable` 默认值对照与 `polymetal_gravel.json` 修正 | `V0-CLIENT-GATE` 完成 | 已完成；资源与测试均闭合，证据见 [WORLDGEN-AUDIT](migration_batches/2026-09-16-worldgen-audit/) |
| 20 | `GAMETEST-REPAIR-B4` | 严格对照旧版 1.12.2 源码修复 `GameplayParityGameTests.java` 中 `HEAD` 的 9 个失败测试（含 B4 worldgen 断言） | 2026-11-16 体检发现 `runGameTestServer` 跑 91 项且 82/91 | 已完成；`All 91 required tests passed`，证据见 [GAMETEST-REPAIR-B4](migration_batches/2026-11-16-gametest-repair/) |
| 21 | `B5-ORE-TAGS` | 对照 `ModOres`、`ModRecipes`、39 个 `ore_dict` JSON 配方、`InventoryHelper`、`TileEntityCrane`、`GuiCrane` 与 `EventHandler` 完成 OreDictionary → Forge Tags 迁移与验证 | `GAMETEST-REPAIR-B4` 完成；协议已冻结于 [B5-ORE-TAGS](migration_batches/2026-11-16-b5-ore-tags/protocol.md) | 已完成；`All 94 required tests passed`，证据见 [B5-ORE-TAGS](migration_batches/2026-11-16-b5-ore-tags/) |
| 22 | `GAMETEST-BACKFILL` | 按历史批次记录分批补回 `SHIP-01`~`SHIP-ATTACK-PROFILE-AUDIT` 缺失的回归测试 | `B5-ORE-TAGS` 完成；先冻结协议 | 已完成；All 276 required tests passed，证据见 [GAMETEST-BACKFILL](migration_batches/2026-11-16-gametest-backfill/) |
| 23 | `B1-GUI-PARITY` | 对照旧版 10 个 `Gui*` 与 9 个 `Container*` 的内部交互、按键与 slot 逻辑 | `GAMETEST-BACKFILL` 完成；先冻结协议 | 已完成；10 Gui / 9 Container 对齐，276/276 GameTest 通过，证据见 [B1-GUI-PARITY](migration_batches/2026-11-16-b1-gui-parity/) |
| 24 | `B2-PACKET-PARITY` | 对照旧版 `C2SGUIPackets`、`S2CGUIPackets`、`C2SInputPackets`、`S2CReactPackets` 全部 PID 与同步分支 | `B1-GUI-PARITY` 完成；先冻结协议 | 已完成；全 84 有效 PID 核对、11 项单人/核心补齐，`282/282` GameTest 通过，证据见 [B2-PACKET-PARITY](migration_batches/2026-11-16-b2-packet-parity/) |
| 25 | `B3-MULTIPLAYER-TEAM` | 对照 `TeamData`、`TeamHelper`、`ServerProxy`、`CapaTeitoku`、`TargetHelper`、`OwnerPaper` 与 `KaitaiHammer` 的多人队伍/同盟权限、PVP/友伤与雷达同步 | `B2-PACKET-PARITY` 完成；协议已冻结 | 已完成；Part 1+2 全部收口，`286/286` GameTest 通过，证据见 [B3-MULTIPLAYER-TEAM](migration_batches/2026-11-17-b3-multiplayer-team/) |
| 26 | `B6-INTERMOD-DETAILS` | `ShinColleConfig` + `SystemConfig`、`polyAsMn`、9 条 `/ship*` 指令、动态光方块、`ShipKills`/`Raiden`、Curios 可选联动与外置 mod/`ID.java` 40 历史名称收口 | `B3-MULTIPLAYER-TEAM` 完成；协议已冻结 | 已完成；全量 `289/289` GameTest 通过，队列清零，证据见 [B6-INTERMOD-DETAILS](migration_batches/2026-11-17-b6-intermod-details/) |

## 晋级规则

1. 先完成一个批次的代码/测试和批次证据。
2. 更新对应矩阵或测试文档，以及本文件的批次状态。
3. 更新 `CURRENT_STATE.md` 的快照、阻塞与 `MIGRATION-CHANGELOG.md`。
4. 只有当前 `NEXT` 批次的 `compileJava`、`processResources` 与 `runGameTestServer` 通过后，才把下一项从 `PENDING` 提升为 `NEXT`。

## 终点

单人主线的完成标准见 [MIGRATION-SCOPE.md](MIGRATION-SCOPE.md)（已于 2026-09-16 收口）。当前阶段目标是按队列逐批完成完整 1.12 parity backlog；40 个无旧版实体源码/注册入口的 `ID.java` 历史名称在未获得旧版实现来源前继续保持 `HISTORICAL_SOURCE_GAP`。
