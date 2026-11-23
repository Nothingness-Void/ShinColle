# Migration Test Matrix

## Ship smoke parity

- Seven LegacyShipSmokeGameTests (PR17/18 adds ordinary type20/limiter and per-hostile scale/anchor coverage; corrects older friendly-kind/Hiei expectations): registered type-43 scale/spacing/velocity, HP type/count/client-16-tick cadence/empty states, exact hostile width*1.5 and swapped flame offsets, friendly/hostile death quality/scale/offsets, codec/network roundtrip and malformed/missing scale. HP cadence has no alive-state restriction, matching legacy; the separate existing chimney alive guard is retained.
- Full required tasks: Forge JDK17 `bash ./gradlew build runGameTestServer`; Neo JDK21 `./gradlew build runGameTestServer`. Pass requires all required tests, not just new smoke tests. [Exact final output and existing radar failure evidence](migration_batches/2026-11-23-ship-smoke/decision.md).
- T3: Neo runClient DISPLAY=:0, actual minor/moderate/heavy and Kongou chimney/rise screenshots. Existing death callbacks/hostile variants are source/pure-emission checked, not separately visually exercised. Aircraft lifecycle is excluded by explicit approval.


## Pointer water movement

- `PointerWaterMoveGameTests`: six real registered-goal ships on water and land, 32-tick path startup and 384-tick legacy arrival tolerance; independent legacy mutex oracle; real fluid/land ray Y, empty selection, dry/sitting and mutex contract. Forge advances/restores the server AI scheduling clock in its synchronous fixture.
- Full default JDK17/21 `build runGameTestServer`; [exact results, initial failures and limits](migration_batches/2026-11-23-pointer-water-move/decision.md).

## Merged PR11 review follow-ups

- `LegacyItemMatchGameTests`: both common tag namespaces; water/lava rejection through actual crane load/unload filters, identical items, material-specific ingots, generic-material rejection, ore-off and empty cases. Temporary holder tags restored in finally; no production fixture resources.
- `LegacyDropMarkerPersistenceGameTests`: real ForgeData entity load/native save/reload, Desk radar payload and actual shipcleardrop, ordinary-item exclusion, missing/malformed/false marker cases. Neo additionally checks native-value precedence and unrelated-data isolation; Forge uses native load with no production migration.
- Full requested default `build runGameTestServer`, JDK17/21; [exact evidence and limits](migration_batches/2026-11-22-review-followups/result.md). No source/resource/version input changed after the final passing gates.

## PR11 client ownership review

- `MorphAndTeamParityGameTests.pointerClientOwnershipUsesSyncedUuidWithoutNumericOwnerCache`: numeric owner cache 0 with synchronized owner UUID and ship UID retained; normal owned ship/mount and teammate sneak consumption, non-team fall-through. Restore numeric field before server cleanup. Existing foreign/null/non-ship/sneak+sprint/long-ray dispatch cases remain required.
- Run both full build/GameTest commands documented below; pass requires all required tests. This fixture represents client data availability, not an actual LocalPlayer packet replay. [Exact results](migration_batches/2026-11-22-riding-aircraft-formation/pointer-owner-result.md).

## Recovery / PR11 / empty-buffer crane follow-up

- `LegacyKilledShipPersistenceGameTests` retains actual creative-player `/tp` into initially unloaded Nether then `/ship get`; verifies destination manager registration and explicitly advances ENTITY_TICKING readiness before the unchanged identity, both-live/age>200, survivor-cache, exact200/dead/reverse-age and kill/disk persistence assertions. Visibility blocker resolved; see [latest evidence](migration_batches/2026-11-22-riding-aircraft-formation/section-readiness-profile-result.md).
- `LegacyAircraftBlockCollisionGameTests.elevatedGuardRoutesClearWallTopForReportedMountsAndFortress`: five actual guarded profiles (eggs32/22/23/35/33), eligible target five blocks higher, whole-bbox solid exclusion every tick and complete wall passage; dedicated 48x32x48 template. Existing finite-wall/all-eight-mount/empty-path coverage retained unchanged.
- `MorphAndTeamParityGameTests.pointerTargetConsumptionRetainsLegacyModifierFallthrough`: own normal/mount, foreign/empty/non-ship, sneak outside/current-team, sneak+sprint clear-team eligibility; shared predicate coverage, not a client input/visual replay. Existing long-ray/crane-dispatch cases retained.
- `CraneFluidWaitGameTests.emptyCraneTankDoesNotBypassLegacyContainerWaits`: production ending-dispatch full/empty modes with empty crane tank; empty bucket, partial ship tank, filled bucket, non-container, empty inventory. Existing nonempty-buffer transfer/cycle coverage retained. Client full-route replay outstanding.
- JDK17 Forge `bash ./gradlew --no-daemon -I /home/ubuntu/shincolle-curios-forge.init.gradle build runGameTestServer`; JDK21 Neo `bash ./gradlew --no-daemon -I /home/ubuntu/shincolle-queue-gametest.init.gradle build runGameTestServer`. The isolated Neo init must align both gameDirectory and syncGameTestStructures destination. Pass criterion: ALL required tests; final outputs in latest evidence, not earlier partial gates.

## Reported GUI/audio regressions

JDK17 `bash ./gradlew --no-daemon build` and `runGameTestServer`: 345/345.
`python3 src/modern/scripts/check_gui_audio_parity.py` checks the unchanged radar
body, restored shipyard text policy and historical frozen-hour clock behavior.
The new GameTest checks 72 menu-message combinations. NeoForge JDK21 passes
the same Gradle tasks with 353/353 GameTests; final scale-8 GUI/audio evidence is
in the [batch record](migration_batches/2026-11-22-gui-audio-regressions/STATUS.md).

## Final NeoForge land/water policy and parity-base merge

JDK21 build and full GameTests **352/352** passed after the source-water
correction. The shared render policy tests default/other/explicit state bits,
standing/sitting and negative/zero/shallow/deep liquid depth without rewriting
stored state. `check_ship_pose_parity.py` checks 45 models with only the two
approved Hime call-site exceptions, then measures 64 variants / 1536 cases.
Targeted VM land/water, other standing/sitting ships and desk/crane placed/held
screenshots are recorded in [final evidence](migration_batches/neoforge-1.21.1/status.md#rendering-backport-and-approved-land-legs).
## Forge rendering backport and Hime land legs

- JDK17：`bash ./gradlew --no-daemon build`，`bash ./gradlew --no-daemon runGameTestServer`（原 wrapper 无执行位，等价调用；不改构建配置）。
- `python3 src/modern/scripts/check_render_resources.py`；JDK17 `python3 src/modern/scripts/check_ship_pose_parity.py --csv migration_batches/rendering-backport-1.20.1/ship-pose-audit.csv`。
- 保留四项渲染 GameTests：运行时尺寸/眼高、办公桌非遮光、方块/物品模型与纹理完整性、Destroyer Hime 默认与显式 leg bit 的陆/水视觉策略及存储不变。全部 344 项通过；45 模型源码、1536 姿态 CPU 检查及实际客户端烟测范围见 [证据](migration_batches/rendering-backport-1.20.1/STATUS.md)。

本文件定义测试触发条件、覆盖范围和批末策略，并提供批次结果摘要；精确执行证据写入批次 `results/`。

## Destroyer Hime hover audit

- JDK 21：`./gradlew --no-daemon compileJava`，`./gradlew --no-daemon runGameTestServer`。
- 编译/客户端依赖准备后：`python3 src/modern/scripts/check_ship_pose_parity.py --csv migration_batches/neoforge-1.21.1/ship-pose-audit.csv`。
- 通过条件：全注册模型源码与旧版一致（仅三项明确 API 别名）；外层姿态调用保留；64 变体/768 采样姿态均产生有限顶点；Destroyer Hime 的无腿悬浮和开腿落地均保留旧版行为。源码/CPU 审计不替代实际 draw path，须同 Y 比较 state 0/16 的站立、坐下客户端截图。
- [范围、352 项结果、CSV 与截图证据](migration_batches/neoforge-1.21.1/decision.md)。强制无腿落地尚未获准偏离旧版，不作为已修复项目。

## 2026-11-22 方块与物品纠偏覆盖及结果

- 批次：[方块与物品旧版行为纠偏](migration_batches/2026-11-22-parity-fix-items-blocks/)；命令与原始输出摘录见 [执行证据](migration_batches/2026-11-22-parity-fix-items-blocks/results/)。
- `bash ./gradlew compileJava` 通过；`bash ./gradlew runGameTestServer` 最终 **301／302 通过**，任务因一项必需测试失败而返回非零，不能称为全绿。
- 唯一失败 `passiveaistopsautomaticrangetargeting` 在用户独立确认的未改动 `origin/codex/mc-1.20.1` 基线中同样失败；基线为 298 项、2 失败。基线另一失败 `round2hurtimmunitiesvoidrescuegoddessandresistancematchlegacy` 本次通过，疑似不稳定，未定位原因。
- `shipyardLiquidFuelAndRuntimeConfig` 及大型造船测试：运行时配置、小型罐容量、拒绝水／禁止外抽、40mB 换能倍率、缺燃料保留进度。
- `craneCycleTransfersShipTankLavaThroughInternalBuffer`、`craneRouteTransfersFilteredItemsBetweenChestAndShip`：warmup、16 tick 边界、内部液体中转、船侧速率及过滤装卸。
- `craneCycleWaitsForInventoryThenCompletesEmptyWork`：缺配对不结束、空工作结束、无 IC2、状态／计时、红石和下一路标；最终回归也覆盖完成后跟随距离保留 2。
- `morphSupportItemsKeepLegacyLimitsAndAircraft`：默认堆叠、配置现代化上限／满额拒绝、未婚 100 级训练／150 级不消耗、读档、修复补机、满血和无变身拒绝、口粮倍率。
- `feedingUsesBuffedResourcesAndRestoresCarrierAircraft`、`kaitaiRespectsDamageRejectionAndCreativeDismantleDropsNothing` 及转让／口粮相关断言：资源倍率、补机与饱食拒绝、伤害拒绝不强杀、创造无拆船掉落、非主人签名转让、创造消耗与 T_T 情绪、不同主人仍被吸引。
- 新增 `finalItemProtectionRingAndOpToolMatchLegacy`、`finalShipTankMachineTargetAndWrenchMemoryMatchLegacy`、`finalShipEggPermissionsAndCollisionMatchLegacy`：回收蛋伤害保护与重载、婚戒旧键、OPTool 无右键及排除目标、ShipTank 实际自身目标、扳手共享内存不写 NBT 与配对消息、水面权限和无额外碰撞拒绝。
- 删除四项自创路线能量测试及相关显示缓存断言，不再为已删除机制维持测试契约。
- 本批 T3 客户端／视觉门未执行；T1-M 独立模型检查也未执行。服务端测试不证明客户端点选、渲染或 Dust 粒子视觉等价，也不证明变身舰载机计数已接入航空攻击生命周期。

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

## 舰娘核心回归

### 舰种个体行为回归

- `hostileBossDropsOnlyItsLegacyEgg` 通过实际击杀核对规模 3 的敌舰只掉一枚基础舰娘蛋。`hostileScaleUsesSpawnAndSaveDataWithLegacyExperience` 验证通用部署默认值、旧档回读、已撤销字段清理和四级规模经验公式。
- `hostileEncounterSpawnInitializesRuntime` 验证显式遭遇规模、固定等级和无士气。`specialCombatHooksApplyHeavyCooldowns` 精确比较重炮冷却。`morphSpecialDispatchUsesLegacyClassIds` 和 `morphSpecialPreviewCooldownUsesBehaviorCatalog` 验证 class ID 58–63 及敌对镜像无专属技能与对应冷却。
- `progressionAdvancementsLoadAndFriendlyShipTriggerAwards` 核对海上 Boss 进度只接受规模 2 和 3 的敌舰。
- JDK 17 运行 `bash ./gradlew compileJava --no-daemon`，通过后运行 `bash ./gradlew runGameTestServer --no-daemon`。要求全量必过项全部通过，运行记录归 [批次证据](migration_batches/2026-11-22-parity-fix-ship-variants/STATUS.md)。

### AI移动与目标回归

- 完整服务端套件规模为 **319项GameTest**（本批次代码完成时的覆盖规模，不是文档整理重跑结果）；历史实测输出归 [AI批次STATUS](migration_batches/2026-11-22-parity-fix-ai-movement/STATUS.md)。
- 状态/跟随/传送：独立FollowMin/Max默认、范围、NBT与同步；Min/Max加宽度与PickItem距离，32tick缓存/寻路，严格传送冷却、精确Y+0.75、未加载区块拒绝及正常/缺失主人。
- 阵型/守卫/路线：安全扫描成功与无落脚点回退、无效槽位、实体阵型位置、min/max迟滞、GuardType存档、仅移动且type>0攻击、旧瞄准公式、维度取消、路点/起重机16/32/128tick调度和距离边界。
- 目标/杂项：Selector正常/空/失效目标与显式攻击入口分离；一船一玩家的确定候选和超过2候选时最近3随机（64种子）；碰撞宽²×16与冷却；游荡1/180、友10/5敌12/1及状态禁用；守卫/有目标时可拾取和旧音量配置。
- 真实3D移动新增：`shipNavigatorMovesUpThroughWater`、`shipNavigatorMovesDownThroughWater`、`shipNavigatorFliesWithoutGravity`、`shipNavigatorRejectsUnavailablePathsAndClearsStoppedPaths`。要求生成包含垂直边的水面/水下路径并实际接近目标，canFly静止不受重力且实际飞向高处；null/empty/blocked路径与坐下清除有显式断言。不能只凭“路径非空”声称移动验证通过。
- 行为/共享helper修改触发T1/T2：JDK17下 `bash ./gradlew compileJava --no-daemon`、`bash ./gradlew runGameTestServer --no-daemon`，全部required通过且失败0。仅文档移动/链接更新不触发GameTest重跑；执行T0与相对链接/旧路径残留检查。
- 319项服务端回归不覆盖飞机/坐骑3D接线、debug路径粒子和客户端视觉；边界见 [批次决定](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md)。

### 战斗、投射物与资源回归

- 战斗与投射物：`launchMissStillResolvesAreaDamage` 覆盖脱靶/正常/空范围撞击，`invisibilityAndGlowingDoNotChangeCombatRolls` 覆盖隐身发光和装备冷却，`potionDamageUsesMaxHealthAndLevelWithoutDefense` 覆盖药水/药水云及 HARM 等级 0/1/2；已有友伤、伤害倍率、照明和弹道断言同步恢复旧版语义。JDK 17 运行 `./gradlew compileJava`、`./gradlew runGameTestServer`（脚本无执行位时用 `bash ./gradlew`），证据见 [战斗批次](migration_batches/2026-11-22-parity-fix-combat-projectile/protocol.md)。

- `shipCoreRestoresLegacyNoFuelAndConfiguredSupplies`：零资源起点、资源不封顶、二级饥饿耗怨念五倍、断粮归零、配置补给及单件消耗。
- 既有资源存档、炮击与航空消耗、128 刻士气和菜单用例按 1.12.2 行为断言。运行 JDK 17 的 `./gradlew compileJava` 与 `./gradlew runGameTestServer`；结果归档到 [舰娘核心批次](migration_batches/2026-11-22-parity-fix-ship-core/STATUS.md)。

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

## PARITY-FIX-MENU-NETWORK-COMMAND focused coverage

- `shipGuiButtonsAndInventoryPagingMirrorLegacy`：验证旧版 `PID.ShipBtn` 按钮写入（攻击开关、PassiveAI/PVPFirst/OnSight/AntiAir/AntiSS、FollowMin/FollowMax 默认 3/12 与联动、FleeHP、ModelState 异或、InvPage 0..2 与非法值归零、DrumState 解锁）、分页货仓映射与 `0aad501` Shift 点击顺序。
- `b6SlashShipCommandsParity` 改为断言旧版 `checkOP`（创造即 OP、非 OP 生存玩家与控制台被拒）与 `/shipcleardrop` 只清除 `BasicEntityItem` 语义掉落。
- 证据见 [PARITY-FIX-MENU-NETWORK-COMMAND results](migration_batches/2026-11-22-parity-fix-menu-network-command/results/README.md)。

## FULL-SHIP-BEHAVIOR-REAUDIT focused coverage

- Round 1（4 项综合 `@GameTest`）：`abyssalHimeBaseSpecsAreFriendlyTamedPickupAndAuraEnabled`、`submarineHimeStealthAndShooterDistanceDodgeMatchLegacy`、`baseShipLifecycleRepairMoraleRegenAndFleeGoalMatchLegacy`、`shipSpecificHooksMountOrientationAndModelCatalogsMatchLegacy`。
- Round 2（5 项综合 `@GameTest`）：`round2HurtImmunitiesVoidRescueGoddessAndResistanceMatchLegacy`、`round2SlotFormationBuffsMinMovAndEnchantScalingMatchLegacy`、`round2FishingCraneAndPointerTargetingMatchLegacy`、`round2AircraftCooldownShootableTorpedoAndRensouhouMatchLegacy`、`round2DeathRecoveryEggXpCostAndHeldCombatRationMatchLegacy`。
- 最终全量验证（JDK `17.0.3.1`）：`.\gradlew.bat build verifyItemModelCoverage runGameTestServer --no-daemon` 通过，**`All 298 required tests passed :)`**（`298 passed / 0 failed / 0 skipped`），证据见 [FULL-SHIP-BEHAVIOR-REAUDIT results](migration_batches/2026-11-18-full-ship-behavior-reaudit/results/reaudit-closeout.json)。

<a id="parity-fix-teitoku-morph-skill310310"></a>
## PARITY-FIX-TEITOKU-MORPH-SKILL（310/310）

- 批次：[STATUS](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/STATUS.md)；[13项结论与旧版依据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md)；[精确运行记录](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/verification.json)。
- `GameplayParityGameTests.uidRegistryUsesLegacyStartAndRecovery` / `periodicPlayerUidAndPointerRefreshMatchLegacy`：UID初值/低值/损坏保护、玩家周期、SID与当前队伍刷新。
- `ringScanAndLiquidDigMatchLegacy` / `ringFireAndMountDamageMatchLegacy`：扫描/NBT/持戒转换、挖掘数值、火伤与普通坐骑伤害。雾视觉不由服务端测试覆盖。
- `ShipChunkLoaderGameTests`（2项）：罗盘/在线主人/票据范围、离线保票、撤装/移动/卸载释放及 Wrong location 下坐骑/removed恢复。真实关服重启 validation 不在覆盖内。
- `MorphParityGameTests`（3项）：HP缩放/属性清理、登录/维度解除、空资源不自动解除、伤害早返/倍率/DEF/闪避/buff/女神消耗与计时、获取/owner与宿主顺序。
- `MorphAndTeamParityGameTests`（4项）：Destroyer I获取与选择/激活；重复/负值/未知选择、旧零哨兵/正值/空字段迁移；当前队伍开关/空位/满员六槽游标/force/null/非本人拒绝/跨队伍保留；Pointer和网络对应调用、生命周期不全队清理、周期清理边界；新增64格ray选择TransportWa→实际crane RouteNode、空/遮挡/外人/越距伪包与mount-host focus。`LegacyAircraftBlockCollisionGameTests.allEightMountsActuallyTickPastIntermediateWaypoints`新增8种真实host/passenger/mount.tick有限墙路由/open/orphan边界；见[E3/D4结果](migration_batches/2026-11-22-riding-aircraft-formation/followup-result.md)。
- 最终命令：`JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 bash ./gradlew compileJava runGameTestServer`；JDK `17.0.20.1`。wrapper原无执行权限，使用bash等价调用，未更改权限。
- 通过门槛：编译成功且全部required测试通过。最后生产/测试编辑后的结果：`All 310 required tests passed :)`；`BUILD SUCCESSFUL in 3m 19s`；退出码0。
- 原始日志保存在外部：`/home/ubuntu/shincolle-zero-team-check.log`；SHA-256见 results/verification.json，不提交完整日志。本轮仅文档收口，不对未变的构建输入重复运行。
- 静态门：`git diff --check`、暂存文件清单与 `src/main/**` 零修改检查；禁止加入日志、临时文件和秘密。
- 先前 clean baseline 298、UID302、chunk304、Morph307与夹具调整证据见 [results](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/README.md)，不把全量310解释为客户端视觉、真实Curios联机、真实旧世界迁移或关服重启验证。

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

## 客户端还原收尾门禁

本次验收套件规模为 **301** 项；具体通过数、失败及退出码只记于
[客户端还原结果](migration_batches/2026-11-22-parity-fix-client/results/README.md)。
旧章节中的解析模型/简版界面测试属于历史覆盖，不能证明 D-016 所要求的完整视觉一致性。

| 入口/辅助断言 | 覆盖 |
| --- | --- |
| `phaseFourMenusMutateServerStateAndExposeData` | 航点正数 PlayerUID/严格 owner、非主人/潜行分支及菜单基本状态；不放宽生产权限 |
| `craneWorkingShipAndElapsedTimeMatchLegacy` | 唯一作业舰/等待舰、累计/完成清零、实体 ID 与 CraneTime 高低位菜单同步及格式边界 |
| `LegacyShipModelMappingGameTests` | 每个 spec 模型映射、攻击粒子计时、本地情绪、持物分类及配置正常/空/无效值、Position_HUD；不是顶点画面验收 |
| `FormationParityGameTests` | 六阵型/九分队热区、60/40 GUI 等待、重复行语义、属性/阵位/预览同步；以实际注册入口为准 |
| `ro500LightAttackLaunchesLegacyMissile` | 实际 launch/target 的总距离/水平距离条件期望，导弹算法不变 |
| `ShipInventoryParityGameTests.check`（并入既有菜单测试） | 三页容量门槛、十二页热区/空预留、独立滑块、旧按钮服务端语义、快照与编队悬停格式 |
| `DeskParityGameTests.check`（并入既有菜单测试） | 四页开关/便携模式、七章边界、同行再点、地图无点击、空/无效选择、class 0、冷却和图文资源 |
| `morphInventoryLegacySupportButtonsMutateSelectedProfile → MorphInventoryParityGameTests.check` | 三页、NoFuel/Sit/表情0..8与0/4/模型位/供应旧包、存档/同步、旧/空档案、无效按钮、敌对元数据；不保留自创激活/循环断言 |

最终必须执行：
```bash
git diff --check
bash ./gradlew compileJava --no-daemon
bash ./gradlew runGameTestServer --no-daemon
git diff origin/devin/parity-fix-menu-network-command --stat -- src/main
```

通过条件：两个 Gradle 命令退出 0，全部 301 必需测试通过、无失败名；最后一条无输出。
本次收尾仅文档，不重复启动客户端。任何后续 UI/模型变更按 T3 针对旧画面验证，不能用编译或模型映射代替。

专用静态/无头检查（相关输入变化时执行）：
- `python3 src/modern/scripts/port_desk.py /home/ubuntu/legacy0aad --check` 与 `port_desk_screen.py` 同参：数据、方法、翻译和原图字节。
- `python3 src/modern/scripts/port_ship_inventory.py /home/ubuntu/legacy0aad --check`：背包目录、控件、展示、画面方法。
- `python3 src/modern/scripts/port_morph_inventory.py /home/ubuntu/legacy0aad --check`：变身方法/控件和贴图。
- `python3 src/modern/scripts/check_particle_parity.py LEGACY_ROOT OUTPUT_DIRECTORY`：表情/文字状态方法的 API 桩及纹理；不是客户端渲染。

## PARITY-FIX-CRAFTING-WORLD-CONFIG（2026-11-22）

- 命令：`bash ./gradlew compileJava --no-daemon`、`bash ./gradlew runGameTestServer --no-daemon`（JDK 17）。
- 专题分支历史结果：`compileJava` 通过；GameTest `297` 项中 `296` 通过，`passiveAiStopsAutomaticRangeTargeting` 失败（基线 `8f1d2fd` 同样失败）。删除 `progressionAdvancementsLoadAndFriendlyShipTriggerAwards`；`chestLootInjectorUsesLegacyConfigLootDefaults` 改为断言跳过 `shincolle:Torpedo` 与整数 rolls；`worldgenResourcesKeepLegacyPolymetalPlacement` 改为断言 `shincolle:polymetal_gravel` feature 与 `Polymetal_Gravel*` 默认值。证据见 [verification](migration_batches/2026-11-22-parity-fix-crafting-world-config/results/verification.md)。

- integration 合并验证：`All 328 required tests passed :)`；新增 `machineConfigListsKeepLegacyKeysAndFallbacks`（键名/配置值/空与无效列表）、`hostileEggUsesLegacyConfiguredLifetime`（默认、0、-1），补充燃料恰满边界；敌舰掉落保留全局怨念事件，起重机与雷电夹具清理/隔离。当前完整证据见同一 verification 的 integration-merge 段。

## 九批 parity integration 最终门禁

- 分支：`devin/parity-fix-integration`；JDK 17；构建入口为 `bash ./gradlew`（wrapper 无执行位）。
- 命令：`bash ./gradlew compileJava --no-daemon`，然后 `bash ./gradlew runGameTestServer --no-daemon`。
- 通过条件：两条退出 0、全部必需测试通过，无冲突标记/未合并路径；`git diff --stat origin/codex/mc-1.20.1 -- src/main` 无输出。
- 最终记录：`All 340 required tests passed :)`；包括原 328 项及新增 Morph/编队/UID/戒指/伤害/票据覆盖。获取仅解锁、classId=0、空值 -1、重复/无效选择、当前队列增删、跨队伍同舰、修复桶舰载机计数、正常/无宿主路径均保留回归。
- 实际输出、日志校验和与验证边界见 [最终集成证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md)。没有运行客户端视觉、真实 Curios 或真实服务器重启清票据验收；服务端全绿不替代这些门禁。
