# GAMETEST-BACKFILL Decision

- 结论：`DONE`（从 Git Dangling Blob `4d70aebad7532c05ebc34a588c84caedb1bd4102` 完整找回并补齐 182 项缺失 `@GameTest` 方法、8 项配套辅助方法与 29 项 import；测试套总规模从 94 提升至 276 项并全绿通过）
- 影响模块：`GameplayParityGameTests.java`、全舰种回归测试保障体系（覆盖 `SHIP-01` 至 `SHIP-ATTACK-PROFILE-AUDIT` 11 个批次）
- 生产代码变更：0（经校验现代生产代码 `LegacyShipBehaviorCatalog.java`、`LegacyShipEntity.java`、`LegacyShipAttackProfile.java`、`LegacyShipCombatHelper.java` 等在 2026-09-16 回退测试时未被回退，保持完整可用）

## 1. 背景与根因

1. 2026-09-16 在编辑 Worldgen 测试故障时，`src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java` 曾被 `git checkout HEAD` 还原至基线提交 `3c9fe42d`，导致自 `SHIP-01` 至 `SHIP-ATTACK-PROFILE-AUDIT` 积累的 182 项回归测试用例在工作区丢失。
2. 随后执行的 `GAMETEST-REPAIR-B4`（修复 9 项测试）与 `B5-ORE-TAGS`（新增 3 项矿物标签测试）使活动用例数为 94 项（91 修复 + 3 新增）。
3. 通过 `git fsck --lost-found` 取证，在未提交 dangling blob `4d70aebad7532c05ebc34a588c84caedb1bd4102` 中完整锁定了 2026-09-16 回退前的 271 项测试源文件（14,363 行）。

## 2. 补回与整合策略

1. **零破坏与高保真保留**：
   - 严格保留活动工作区中已有 94 项 `@GameTest` 测试方法（包含 `GAMETEST-REPAIR-B4` 修复的 9 项权威用例与 `B5-ORE-TAGS` 新增的 3 项矿物用例），原样保持无改动。
2. **缺失组件精准补充**：
   - 补入 29 项缺失 imports（涵盖投射物实体、Goal、粒子包、寻路与模型目录）。
   - 补入 182 项历史 `@GameTest` 测试方法。
   - 补入 8 项配套私有 helper 方法：`expectedLegacyArcMissileMove`、`attackEffectMatches`、`attackEffectMissing`、`setSyntheticGuardPath`、`engineSmokePointMatches`、`cannonFeedbackMatches`、`engineVisualPointMatches`、`clientEffectPointMatches`。
3. **架构单文件保持**：
   - 保持所有测试统一在 `GameplayParityGameTests.java`，确保与 `src/modern/resources/gameteststructures/gameplayparitygametests.empty.snbt` 自动结构绑定 100% 契合，且直接复用私有 helper 方法。

## 3. 覆盖范围与 11 批次核验

| 批次 / 领域 | 覆盖舰种与特性 | 关键用例 |
|---|---|---|
| **SHIP-02** | CANe, CarrierWo, BattleshipRu/Ta/Re，路径回溯与命令边界 | `ship02CommandBoundariesCoverFiveLegacyVariants`, `waypointRouteBacktracksUsingLegacyLastWaypoint`, `legacyWaypointNbtMigratesOwnerAndRouteLinks` |
| **SHIP-03** | Wa, Ka, Yo, So, Carrier Hime，资源消耗与潜艇弹道 | `ship03TransportSubmarinesAndCarrierHimeKeepLegacyCombatBoundaries`, `transportWaKeepsLegacyCargoAndFleetSupplyRules`, `carrierHimeKeepsLegacyMeleeMissileLaunch`, `fiveSubmarineProjectileVariantsKeepLegacyMoveAndSpeedRules`, `legacyAttackCostsFollowShipClassAndMoraleRules` |
| **SHIP-04** | Airfield, Battleship Hime, Destroyer Hime, Harbour Hime, Isolated Hime，模型源与装备位 | `ship04HimeLegacySourcesAndProjectileFamilies`, `ship04HimeModelStateUsesLegacyShowEquipBits` |
| **SHIP-06** | U-511, Ro-500, Submarine Hime, SSNH, Yamato, Kaga，双发导弹与航母舰载机 | `ship06SubmarineYamatoKagaLegacyModelsKeepExactSourceAndStateRules`, `ship06SelectedModelScaleBranchesMatchLegacySource`, `ship06SubmarineHimeAndBeamProjectileBoundariesMatchLegacy`, `submarineHimeLightAttackLaunchesDoubleMissile`, `ssnhLightAttackLaunchesDoubleMissile`, `carrierAircraftCapacityAndRuntimeFollowLegacyCvRules` |
| **SHIP-07** | Akagi, CA Hime, Akatsuki, Hibiki, Ikazuchi，六驱骑乘与夜战刷新 | `ship07SelectedModelsBakeAndKeepLegacyShowEquipBranches`, `ship07DestroyerRiderFuelBoundaryMatchesLegacyOverrides`, `caHimeRefreshesNightStatsOnLegacyCadence`, `sixDestroyerRiderStateFollowsLegacyMasks`, `sixDestroyerRidersUseLegacyPassengerPositions`, `friendlyIkazuchiAndInazumaKeepLegacyRaidenPairing` |
| **SHIP-08** | Inazuma, Tenryuu, Tatsuta, Atago, Takao，大招技能阶段与重巡静电 | `ship08SelectedModelsBakeAndKeepLegacyShowEquipBranches`, `atagoAndTakaoReactionAndMountBehaviorMatchLegacyClasses`, `heavyCruiserTypeFiveMissilesSpawnLegacyStaticFields`, `tenryuuHeavyAttackRunsLegacySkillPhases`, `tatsutaHeavyAttackRunsLegacySkillPhases` |
| **SHIP-09** | Kongou, Hiei, Haruna, Kirishima, SSNH，主炮反馈与潜艇公主光环 | `ship09KongouFamilyModelsAndRuntimeBoundariesMatchLegacy`, `kongouFamilyFiveShipCombatHooksKeepLegacyCannonFeedback`, `catalogHeavyMissesRemainProjectileOnly`, `submarineHimeKeepsLegacyGeometryAndRingBehavior` |
| **SP-CLOSE** | 单人 AI 优先级契约、Boss 隐身过滤与战斗反应 | `singlePlayerAiPriorityContractMatchesLegacyBoundaries`, `hostileBossDropsInvalidTargetBeforeNavigation`, `fiveHimeBossesKeepLegacyInvisibleTargetBoundary`, `hostileCombatReactDoesNotAllowPlayerActionbar` |
| **COMBAT-REGRESSION** | 长门士气事件、长门 4 阶段重击、大和 2 阶段光束 | `nagatoMoraleEventMatchesLegacyNearbyTargets`, `nagatoHeavyStrikeUsesLegacyFourPhaseCycle`, `yamatoHeavyAttackUsesLegacyTwoPhaseBeam` |
| **ALL-SHIP-OVERRIDE** | 全 45 舰种伤害类型与防御光照修正 | `allShipDamageTypesAndLightModifiersMirrorLegacy` |
| **SHIP-ATTACK-PROFILE** | 全 64 舰娘变种攻击通道门禁与战斗钩子矩阵 | `registeredAttackChannelsMirrorLegacySources` |

## 4. 验证结果

1. **`git diff --check`**：通过（exit 0，无任何空白或格式缺陷）。
2. **`./gradlew compileJava`**：通过（exit 0，10s 构建通过，零语法与类型错误）。
3. **`./gradlew runGameTestServer`**：
   - 运行用例总数：**276**
   - 结果：**All 276 required tests passed :)**（276 passed / 0 failed / 0 skipped，耗时 36s）。
   - 语义真源边界：`src/main/` 严格只读，零改动。
