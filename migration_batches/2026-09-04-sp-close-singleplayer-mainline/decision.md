# SP-CLOSE Decision

状态：`M0-M9 PASS (SP mainline); V0 PENDING`

本批只收束单人主线的跨模块证据。`src/main/java` 与 `src/main/resources` 仍是只读语义真源；没有为敌对 Boss 追击或敌方战斗提示引入新的行为实现。

| Track | Required domain | Status | Evidence |
| --- | --- | --- | --- |
| M0 | registration/resources | PASS (SP mainline) | `registeredPhaseOneResourcesResolve`、所有 `ShipEntitySpecs` model source/texture 资源检查；详见 [sp-close.json](results/sp-close.json) |
| M1 | lifecycle/sync/save boundary | PASS (SP mainline) | `shipWorldCacheMarksUnloadedShipsOffline`、`gameplayStatePayloadSyncsOfflineShipCacheAndRuntimeSlices`、既有 SHIP-01 至 SHIP-09 批次结果 |
| M2 | runtime/supply/repair/rescue/cooldown | PASS (SP mainline) | `commandRuntimeAndTeamStatePersistThroughNbt`、`shipRuntimeSuppliesGatePersistAndRestoreLegacySupportValues`、`repairGoddessCargoRescueRestoresLegacyRuntimeAndTooltip` |
| M3 | packet/service/menu/feedback | PASS (SP mainline) | `shipCommandPacketRoundTripsTypedFields`、`shipCommandServiceReportsFailureReasons`、`singleShipCommandLoopMovesGuardsAttacksAndStops`、`formationModeCommandsGateGroupMovesAndOffsetFollowers` |
| M4 | AI/route/formation/target/supply/pickup priority | PASS (SP mainline) | `singlePlayerAiPriorityContractMatchesLegacyBoundaries`、`explicitMoveCommandWinsOverAutonomousCombat`、`shipRangeTargetUsesLegacyCategoryPriority`、`guardTargetUsesLegacyGuardingBounds`、`shipCanFollowMigratesAndSwitchesWithCommands`、`shipPickupGoalUsesLegacySelectionAndCooldown` |
| M5 | all mainline per-ship behavior domains | PASS (SP mainline) | legacy 45 个非 Mob 具体舰种类与 19 个 Mob 敌对变体类已盘点并与现代 specs/矩阵对齐；另见 SHIP-01 至 SHIP-09 批次证据和 [MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md) |
| M6 | combat/resource/loot result domains | PASS (SP mainline) | `perShipBehaviorCatalogRoutesAttackProfiles`、`perShipBehaviorCatalogRoutesSpecialCombatHooks`、`specialCombatHooksApplyHeavyCooldowns`、`catalogHeavyMissesRemainProjectileOnly`、各 SHIP 批次 combat/resource 断言 |
| M7 | projectile/reaction/feedback families | PASS (SP mainline) | `ship04HimeLegacySourcesAndProjectileFamilies`、`ship06SubmarineHimeAndBeamProjectileBoundariesMatchLegacy`、`projectileProfilesMatchShipRoles`、`combatReactActionbarScopeRequiresParticipant`、`hostileCombatReactDoesNotAllowPlayerActionbar` |
| M8 | model/renderer/screen/particle/sound | PASS (SP mainline) | `registeredShipLegacyModelSourcesParse`、SHIP-04 至 SHIP-09 model/bake 断言、Java 17 client startup/resource/model-log smoke 证据 |
| M9 | recipe/loot/advancement/encounter/DeskReference | PASS (SP mainline) | `growthEntryRecipesResolveAndCraft`、`lateGameRecipesResolveAndCraft`、`worldgenResourcesKeepLegacyPolymetalPlacement`、`chestLootInjectorUsesLegacyConfigLootDefaults`、`progressionAdvancementsLoadAndFriendlyShipTriggerAwards`、hostile encounter tests |
| V0 | real in-world client acceptance | PENDING | 当前自动化环境没有暴露 Minecraft 原生窗口，尚未执行进世界生成、交互、战斗、GUI 和存档恢复流程；既有 startup smoke 不替代 V0 |

## Hostile target and feedback audit

旧版 `BasicEntityShipHostile` 通过 `EntityAIShipRangeTarget` 执行每 8 tick 的范围扫描，范围使用整数化 `attackRange`，并以 `TargetHelper.SelectorForHostile` 排除 creative/invulnerable 玩家、敌对舰和不可见目标，默认 `ConfigHandler.mobAttackPlayer=true`。现代 `LegacyShipHostilePlayerTargetGoal`、`LegacyShipRangeTargetGoal`、`LegacyShipBehaviorCatalog` 与 `LegacyShipEntity.canEngage` 已保留这些边界：相同范围盒、8 tick 节奏、最近/前三随机选择、creative/无敌/观察者排除、敌对舰互斥和敌方不可见目标排除。

敌方飞机起飞/命中反应通过现有 combat reaction packet bus；`ClientboundCombatReactPacket.allowsPlayerActionbar` 要求玩家参与者且攻击者不是 hostile ship/summon。因此该问题本批结论为 `LOCKED / no change`，不是把追击距离或玩家提示再改一遍。

## Ship inventory completeness audit

仅按 legacy `entity/battleship`、`carrier`、`cruiser`、`destroyer`、`hime`、`submarine`、`transport` 目录的具体类名盘点：45 个非 `Mob` 舰种类与 19 个 `Mob` 敌对变体类均已在现代 `ShipEntitySpecs` 的 friendly/hostile metadata、渲染资源检查和舰种矩阵中有对应入口。`BasicEntity*`、mount、aircraft、projectile、summon 属于共享基类或联动实体，不另计舰种。命令输出摘要见 [sp-close.json](results/sp-close.json)。

## Concrete override inventory audit

本批随后按协议冻结的目录和方法签名，对 45 个非 Mob 具体类的特殊入口做了定向 body 对照，并同步核对 19 个 hostile Mob 变体的 class mapping。结果见 [ship-override-audit.json](results/ship-override-audit.json)：航空母舰机型/音效、姬级被动与 mount、特殊攻击、四驱逐合体、潜艇/运输 runtime、巡洋舰反击与 Re 的 post-hit motion 均已落在现有 `ShipEntitySpecs`、`LegacyShipAttackProfile`、`LegacyShipBehaviorCatalog` 或共享实体入口；没有发现需要新增现代代码的未覆盖舰种入口。

这是一项 `codeChange: NONE` 的无差异审计，不把类名存在或矩阵锁定替代行为证据。旧版 `Mode_Easy`、`Attack_Player_ShipMob` 等可选配置开关未在本批新增第二套配置层，作为完整 parity backlog 保留；单人默认行为与现有回归不受影响。

## Open gap

- 只剩 V0：需要可操作的 Minecraft 原生客户端窗口，完成 [MIGRATION-FLOW-CHECKLIST.md](../../MIGRATION-FLOW-CHECKLIST.md) 的进世界流程并留下人工记录。V0 完成前，根文档不能写成最终 `Done (SP mainline)`。

## Non-goals

多人 ally UI、完整旧版 packet-heavy GUI、完整 inter-mod API、全部历史 worldgen/ore-dict 矩阵和非主线边角兼容继续进入 parity backlog，不影响 M0-M9 的单人主线结论。

## Follow-up

V0 记录完成后重新检查 T0-T4；若 V0 通过，再统一把 Phase 5-10 和模块状态提升为 `Done (SP mainline)`，并保留上述 parity backlog。
