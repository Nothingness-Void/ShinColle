# GAMETEST-REPAIR-B4 Decision

- 第 6 项的普通实体分派结论保留，该条提及的两项专属变身技能已经撤销。见 [D-014](../../MIGRATION-DECISIONS.md#d-014舰种个体行为的现代偏离已撤销)。

- 结论：`DONE`（修复 9 项测试；同步关闭 `WORLDGEN-AUDIT` B4 测试缺口）
- 影响模块：M3、M4、M5、M6、M9 与测试套 `GameplayParityGameTests.java`
- 代码变更：仅修改 [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java)；现代生产逻辑经 1.12.2 真源核对全部已匹配，未引入任何新实现或非 1.12 规则。

## 逐项真源对照结论（9 / 9）

1. **`worldgenResourcesKeepLegacyPolymetalPlacement`（B4 Worldgen）**：1.12.2 `ConfigHandler.java#L267-269` 默认 `polyGravelBaseBlock = {true, true, false, false}`（`stone=true, gravel=true, sand=false, dirt=false`）；`ShinColleWorldGen.java#L92` 与 `WorldGenPolyGravel.java#L75` 为 `b0=1`、半径 `1..2`。现代 `polymetal_gravel.json` 已匹配 1.12 默认值；更新测试只校验 `minecraft:stone` 与 `minecraft:gravel` 并拒绝 `sand`/`dirt`/`clay`。
2. **`hostileSpecFlagsMatchLegacySemantics`**：1.12.2 `ModEntity.java#L161` 中 `EntityDestroyerI`（`eggMeta 2`）继承 `BasicEntityShipSmall`（可驯服友好舰，非 Mob）。现代 `ShipEntitySpecs.java#L24` 设置 `hostile=false` 匹配旧版；修正测试中 `!destroyerI.hostile()` 陈旧断言为 `destroyerI.hostile()`。
3. **`shipCommandServiceReportsFailureReasons`**：1.12.2 `FormationHelper.java#L1026-1027,1064-1065,1101-1102,1120` 仅检查**玩家到舰娘距离 `< 64`**，服务端并不因目标方块坐标遥远而拒收命令；`ShipCommandService.java#L26,L175,L406` 已按 1.12 对齐（`HEAD` 测试仍残留旧自定义 `320` 格上限断言）。改为验证 `>= 64` 格远舰返回 `NO_COMMANDABLE_SHIP`，近距舰可接受远距离移动坐标。
4. **`shipCommandServiceRejectsUnauthorizedFarAndDeadShips`**：同上，`>= 64` 格远距己方舰被拒绝而近距舰允许远坐标指令；更新测试用 `>= 64` 格舰验证距离门禁。
5. **`perShipBehaviorCatalogRoutesAttackProfiles`**：1.12.2 `EntityTransportWa.java#L40-43`（禁用 light/heavy/air）与 `EntityCarrierKaga.java#L42-43`（禁用 `AtkType_Light/Heavy`）在现代 `LegacyShipAttackProfile.java#L305-325` 已精确还原；更新测试断言匹配两舰的真实 1.12 开关。
6. **`perShipBehaviorCatalogRoutesSpecialCombatHooks`**：1.12.2 `EntityCAAtago.java`、`EntityCATakao.java`、`EntityBBKongou..Kirishima` 无重炮特殊攻击覆写，`LegacyShipBehaviorCatalog.entityCombatHookForClassId` 返回 `CombatHook.NONE`（`HEAVY_CRUISER_BARRAGE` 与 `KONGOU_COMBO` 仅属于 morph 技能表）；改为校验上述类为 `NONE` 并额外断言实际拥有 1.12 实体特攻钩子的 `15/16/22/33/46`。
7. **`hostileBossUsesLegacyPlayerTargetRules`**：MC 1.20.1 `GameTestHelper#makeMockPlayer()` 返回的匿名 `Player` 硬编码 `isCreative() == true`，导致仅清理 `abilities.invulnerable`/`instabuild` 仍被 `LegacyShipEntity#canEngage`（对应 1.12 `TargetHelper.SelectorForHostile` 对 `capabilities.disableDamage` 排除）拦截。保留 `makeMockPlayer()` 验证 Creative 排除，同时使用 `makeServerBackedPlayer` 覆盖 Survival 与 `invulnerable` 开关及射程牵引。
8. **`shipPickupGoalStoresNearbyDrops`**：1.12.2 `EntityCLTenryuu.setAIList()`（`EntityCLTenryuu.java#L99-108`）从未注册 `EntityAIShipPickItem`，现代 `LegacyShipBehaviorCatalog.supportsItemPickup` 对 `eggMeta 58` 返回 `false` 属 1.12 正解；且 1.12 `EntityAIShipPickItem.java#L147` 校验 `!entitem.cannotPickup()`。同时原测试在全局 `(1.0,2.0,1.0)` 生成实体，与同批测试遗留在 `(1.5,2.0,1.5)` 且含拾取延迟的掉落蛋冲突。现改用有拾取 AI 的 `eggMeta 53`（Akatsuki）、调用 `setNoPickUpDelay()` 并用 `helper.absolutePos(...)` 隔离测试单元且清理实体。
9. **`shipPickupGoalYieldsToOrdersCombatAndSupplyFlag`（含 `pickupGoalCanUseWith`）**：1.12.2 `EntityAIShipPickItem.java#L72` 与现代 `LegacyShipPickItemGoal.java#L91` 均由 `PickItem` AI 标志控制拾取而非 `AutoSupply`；改用 `eggMeta 53` 并测试 `setAiPickItem(false)`。

## No-change audits

- 现代生产代码（世界生成 JSON、生物规则、命令服务、攻击通道、行为表、目标筛选、拾取 Goal）经逐行比对 1.12.2 源码确认无偏差，本次保持零生产修改。

## Follow-up

- `WORLDGEN-AUDIT` 的 B4 测试同步待办已清零，状态升格为完全 `DONE`。
- 下一批按 `ROADMAP.md` 立即冻结并执行 `B5-ORE-TAGS`。
