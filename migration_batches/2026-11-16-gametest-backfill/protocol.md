# GAMETEST-BACKFILL Protocol

- 批次 ID：`GAMETEST-BACKFILL`
- 开始日期：2026-11-16
- 目标：根据 `ROADMAP.md` 第 22 项与 `MIGRATION-TEST-MATRIX.md` 要求，完整补回自 `SHIP-01` 至 `SHIP-ATTACK-PROFILE-AUDIT` 历史上因 2026-09-16 `git checkout HEAD` 丢失的 182 项核心回归用例（包含各舰种构造与属性、命令与路线回溯、战斗通道与攻击模式、Boss 行为与目标筛选、投射物与特殊攻击、骑乘与解散、伤害类型与防御光照修正等），恢复全量 GameTest 回归保障。

## 1. 语义与历史真源（只读对照）

- 历史全量测试真源快照：Git Dangling Blob `4d70aebad7532c05ebc34a588c84caedb1bd4102`
- 历史批次决策与结果：
  - `migration_batches/2026-09-03-ship-01-destroyer-ri/`
  - `migration_batches/2026-09-03-ship-02-command-route-target/`
  - `migration_batches/2026-09-03-ship-03-combat-behavior/`
  - `migration_batches/2026-09-03-ship-04-hime-visual-projectile/`
  - `migration_batches/2026-09-04-ship-05-special-runtime/`
  - `migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/`
  - `migration_batches/2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi/`
  - `migration_batches/2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao/`
  - `migration_batches/2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new/`
  - `migration_batches/2026-09-04-sp-close-singleplayer-mainline/`
  - `migration_batches/2026-09-05-combat-regression-suite/`
  - `migration_batches/2026-09-05-all-ship-override-audit/`
  - `migration_batches/2026-09-05-ship-attack-profile-audit/`
- 旧版 1.12.2 实体与行为真源：`src/main/java/com/lulan/shincolle/`（严禁修改，只读核对）

## 2. 现代修改与维护范围

- 测试实现目标文件：`src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`
- 批次文档：`migration_batches/2026-11-16-gametest-backfill/`（`protocol.md`、`decision.md`、`results/gametest-backfill.json`）
- 矩阵文档同步：`MIGRATION-TEST-MATRIX.md`、`CURRENT_STATE.md`、`ROADMAP.md`
- 约束：
  - 严禁修改 `src/main/` 任何文件。
  - 保留 `GAMETEST-REPAIR-B4` 修复的 9 项测试与 `B5-ORE-TAGS` 新增的 3 项测试，不得回退这 12 项现代权威断言。
  - 生产代码（`src/modern/java/com/lulan/shincolle/entity/` 等）经核对已具备完整实现，本批次为纯测试补齐与文档收口，原则上保持生产代码零改动（如发现微小编译适配需在 `decision.md` 详述）。

## 3. 验收断言

1. **测试用例全量对齐**：
   - 补齐从 `SHIP-01` 至 `SHIP-ATTACK-PROFILE-AUDIT` 的 182 项测试方法与 8 项配套私有 helper 方法及 29 项 import。
   - 测试用例总数达到 276 项（当前 94 + 补回 182 = 276）。
2. **核心覆盖点**：
   - SHIP-02：`ship02CommandBoundariesCoverFiveLegacyVariants`、`waypointRouteBacktracksUsingLegacyLastWaypoint`、`legacyWaypointNbtMigratesOwnerAndRouteLinks`。
   - SHIP-03：`ship03TransportSubmarinesAndCarrierHimeKeepLegacyCombatBoundaries`、`transportWaKeepsLegacyCargoAndFleetSupplyRules`、`carrierHimeKeepsLegacyMeleeMissileLaunch`、`fiveSubmarineProjectileVariantsKeepLegacyMoveAndSpeedRules`、`legacyAttackCostsFollowShipClassAndMoraleRules`。
   - SHIP-04：`ship04HimeLegacySourcesAndProjectileFamilies`、`ship04HimeModelStateUsesLegacyShowEquipBits`。
   - SHIP-06：`ship06SubmarineYamatoKagaLegacyModelsKeepExactSourceAndStateRules`、`ship06SelectedModelScaleBranchesMatchLegacySource`、`ship06SubmarineHimeAndBeamProjectileBoundariesMatchLegacy`、`submarineHimeLightAttackLaunchesDoubleMissile`、`ssnhLightAttackLaunchesDoubleMissile`、`carrierAircraftCapacityAndRuntimeFollowLegacyCvRules`。
   - SHIP-07：`ship07SelectedModelsBakeAndKeepLegacyShowEquipBranches`、`ship07DestroyerRiderFuelBoundaryMatchesLegacyOverrides`、`caHimeRefreshesNightStatsOnLegacyCadence`、`sixDestroyerRiderStateFollowsLegacyMasks`、`sixDestroyerRidersUseLegacyPassengerPositions`、`friendlyIkazuchiAndInazumaKeepLegacyRaidenPairing`。
   - SHIP-08：`ship08SelectedModelsBakeAndKeepLegacyShowEquipBranches`、`atagoAndTakaoReactionAndMountBehaviorMatchLegacyClasses`、`heavyCruiserTypeFiveMissilesSpawnLegacyStaticFields`、`tenryuuHeavyAttackRunsLegacySkillPhases`、`tatsutaHeavyAttackRunsLegacySkillPhases`。
   - SHIP-09：`ship09KongouFamilyModelsAndRuntimeBoundariesMatchLegacy`、`kongouFamilyFiveShipCombatHooksKeepLegacyCannonFeedback`、`catalogHeavyMissesRemainProjectileOnly`、`submarineHimeKeepsLegacyGeometryAndRingBehavior`。
   - SP-CLOSE：`singlePlayerAiPriorityContractMatchesLegacyBoundaries`、`hostileBossDropsInvalidTargetBeforeNavigation`、`fiveHimeBossesKeepLegacyInvisibleTargetBoundary`、`hostileCombatReactDoesNotAllowPlayerActionbar`。
   - COMBAT-REGRESSION：`nagatoMoraleEventMatchesLegacyNearbyTargets`、`yamatoHeavyAttackUsesLegacyTwoPhaseBeam`。
   - ALL-SHIP-OVERRIDE：`allShipDamageTypesAndLightModifiersMirrorLegacy`。
   - SHIP-ATTACK-PROFILE：`registeredAttackChannelsMirrorLegacySources`。
3. **自动化验证标准**：
   - `git diff --check` 通过（无新增空白错误）。
   - `./gradlew compileJava` 编译通过（0 错误）。
   - `./gradlew runGameTestServer` 运行全量 276 项 GameTest，所有测试全部通过（0 失败，0 跳过）。

## 4. 非目标

- 不重新设计或重构已验证的生产逻辑（`LegacyShipBehaviorCatalog`、`LegacyShipEntity` 等）。
- 不将测试类拆分为非兼容类名而破坏 `gameplayparitygametests.empty.snbt` 绑定。
- 不提前展开 B1（GUI/Container）、B2（网络包）、B3（多人队伍）相关开发。
