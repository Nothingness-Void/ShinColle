# Batch Status: B3-MULTIPLAYER-TEAM

- 状态：`COMPLETED`（Part 1 与 Part 2 全部收口，全量 `286/286` GameTest 全绿通过）
- 开始：2026-11-17
- 最近更新：2026-11-18
- 当前阻塞：无

## Completed work

### Part 1（核心队伍生命周期、跨维同步与方块同盟/OP 权限）
- 创建 `migration_batches/2026-11-17-b3-multiplayer-team/` 并冻结 `protocol.md`。
- 完成 `TeitokuData` 初始冷却 `20`、`TeitokuSavedData.getUid(UUID)`、`TeamSavedData.removeStaleTeamsByLeaderName`、`TeitokuHelper` 跨维度同步 / `>=2` 字符队名 / 双队存在性校验、`PlayerOwnerData.canEdit` 所有者+同盟+level-2 OP 权限与 UID 回退反查。
- 回归验证：`284/284`（[results/b3-part1.json](results/b3-part1.json)）。

### Part 2（PVP/友伤开关、交互权限、召唤物/投射物门禁与多人 Desk Radar 同步）
- 新增 [ShinColleConfig.java](../../src/modern/java/com/lulan/shincolle/config/ShinColleConfig.java)（并在 [ShinColle.java](../../src/modern/java/com/lulan/shincolle/ShinColle.java) 注册 `COMMON_SPEC` + `CLIENT_SPEC`），先接入 B3 所需的 `friendlyFire`、`shipAttackPlayer`、`mobAttackPlayer`、`dmgSvS`、`easyMode`、`kaitaiAmountSmall`、`kaitaiAmountLarge`、`radarUpdate`。
- 对齐 `LegacyShipEntity` 受伤防御（非敌对舰娘非主人/创意玩家 `!friendlyFire` 免疫、`isLegacyFriendlyFireAllowed` + 友方舰攻击 `dmgSvS * 0.01F` 百分比缩放、防御反伤排除同盟）、目标筛选（新增 `canCommandOrRevengeEngage` 允许手动指令与复仇反击中立目标同时拒绝同盟、`canEngage` 接入 `mobAttackPlayer()` 与 `shipAttackPlayer()` 0..3 模式及友好舰船 PVP `aiAllowPvp && isBanned` 校验）。
- 补齐 `LegacyShipRensouhouEntity`、`LegacyShipAircraftEntity` 召唤物 `hurt` 友伤/所有者/同盟门禁、`LegacyShipProjectileEntity` 同盟接触过滤、`OwnerPaper`（`emotion=2` + `MARRY` 音效 + `OwnerPaperItem` 全服维度/关卡在线玩家回退解析）、以及 `KaitaiHammer` 左键一击回收与潜行右键按舰种解体 4 类掉落 + `6`/`8` 情绪。
- 新增 `GameplayCommandType.REQUEST_DESK_RADAR`、`TeitokuHelper.buildDeskRadarPayloadTag`、局部缓存保护与 `DeskReferenceScreen` 打开/周期服务端雷达联系人及世界规则同步。
- 新增 2 项 B3 Part 2 `@GameTest`；完整验证通过 **`All 286 required tests passed :)`**（[results/b3-part2.json](results/b3-part2.json)）。

## Evidence links

- [README.md](README.md)
- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/README.md](results/README.md)
- [results/b3-part1.json](results/b3-part1.json)
- [results/b3-part2.json](results/b3-part2.json)
