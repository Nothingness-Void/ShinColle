# Migration Test Matrix

本文件只定义测试触发条件和批末策略，不记录某次运行的当前结果。具体结果写入批次 `results/`。

## 固定门

| 门 | 触发 | 检查 |
| --- | --- | --- |
| T0 | 每个批次、每次文档/代码收口 | `git diff --check`；确认 legacy 源码和资源未被修改 |
| T1 | Java 或共享实现变化 | `compileJava`、必要时 `processResources` |
| T1-R | 资源、配方、loot、worldgen 变化 | `cleanProcessResources processResources`，检查输出资源 |
| T1-M | item/model/entity registry 或模型变化 | `verifyItemModelCoverage` |
| T2 | 行为、网络、存档、资源或共享 helper 变化 | `runGameTestServer` 和批次专用断言 |
| T3 | renderer、model、particle、screen、sound 或客户端资源变化 | dev client 启动、资源 reload、进世界烟测 |
| T4 | V0 收口 | 单人世界完整人工流程并记录结果 |

## 按改动追加

| 改动 | 必须覆盖 |
| --- | --- |
| network/menu/command | encode/decode、权限、目标无效、cooldown、资源不足和服务端状态 |
| NBT/runtime/cache | 缺字段、旧字段、默认值、UID、死亡/离线恢复、保存读取 round-trip |
| behavior catalog/AI | 每行为族 hit/miss、crit、cooldown、target validity、resource、summon、loot、AI priority |
| projectile/reaction | 投射物族、释放顺序、命中/未命中/同 owner、玩家反馈隔离 |
| resource/model/renderer | lowercase 路径、可 bake、无静默 fallback、友方/敌方/Boss 可见 |

## SHIP-02 focused coverage

- `ship02CommandBoundariesCoverFiveLegacyVariants`：五个矩阵变体的严格 64 格舰船命令边界、远距离移动坐标、远距离护卫目标。
- `waypointRouteBacktracksUsingLegacyLastWaypoint`：上一 waypoint 记忆、回路反向选择和无出口时保留当前守卫节点。
- `legacyWaypointNbtMigratesOwnerAndRouteLinks`：旧版 `pid`、`wpstay`、路线数组和配对箱字段，以及 UID 所有权。
- `shipCommandServiceRejectsUnauthorizedFarAndDeadShips`、`shipCommandServiceReportsFailureReasons`：无权限/超范围实体和缺失、无效、不可交战目标的命令结果。

## SHIP-03 focused coverage

- `ship03TransportSubmarinesAndCarrierHimeKeepLegacyCombatBoundaries`：Transport Wa、Submarine Ka/Yo/So、Carrier Hime 的攻击通道、待机资源、拾取范围、Yo 轻型导弹、飞机容量和 `0.9F` 起飞高度边界。
- `transportWaKeepsLegacyCargoAndFleetSupplyRules`：128 tick 旧版货物消耗、256 tick 同所有者补给、燃料闸门和经验。
- `carrierHimeKeepsLegacyMeleeMissileLaunch`：航母姬旧版近战导弹释放、延迟命中、起飞位置和冷却。
- `fiveSubmarineProjectileVariantsKeepLegacyMoveAndSpeedRules`、`legacySubmarineVariantsKeepFriendlyRoleAndRingBehavior`：潜艇导弹族、深度移动、隐身、拾取和实体边界。
- `legacyAttackCostsFollowShipClassAndMoraleRules`：舰种资源消耗、grudge、弹药和士气边界。

## SHIP-04 focused coverage

- `ship04HimeLegacySourcesAndProjectileFamilies`：五个姬级 class 的旧版模型源、维度、parser/bake、飞机视觉族、集束导弹、railgun 和黑洞投射物族。
- `ship04HimeModelStateUsesLegacyShowEquipBits`：Airfield、Destroyer、Isolated 的旧版 `showEquip` 位规则，以及 Battleship/Harbour 未控制部件默认可见边界。
- 结果、命令退出码和客户端启动级烟测见 [SHIP-04 results](migration_batches/2026-09-03-ship-04-hime-visual-projectile/results/ship-04.json)。进世界人工交互属于 V0 T4 手工验收，不在本文件复制运行结论。

## SHIP-06 focused coverage

- `ship06SubmarineYamatoKagaLegacyModelsKeepExactSourceAndStateRules`：U-511、Ro-500、Submarine Hime、SSNH、Yamato、Kaga 的旧版模型源、`showEquip` 状态位和可 bake 入口。
- `ship06SelectedModelScaleBranchesMatchLegacySource`：U/Ro、Yamato、Kaga 的旧版 scale-level 分支和运行时渲染投影。
- `ship06SubmarineHimeAndBeamProjectileBoundariesMatchLegacy`：潜艇姬双侧导弹偏移/顺序、host 排除、敌对同队免伤和 miss 不造成伤害。
- `fiveSubmarineProjectileVariantsKeepLegacyMoveAndSpeedRules`、`himeSubmarineProjectilePathUsesLegacyDepthSelection`、`submarineHimeLightAttackLaunchesDoubleMissile`、`ssnhLightAttackLaunchesDoubleMissile`：潜艇导弹族、深度路径、双导弹和 miss 后旧版散射分支。
- `carrierAircraftCapacityAndRuntimeFollowLegacyCvRules`：Kaga 的飞机容量、Zero/T 选择和恢复 runtime 边界。
- 结果、命令退出码和客户端启动级烟测见 [SHIP-06 results](migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/results/ship-06.json)。进世界人工交互属于 V0 T4 手工验收，不在本文件复制运行结论。

## SHIP-07 focused coverage

- `ship07SelectedModelsBakeAndKeepLegacyShowEquipBranches`：Akagi、CA Hime、Akatsuki、Hibiki、Ikazuchi 的旧版 model source、texture lookup、parser/bake 和 `showEquip` 位/条件分支。
- `ship07DestroyerRiderFuelBoundaryMatchesLegacyOverrides`：Hibiki 乘 Akatsuki 的 no-fuel 解体、Ikazuchi/Inazuma 两侧 Raiden no-fuel 解体及状态清理。
- `caHimeRefreshesNightStatsOnLegacyCadence`、`sixDestroyerRiderStateFollowsLegacyMasks`、`akatsukiDismountsRidersOnLegacyFuelOrDamage`、`sixDestroyerRidersBreakFormationAfterDamage`：夜战、六驱逐 mask、damage/fuel cleanup 和 rider boundary。
- `sixDestroyerRidersUseLegacyPassengerPositions`、`sixDestroyerEyeHeightsMatchLegacyClasses`、`friendlyIkazuchiAndInazumaKeepLegacyRaidenPairing`：乘客坐标、fixed eye height、friendly/hostile Raiden 条件。
- `carrierAircraftCapacityAndRuntimeFollowLegacyCvRules`、`projectileProfilesMatchShipRoles`：Akagi/Kaga 继承飞机容量、Zero/T 选择、攻击通道和 visual family。
- 结果、命令退出码和客户端启动级模型日志烟测见 [SHIP-07 results](migration_batches/2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi/results/ship-07.json)。进世界人工交互属于 V0 T4 手工验收，不在本文件复制运行结论。

## SHIP-08 focused coverage

- `ship08SelectedModelsBakeAndKeepLegacyShowEquipBranches`：Inazuma、Tenryuu、Tatsuta、Atago、Takao 的旧版模型源、texture lookup、parser/bake 和精确 `showEquip` 位分支。
- `atagoAndTakaoReactionAndMountBehaviorMatchLegacyClasses`、`heavyCruiserTypeFiveMissilesSpawnLegacyStaticFields`：Atago/Takao 的 reaction、mounted 边界、继承攻击路径和 type-five 重炮条件。
- `tenryuuSkillPhaseFollowsLegacyDashTiming`、`tatsutaSkillPhaseFollowsLegacyControlTiming`：Tenryuu/Tatsuta 技能阶段、计时、目标与资源边界。
- `friendlyIkazuchiAndInazumaKeepLegacyRaidenPairing`、`ship07DestroyerRiderFuelBoundaryMatchesLegacyOverrides`：Inazuma 与既有 Raiden/驱逐骑乘配对和燃料解体边界回归。
- 结果、命令退出码和客户端启动级模型日志烟测见 [SHIP-08 results](migration_batches/2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao/results/ship-08.json)。进世界人工交互属于 V0 T4 手工验收，不在本文件复制运行结论。

## SHIP-09 focused coverage

- `ship09KongouFamilyModelsAndRuntimeBoundariesMatchLegacy`：Kongou、Hiei、Haruna、Kirishima 和 SSNH 的旧版模型源、texture lookup、`showEquip` 状态、parser/bake、维度、光环、重炮和 SSNH 深度/骑乘入口。
- `kongouFamilyFiveShipCombatHooksKeepLegacyCannonFeedback`、`hostileDestroyerAndKongouModelStatesKeepLegacyDefaults`：四艘金刚型的 friendly/hostile combat feedback、BossInfo、scale 和初始模型状态。
- `catalogHeavyMissesRemainProjectileOnly`：共享 `heavy + canMiss + justLaunch` 分支的 miss 不造成直接伤害，并保留 missed projectile 标记。
- `submarineHimeKeepsLegacyGeometryAndRingBehavior`、`ssnhLightAttackLaunchesDoubleMissile`：SSNH 几何、深度、隐身、双导弹、miss 散射和宿主/目标边界。
- 结果、命令退出码和客户端启动级模型日志烟测见 [SHIP-09 results](migration_batches/2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new/results/ship-09.json)。进世界人工交互属于 V0 T4 手工验收，不在本文件复制运行结论。

## SP-CLOSE focused coverage

- `singlePlayerAiPriorityContractMatchesLegacyBoundaries`：把 `death/stop/sit > explicit command > active combat target > auto supply > pickup > idle/follow` 固定为可回归的 catalog 契约，并验证坐下、显式移动和命令清除边界。
- `hostileBossUsesLegacyPlayerTargetRules`、`hostileBossDropsInvalidTargetBeforeNavigation`、`fiveHimeBossesKeepLegacyInvisibleTargetBoundary`：旧版 `EntityAIShipRangeTarget` / `TargetHelper.SelectorForHostile` 对应的 attack-range leash、creative/无敌/观察者、不可见目标、敌对舰互斥和 stale target 清理。
- `combatReactActionbarScopeRequiresParticipant`、`hostileCombatReactDoesNotAllowPlayerActionbar`：敌方飞机/召唤物的 launch/hit 反应只走客户端效果总线，不向玩家 actionbar 冒充友方提示。
- M0-M9 的跨模块证据、批末命令和 V0 状态见 [SP-CLOSE results](migration_batches/2026-09-04-sp-close-singleplayer-mainline/results/sp-close.json)。

## COMBAT-REGRESSION-SUITE focused coverage

- `nagatoMoraleEventMatchesLegacyNearbyTargets`：旧版 16/12/16 搜索盒、驱逐舰/Northern Hime 筛选、7650 阈值、每目标 150 士气和公共 idle morale 顺序。
- `tenryuuHeavyAttackRunsLegacySkillPhases`、`tatsutaHeavyAttackRunsLegacySkillPhases`：旧版两阶段技能、level 缩放、资源消耗、冷却和直接 GameTest tick 夹具边界。
- `yamatoHeavyAttackUsesLegacyTwoPhaseBeam`：旧版 phase 1 准备、phase 2 beam、资源门、冷却和 beam 延迟命中。
- `submarineHimeLightAttackLaunchesDoubleMissile`、`ssnhLightAttackLaunchesDoubleMissile`：旧版双侧发射、miss 散射、深度选择，以及 `EntityAbyssMissile` 的短距 ARC 编号/直线运动和加速度回退规则。
- 结果、命令退出码和旧版逐入口结论见 [COMBAT-REGRESSION-SUITE results](migration_batches/2026-09-05-combat-regression-suite/results/combat-regression-suite.json)。最终 required GameTest 为 `269/269`。

## ALL-SHIP-OVERRIDE-AUDIT focused coverage

- `allShipDamageTypesAndLightModifiersMirrorLegacy`：45 个基础 class ID 的 explicit damage type、hostile normalized path、Transport `UNDEFINED`、`BattleshipRe`/设施类 `AVIATION`、Rensouhou 大小类型和旧版 day/night 表取值顺序。
- `LegacyShipEntity.hurt`：防御修正后接入目标光照、攻击源类型和宿主夜视效果；飞机攻击携带 aircraft direct entity，普通投射物仍从宿主 source 识别。
- 结果、命令退出码和逐类覆盖结论见 [ALL-SHIP-OVERRIDE-AUDIT results](migration_batches/2026-09-05-all-ship-override-audit/results/all-ship-override-audit.json)。最终 required GameTest 为 `270/270`。

## SHIP-ATTACK-PROFILE-AUDIT focused coverage

- `registeredAttackChannelsMirrorLegacySources`：45 个 friendly 基础 class 的 light/heavy/air 有效开关、19 个 hostile Mob 变体的 hostile override，以及每个注册基础规格的特殊 hook channel boundary。
- `EntityBattleshipTa`：断言旧版只有轻型 Rensouhou 入口；继承的 raw heavy flag 不得打开 modern generic heavy hook。
- 结果、命令退出码和逐类对照结论见 [SHIP-ATTACK-PROFILE-AUDIT results](migration_batches/2026-09-05-ship-attack-profile-audit/results/attack-profile-audit.json)。最终 required GameTest 为 `271/271`。

## B5-ORE-TAGS focused coverage

- `b5OreTagResourcesAndConditionalRecipesParity`：验证 `forge:paper` 与 8 个跨 mod 兼容空 Tag 资源存在，默认 Forge Item Tag 成员表（含 `polyAsMn=false` 边界），以及 13 个 `forge:not(forge:tag_empty)` 跨 mod 条件配方在无第三方 Tag 时保持禁用。
- `b5LegacyOreTagRecipesParity`：覆盖 1.12 修正/补齐的 `abyssmetal_from_iron_and_grudge`、`ammo`/`ammo1`/`ammo2`/`ammo3`、`ammo_from_*`/`ammo2_from_*`、`blockframe`、`blockvolblock`/`_alt`、`blockvolcore`/`_alt`、`blockwaypoint`（竖直拒横向）、`combatration`（两行拒三行）、`combatration2`（原版生鱼）、`marriagering`（`abyssnugget` 拒 `abyssmetal`）、`recipepaper`（蓝染料）、`repairgoddess`/`_alt`、`shiptank1`。
- `b5CraneAndTaskOreTagFilterMatchingParity`：验证 `CraneBlockEntity` / `CraneTerminalMenu` 按钮 `6..8`（`checkMetadata` / `checkOredict` / `checkNbt` 默认 `false` 与 `"meta"`/`"dict"`/`"nbt"` NBT 往返）、Tag 兄弟物品匹配与 `LegacyShipBehaviorCatalog#matchesLegacyTaskItem` 在含耐久损毁下保持 `checkNbt` 与 `checkMetadata` 独立性。
- 详细命令与计数见 [B5-ORE-TAGS results](migration_batches/2026-11-16-b5-ore-tags/results/b5-ore-tags.json)。

## 当前自动化覆盖说明（2026-11-16 GAMETEST-BACKFILL 收口）

- 2026-11-16 完成 `GAMETEST-BACKFILL` 批次，从 Git Dangling Blob `4d70aebad7532c05ebc34a588c84caedb1bd4102` 完整补齐 `SHIP-01` 至 `SHIP-ATTACK-PROFILE-AUDIT` 缺失的 182 项核心回归用例、8 项配套辅助方法与 29 项 import。
- 2026-11-16 实测现状（`GAMETEST-BACKFILL` 收口后）：
  - `git diff --check`、`compileJava`、`runGameTestServer` 全部通过（exit `0`）。
  - `runGameTestServer` 运行全量 `276` 项：`All 276 required tests passed :)`（`276 passed / 0 failed / 0 skipped`）。
  - 既有 94 项测试（含 `GAMETEST-REPAIR-B4` 修复的 9 项与 `B5-ORE-TAGS` 新增的 3 项）与补回的 182 项测试全部绿灯通过。
- 详细执行与验证结论见 [GAMETEST-BACKFILL results](migration_batches/2026-11-16-gametest-backfill/results/gametest-backfill.json) 与 [decision.md](migration_batches/2026-11-16-gametest-backfill/decision.md)。

## B1-GUI-PARITY focused coverage

- `LargeShipyardMenu` & `LargeShipyardScreen`: 验证去除 (9, 83) 错误 slot，完整对齐 9 个托盘输入 slot（y=116, x=25..169），总计 46 个 slot；对齐 `BUTTON_INVENTORY_MODE`（出货模式 UV 覆盖）、4 行资材选择框与 8 个增减按钮（+/- 1000..1）；并在 x=125 居中黄色显示 `matStock0..3`。
- `LegacyCoreMenu` & `LegacyCoreScreen`: 验证 3x3 燃料槽（62 + col*18, 19 + row*18）、玩家背包（8, 84）与快捷栏（8, 142）对齐 `gui_vol_core.png` 贴图槽位；电量进度条对齐（38, 59-chargeBar, 12, chargeBar）；电源切换按钮（7, 6, 13, 13）与激活贴图 UV（12, 166, 13, 13）对齐。
- `CraneTerminalMenu` & `CraneTerminalScreen`: 验证电源切换按钮（7, 6, 13, 13）与激活贴图 UV（176, 0, 13, 13）；等待模式按钮支持右键倒序递减。
- `DeskReferenceScreen`: 验证多人模式下 `getSingleplayerServer()` 安全判空守护，避免 Dedicated Server 运行抛出 NPE。
- 详细执行与验证结论见 [B1-GUI-PARITY results](migration_batches/2026-11-16-b1-gui-parity/results/b1-gui-parity.json) 与 [decision.md](migration_batches/2026-11-16-b1-gui-parity/decision.md)。

## Block harvest & beacon base coverage (2026-11-16 收尾补充)

- `blockHarvestToolTagsAndBeaconBaseMirrorLegacy`：验证 `src/modern/resources/data/minecraft/tags/blocks/` 下新增的 `mineable/pickaxe`（level-0 7 种 + `blockpolymetalore` + `blocksmallshipyard`）、`mineable/shovel`（5 种怨念/砾石块）、`needs_stone_tool`（`blockpolymetalore`）、`needs_diamond_tool`（`blocksmallshipyard`）与 `beacon_base_blocks`（7 种 1.12 `isBeaconBase` 方块），以及石镐挖掘 `blockpolymetalore` 掉落 `1..2` 个多金属锭、`blockpolymetalgravel` 掉落自身；最终全量 `cleanProcessResources build runGameTestServer` 通过 **`All 277 required tests passed :)`**（277/277）。

## B2-PACKET-PARITY focused coverage

- `b2PacketCodecRoundTripsEncodeAndDecodeCleanly`：`ServerboundGameplayCommandPacket` 新增 `SET_FORMATION`、`SET_UNIT_NAME`、`SYNC_DESK_FUNC`、`OPEN_MOUNT_SHIP_INVENTORY`、`MOUNT_MOVE`、`SET_SHIP_HIT_ZONE` 的 `encode`/`decode` 往返、`fromOrdinal` 越界回退，及 `ServerboundShipCommandPacket` 编码往返。
- `b2WaypointAndDeskBlockEntitySyncUpdatePackets`：`WaypointBlockEntity` 与 `DeskBlockEntity` 的 `getUpdateTag()`/`ClientboundBlockEntityDataPacket` 更新包与 `SYNC_DESK_FUNC` 四字段同步。
- `b2PointerModeFormationAndUnitNameHandlers`：`SyncPlayerItem` 跨模式切换至单舰模式自动选中首舰与同组开关保留选区、`SetFormation` `> 4` 舰生效门控、`SetUnitName` 无提督 Team 条件下的分队独立命名。
- `b2OpToolToggleRequiresOpPermission`：`SetUnatkClass` 服务端 level-2 OP 权限校验（拦截非 OP 玩家，放行 OP 增删世界规则）。
- `b2MountMoveOpenInventorySkillAndHitZoneParity`：`PlayerSkillHostResolver` 与 `MorphHelper` 对 `LegacyShipMountEntity`（`MOUNT`）及背负舰娘乘客（`RIDER`）技能解析、`OPEN_MOUNT_SHIP_INVENTORY` 权限门控、`MOUNT_MOVE` WASD+Jump 接管、以及 `SET_SHIP_HIT_ZONE` 摸头区（`>= 75`）`+30` 士气反应。
- 详细命令、全 84 PID 矩阵与计数见 [B2-PACKET-PARITY results](migration_batches/2026-11-16-b2-packet-parity/results/b2-packet-parity.json)。

## B3-MULTIPLAYER-TEAM (Part 1 + Part 2) focused coverage

- `b3TeamLifecycleCooldownAndNameValidation`：验证 `TeitokuData.INITIAL_TEAM_COOLDOWN`（`20`）默认初始化与缺字段恢复、初始冷却门控、归一化队名长度 `>= 2` 限制、`TeitokuSavedData.getUid(UUID)` 只读反查、`disbandOwnTeam` 冷却门控与 `TeamSavedData.createTeam` 同队长旧队清理及盟友/黑名单引用清洗。
- `b3TeamAllyBannedExistenceAndBlockOwnershipPermissions`：验证 `TeitokuHelper.isAlly` / `isBanned` 在不同队 ID 时要求双侧队伍均存在，以及 `WaypointBlockEntity.canEdit` / `PlayerOwnerData.canEdit` 放行所有者、同盟与 level-2+ OP、拒绝中立玩家（含 `uid=0` 时按 UUID 反查 `TeitokuSavedData`）。
- `b3Part2FriendlyFirePvpModesAndCommandRevengeTargeting`：验证 `ShinColleConfig` 的 `friendlyFire`、`dmgSvS`（20% vs 100%）、`shipAttackPlayer` `0..3`、`mobAttackPlayer` 对舰娘与召唤物（`LegacyShipRensouhouEntity`）受伤与 `canEngage` 判定，以及 `canCommandOrRevengeEngage` 允许手动指令攻击中立非同盟目标同时拒绝同盟舰船。
- `b3Part2OwnerPaperKaitaiHammerAndDeskRadarSync`：验证 `OwnerPaper` 潜行右键移交所有权（含 `ServerLevel.players()` 回退解析）与 `emotion=2`、潜行右键 `KaitaiHammer` 4 类材料掉落及 `easyMode` 大型舰缩减、左键一击回收生成蛋，以及 `REQUEST_DESK_RADAR` / `DeskRadarContacts` 多人同步且不误清本地队伍缓存。
- 详细命令与计数见 [B3-MULTIPLAYER-TEAM results](migration_batches/2026-11-17-b3-multiplayer-team/results/README.md)、[b3-part1.json](migration_batches/2026-11-17-b3-multiplayer-team/results/b3-part1.json) 与 [b3-part2.json](migration_batches/2026-11-17-b3-multiplayer-team/results/b3-part2.json)。

## B6-INTERMOD-DETAILS focused coverage

- `b6SlashShipCommandsParity`：通过服务端 `Commands#performPrefixedCommand` 验证全部 9 条 `/ship*` 指令（`/shipinfo`、`/shipemotes`、`/shipupdateowneruid`、`/shipattrs 50 10..10` 与 `0..100` 存档往返、`/shipstopai`、`/shipchangeowner`、`/ship list|get|del`、`/shipcleardrop` 与 `/shipkill` 敌我隔离）。
- `b6DynamicLightBlocksSearchlightAndFlareDecay`：验证 `LightBlockHelper.placeLightBlock` 在空气放置 `BLOCK_LIGHT_AIR`（亮度 `15`）、在水中放置 `BLOCK_LIGHT_LIQUID`（保留水流体状态），`LightBlockEntity.serverTick` 120 tick 期满自动还原原方块，以及 `ShinColleConfig.canFlare()` 对照明光方块的开关控制。
- `b6SystemConfigPolyAsMnShipKillsAndRaidenLabel`：验证 `TeitokuHelper.buildGameplayStateTag` → `applyClientState` `SystemConfig` 同步与 `DeskTerminalMenu.stillValid` 使用 `closeGuiDist`、`PolyAsMnCondition`（`shincolle:poly_as_mn`）与 `LegacyItemMatchHelper.matchesTag` 开关、舰娘及 `LegacyShipRensouhouEntity` 的 `ShipKills` 累加和 `Ikazuchi` `Raiden` 角色标签（`gui.shincolle.ship_inventory.role.raiden`）。
- 最终全量验证（JDK `17.0.3.1`）：`.\gradlew.bat build verifyItemModelCoverage runGameTestServer --no-daemon` 通过，**`All 289 required tests passed :)`**（`289 passed / 0 failed / 0 skipped`），证据见 [B6-INTERMOD-DETAILS results](migration_batches/2026-11-17-b6-intermod-details/results/b6-closeout.json)。

## FULL-SHIP-BEHAVIOR-REAUDIT focused coverage

- Round 1（4 项综合 `@GameTest`）：`abyssalHimeBaseSpecsAreFriendlyTamedPickupAndAuraEnabled`、`submarineHimeStealthAndShooterDistanceDodgeMatchLegacy`、`baseShipLifecycleRepairMoraleRegenAndFleeGoalMatchLegacy`、`shipSpecificHooksMountOrientationAndModelCatalogsMatchLegacy`。
- Round 2（5 项综合 `@GameTest`）：`round2HurtImmunitiesVoidRescueGoddessAndResistanceMatchLegacy`、`round2SlotFormationBuffsMinMovAndEnchantScalingMatchLegacy`、`round2FishingCraneAndPointerTargetingMatchLegacy`、`round2AircraftCooldownShootableTorpedoAndRensouhouMatchLegacy`、`round2DeathRecoveryEggXpCostAndHeldCombatRationMatchLegacy`。
- 最终全量验证（JDK `17.0.3.1`）：`.\gradlew.bat build verifyItemModelCoverage runGameTestServer --no-daemon` 通过，**`All 298 required tests passed :)`**（`298 passed / 0 failed / 0 skipped`），证据见 [FULL-SHIP-BEHAVIOR-REAUDIT results](migration_batches/2026-11-18-full-ship-behavior-reaudit/results/reaudit-closeout.json)。

## 批末命令

在 Windows JDK 17 环境中按批次需要执行：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.3.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
git diff --check
.\gradlew.bat compileJava
.\gradlew.bat processResources
.\gradlew.bat runGameTestServer
```

不要在每个小改动后重复启动游戏；一个批次完成后统一执行所需门，并在 `results/` 记录命令、JDK、退出状态、测试计数和日志路径。

## PARITY-FIX-CRAFTING-WORLD-CONFIG（2026-11-22）

- 命令：`bash ./gradlew compileJava --no-daemon`、`bash ./gradlew runGameTestServer --no-daemon`（JDK 17）。
- 结果：`compileJava` 通过；GameTest `297` 项中 `296` 通过，`passiveAiStopsAutomaticRangeTargeting` 失败（基线 `8f1d2fd` 同样失败）。删除 `progressionAdvancementsLoadAndFriendlyShipTriggerAwards`；`chestLootInjectorUsesLegacyConfigLootDefaults` 改为断言跳过 `shincolle:Torpedo` 与整数 rolls；`worldgenResourcesKeepLegacyPolymetalPlacement` 改为断言 `shincolle:polymetal_gravel` feature 与 `Polymetal_Gravel*` 默认值。证据见 [verification](migration_batches/2026-11-22-parity-fix-crafting-world-config/results/verification.md)。
