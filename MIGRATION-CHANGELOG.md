# 迁移变更索引

- GUI/audio 回归复核：雷达旧行为、大船坞布局恢复、冻结整点报时溯源；[本轮记录](migration_batches/2026-11-22-gui-audio-regressions/README.md)。

## Rendering backport and approved land legs

Forge 渲染回移 `1547691`、水面判定修正 `bccd7dc`：原外层姿态、BEWLR、旋转/透光、图标、大小写清理、审计与测试全部适配，并带回用户批准的 Destroyer Hime 陆上开腿例外；[Forge 证据](migration_batches/rendering-backport-1.20.1/STATUS.md)。NeoForge merge 保留其 API 适配，且不重复注册尺寸测试；[本轮结果](migration_batches/neoforge-1.21.1/status.md#rendering-backport-and-approved-land-legs)。

## Minecraft 1.21.1 / NeoForge 21.1.267

Destroyer Hime 后续反馈：新增全模型源码/CPU 顶点审计与 Leg 开关回归，确认 egg 29 默认无腿悬浮是旧版姿态而非遗漏的 renderer 分支；未改变旧版视觉。352 项验证及未决落地取舍见 [审计决定](migration_batches/neoforge-1.21.1/decision.md)。

客户端反馈跟进：恢复舰娘旧版 `setRotationAngles` 外层姿态调用、办公桌/小船坞 TESR 物品路径与世界朝向/透光、起重机/航点的 3D 物品变换；删除 16 个现代资源大小写冲突路径。根因、351 项服务端测试与针对性截图见 [客户端修复记录](migration_batches/neoforge-1.21.1/status.md#client-rendering-follow-up-ship-placement)。

固定 1.20.1 parity 基线上的版本升级，分支 `devin/mc-1.21.1-neoforge`。构建、数据兼容、获批的护甲前 Mixin 钩子、GameTest 夹具 API 适配、最终验收与未验范围均见 [批次结果](migration_batches/neoforge-1.21.1/status.md)；当前快照见 [CURRENT_STATE.md](CURRENT_STATE.md)，后续仅见 [ROADMAP.md](ROADMAP.md)。下方九批记录保留为 1.20.1 历史，不代表新增功能已在本轮补完。

## 九批 parity 集成收尾

`devin/parity-fix-integration` 的历史最终 merge SHA 与结果见 [集成证据](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/integration-merge.md) 与 [测试矩阵](MIGRATION-TEST-MATRIX.md#九批-parity-integration-最终门禁)，重复实现取舍及 D-015～D-018 重编号见 [集成决定](MIGRATION-DECISIONS.md#九批集成的重复实现归并)。未验证项/遗留只在当前状态维护，后续只看 [ROADMAP.md](ROADMAP.md)，不以本索引复制旧专题通过数作为最终结果。

九个批次入口：
[ship-core](migration_batches/2026-11-22-parity-fix-ship-core/) ·
[combat-projectile](migration_batches/2026-11-22-parity-fix-combat-projectile/) ·
[ai-movement](migration_batches/2026-11-22-parity-fix-ai-movement/) ·
[ship-variants](migration_batches/2026-11-22-parity-fix-ship-variants/) ·
[menu-network-command](migration_batches/2026-11-22-parity-fix-menu-network-command/) ·
[client](migration_batches/2026-11-22-parity-fix-client/) ·
[items-blocks](migration_batches/2026-11-22-parity-fix-items-blocks/) ·
[crafting-world-config](migration_batches/2026-11-22-parity-fix-crafting-world-config/) ·
[teitoku-morph-skill](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/)。

下方保留各专题历史记录；不替代上述集成验证。

## 2026-11-22 客户端还原与文档收尾

详见 [批次导航](migration_batches/2026-11-22-parity-fix-client/)、[批次决定](migration_batches/2026-11-22-parity-fix-client/decision.md) 与 [验收证据](migration_batches/2026-11-22-parity-fix-client/results/README.md)。本索引不复制具体结论。

## 2026-11-22

- 舰种个体行为七项一致性修复：[批次记录](migration_batches/2026-11-22-parity-fix-ship-variants/)。

- AI移动与目标第1—5步修复、第6步文档归并及历史偏离撤销：[统一批次](migration_batches/2026-11-22-parity-fix-ai-movement/README.md)。

- 战斗与投射物七项一致性复核及修复：[批次记录](migration_batches/2026-11-22-parity-fix-combat-projectile/protocol.md)。

- 舰娘核心行为一致性修复：[批次记录](migration_batches/2026-11-22-parity-fix-ship-core/)。

## 2026-11-22

- [方块与物品旧版行为纠偏批次](migration_batches/2026-11-22-parity-fix-items-blocks/)。

## 2026-11-22

### PARITY-FIX-CRAFTING-WORLD-CONFIG：大建装备/投料、宝箱 loot、敌舰生成、多金属砂砾与配置按 1.12.2 还原

- 删除大建 `equipRate` 弹药兜底、恢复大建 `totalMat = 0` 与 `enchLv` 附魔；大建投料补齐装备/船蛋回收与 easyMode；恢复 `loottable.cfg` 与整数 rolls、移除鱼雷条目；接回 `Check_Ring`/`Limit_MobSpawnNumber`、护卫 50% 精英；移植 `WorldGenPolyGravel`；接回 `DropRate_Grudge`、`Despawn_Egg`、`Tile_*`、`Polymetal_Gravel*`；删除自创 advancements。GameTest `296/297`（唯一失败项在基线同样失败）。证据见 [PARITY-FIX-CRAFTING-WORLD-CONFIG](migration_batches/2026-11-22-parity-fix-crafting-world-config/)，决定见 D-017。

## 2026-11-22

- [PARITY-FIX-TEITOKU-MORPH-SKILL：批次入口](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/README.md) · [STATUS](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/STATUS.md) · [decision](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md) · [results](migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/README.md) · [D-018](MIGRATION-DECISIONS.md#d-018)。

## 2026-11-18

### FULL-SHIP-BEHAVIOR-REAUDIT：全 64 舰船变体与全子系统两轮并发严查修复收口（`298/298` GameTest）

- 针对 `v0.1.0-alpha` 反馈开展两轮多 Worker 并发逐行复审（7 路全舰种审计 + 4 路修复；6 路跨子系统深查 + 5 路修复 + 独立服务端安全复审），严格对照 1.12.2 源码修复全部已写代码中的隐藏偏差：
  - **深海栖姬友方身份与潜艇隐身/发光/防剔除**：[ShipEntitySpecs.java](src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java) 将 11 个深海栖姬基础规格（`22..74`）修正为 `hostile = false`（仅 `> 2000` 为敌对 `*Mob`）；[LegacyShipBehaviorCatalog.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java) 修复 `SubmHime`/`SSNH` 双隐身叠加；[LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) 与 [ClientShipVisibilityHelper.java](src/modern/java/com/lulan/shincolle/client/ClientShipVisibilityHelper.java) 补齐同主人/同盟隐身发光 `isCurrentlyGlowing()`、`noCulling = true` 视锥防剔除与真实射手距离潜艇闪避。
  - **基类生命周期、AI 与完整受伤管线**：补齐 32t 自动快修桶（含空母舰载机 `+1/+1`）与饱和药水 `+100*lv` 士气、128t `updateMorale()`（`1500/4000/8000/12000` 阈值）、256t 自动回血、[LegacyShipFleeGoal.java](src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFleeGoal.java) / [LegacyShipOpenDoorGoal.java](src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOpenDoorGoal.java) / `canTeleport`、自然敌舰 `128/32` 格配置化消散；完整对齐 `hurt()` 地形免疫、友方掉虚空传送 `Y=4` 救援、纯魔法 `< 1%` 忽略、女神 `>= health - 1` 触发及完整 120t 无敌、抗性/抗火药水 `20%*lv` 减伤、`ShinColleConfig.dmgSvS()` 及 [TeitokuEvents.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuEvents.java) 32/64 格群体复仇。
  - **阵型槽位/航速、装备附魔与逐舰特殊行为**：[LegacyShipStatTables.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java) / [LegacyShipStats.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStats.java) 补齐 `0..5` 槽位独立阵型加成与编队 `MinMOV` 航速同步；[ShipEquipmentProfile.java](src/modern/java/com/lulan/shincolle/entity/ship/ShipEquipmentProfile.java) 接入装备附魔缩放；修正北方栖姬 `0.75F` 重炮与潜行下坐骑、战舰 Re / 补给舰 Wa 骑乘高度、潜艇 Yo 轻攻、天龙瞬移候选格判定、龙田敌我击退与光束轨迹、晓 Mob 探照灯光块及防空/反潜目标选择。
  - **坐骑、召唤物、投射物、指挥棒、起重机、道具与模型渲染**：修正坐骑双人座位 X/Z 轴向反置与水面浮力，补齐坐骑/舰载机/连装炮/浮游要塞渲染器 `scale(-1,-1,1)` + `translate(0,-1.501F,0)`、`Glow*` 全亮层、15 个 `scaleLevel` 缩放表与 14 个模型 `showEquip` 部件位；补齐舰载机 `3x`/`2.5x` 攻速与可击落 HP、连装炮暴击与闪避减伤、导弹零额外阻力与 `moveType=2` 鱼雷入水加速及黑洞弹 miss 生成引力场；修正指挥棒 `friendlyFire=false` 与 64 格水面射线、钓鱼任意方块与幸运加成、起重机电源/`WaitMode 1..4`/红石信号、沉船回收蛋 `level - 1` 与生存 `level / 3` 经验消耗、手持口粮吸引、变身饮用快修桶及 `SOUNDRATE` 语音概率。
- 新增 9 项综合 `@GameTest`，`git diff --check`、`src/main/` 零改动及 JDK `17.0.3.1` `build verifyItemModelCoverage runGameTestServer --no-daemon` 通过 **`All 298 required tests passed :)`**（证据见 [FULL-SHIP-BEHAVIOR-REAUDIT](migration_batches/2026-11-18-full-ship-behavior-reaudit/)）。

### B6-INTERMOD-DETAILS（最终收口）：9 条 `/ship*` 指令、动态光方块、`ShinColleConfig` + `SystemConfig`/`polyAsMn`、`ShipKills`/`Raiden` 与外置 mod 收口

- 完成全队列最后一批 `B6-INTERMOD-DETAILS` 并正式将 [ROADMAP.md](ROADMAP.md) 当前指针归零为 **`NONE`**：
  - **全部 9 条 `/ship*` 指令**：新增 [ShinColleCommands.java](src/modern/java/com/lulan/shincolle/command/ShinColleCommands.java) 并在 [ShinColle.java](src/modern/java/com/lulan/shincolle/ShinColle.java) 注册，覆盖 `/shipchangeowner`（`/shipch`）、`/shipemotes`（`/em`/`/emo`/`/emote`/`/emotes` 与 35 个别名）、`/ship list|get|del`、`/shipkill`、`/shipattrs`、`/shipcleardrop`、`/shipinfo`、`/shipstopai`（`/shipstop`）及 `/shipupdateowneruid`；补齐 [ShipCacheSavedData.java](src/modern/java/com/lulan/shincolle/teitoku/ShipCacheSavedData.java) `removeShip`、[LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) 全局 `stopAi` + `0..100` 近代化存读区间 + `setModernizationSteps` 与 [LegacyMountEntity.java（已撤销）](MIGRATION-DECISIONS.md#d-018) / [LegacyShipMountEntity.java](src/modern/java/com/lulan/shincolle/entity/mount/LegacyShipMountEntity.java) `stopAi` 暂停。
  - **动态光方块与照明联动**：新增 [TemporaryLightBlock.java](src/modern/java/com/lulan/shincolle/block/TemporaryLightBlock.java)、[LightBlockEntity.java](src/modern/java/com/lulan/shincolle/blockentity/LightBlockEntity.java) 与 [LightBlockHelper.java](src/modern/java/com/lulan/shincolle/world/LightBlockHelper.java)，更新 [ModBlocks.java](src/modern/java/com/lulan/shincolle/registry/ModBlocks.java) 与 [ModBlockEntities.java](src/modern/java/com/lulan/shincolle/registry/ModBlockEntities.java) 实现 15 亮度及 120-tick 自动还原空气/静水/流水；接入 [LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) Searchlight/Flare 光块放置及 [GameplayClientEffects.java](src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java) 粒子门控。
  - **配置体系、`SystemConfig` 同步与 `polyAsMn`**：[TeitokuHelper.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java) 同步 `SystemConfig`（`RingAbility`、`EasyMode`、`CloseGuiDist`、`RadarUpdate`、`PolyAsMn`）与客户端有效读取，并接入 `teamCooldown`；新建 [PolyAsMnCondition.java](src/modern/java/com/lulan/shincolle/crafting/PolyAsMnCondition.java)（`shincolle:poly_as_mn`）与 [LegacyItemMatchHelper.java](src/modern/java/com/lulan/shincolle/crafting/LegacyItemMatchHelper.java) `matchesTag` 动态匹配（保持静态包标签默认值）；[GameplayCommandHandler.java](src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java)、[TargetWrenchItem.java](src/modern/java/com/lulan/shincolle/item/TargetWrenchItem.java)、[DeskTerminalMenu.java](src/modern/java/com/lulan/shincolle/menu/DeskTerminalMenu.java)、[DeskReferenceScreen.java](src/modern/java/com/lulan/shincolle/client/screen/DeskReferenceScreen.java)、[HostileEncounterSpawner.java](src/modern/java/com/lulan/shincolle/world/HostileEncounterSpawner.java)、[LegacyShipCombatHelper.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java)、[MorphHelper.java](src/modern/java/com/lulan/shincolle/morph/MorphHelper.java)、[MarriageRingItem.java](src/modern/java/com/lulan/shincolle/item/MarriageRingItem.java)、[ShinColleSoundHelper.java](src/modern/java/com/lulan/shincolle/sound/ShinColleSoundHelper.java) 与 [LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) 完成全量配置接线。
  - **`ShipKills`、`Raiden` 与外置 mod 收口**：[LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java)、[LegacyShipRensouhouEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java)、[LegacyShipAircraftEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java)、[ShipInventoryMenu.java](src/modern/java/com/lulan/shincolle/menu/ShipInventoryMenu.java) 与 [ShipInventoryScreen.java](src/modern/java/com/lulan/shincolle/client/screen/ShipInventoryScreen.java) 实现 `ShipKills` 统计展示及 `Raiden` 合体角色识别，补全三语翻译键；[TeitokuEvents.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuEvents.java) 新增 Curios 反射软钩，并将 Metamorph、IC2 替代方案与 40 个 `ID.java` `HISTORICAL_SOURCE_GAP` 名称在 [B6 closeout](migration_batches/2026-11-17-b6-intermod-details/results/b6-closeout.json) 建档收口。
- 在 [GameplayParityGameTests.java](src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 新增 `b6SlashShipCommandsParity`、`b6DynamicLightBlocksSearchlightAndFlareDecay` 与 `b6SystemConfigPolyAsMnShipKillsAndRaidenLabel`；`git diff --check`、`src/main/` 零改动及 JDK `17.0.3.1` `build verifyItemModelCoverage runGameTestServer --no-daemon` 通过 **`All 289 required tests passed :)`**（证据见 [B6-INTERMOD-DETAILS](migration_batches/2026-11-17-b6-intermod-details/)）。

### B3-MULTIPLAYER-TEAM（Part 2 收口）：PVP/友伤门禁、召唤物/投射物防误伤、`OwnerPaper` & `KaitaiHammer` 交互及多人 Desk Radar 同步

- 完成 `B3-MULTIPLAYER-TEAM` 第二部分全部收口并将 [ROADMAP.md](ROADMAP.md) 唯一 `NEXT` 晋级至 `B6-INTERMOD-DETAILS`：
  - **Forge 配置骨架与战斗友伤/PVP**：新建 [ShinColleConfig.java](src/modern/java/com/lulan/shincolle/config/ShinColleConfig.java) 并在 [ShinColle.java](src/modern/java/com/lulan/shincolle/ShinColle.java) 注册 `COMMON_SPEC` + `CLIENT_SPEC`；在 [LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) 接入非创造非主人玩家 `friendlyFire` 门禁、`isLegacyFriendlyFireAllowed` + 友方舰 `dmgSvS * 0.01F` 百分比缩放、同盟免防御反伤、新增 `canCommandOrRevengeEngage` 及 `canEngage` 的 `mobAttackPlayer`/`shipAttackPlayer` `0..3` 分支。
  - **召唤物/投射物/目标目标选择器同盟门禁**：[LegacyShipBehaviorCatalog.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java) 新增 `isLegacyFriendlyFireAllowed`；[LegacyShipRensouhouEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java) 与 [LegacyShipAircraftEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java) 补全 `hurt` 门禁；[LegacyShipProjectileEntity.java](src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java) 过滤同盟接触；[LegacyShipOwnerHurtByTargetGoal.java](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md)、[LegacyShipOwnerHurtTargetGoal.java](migration_batches/2026-11-22-parity-fix-ai-movement/decision.md) 与 [ShipCommandService.java](src/modern/java/com/lulan/shincolle/network/ShipCommandService.java) 切换为 `canCommandOrRevengeEngage`。
  - **`OwnerPaper` 与 `KaitaiHammer` 双模式**：[OwnerPaperItem.java](src/modern/java/com/lulan/shincolle/item/OwnerPaperItem.java) 为 `resolveTransferTarget` 增加全服关卡玩家回退查询；[LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) 对齐 `OwnerPaper` `emotion(2)` + `MARRY` 音效，区分 [KaitaiHammerItem.java](src/modern/java/com/lulan/shincolle/item/KaitaiHammerItem.java) 左键一击回收（`performKaitaiKill`）与潜行右键 `createLegacyKaitaiDrops` 分舰种 4 格掉落 + `6`/`8` 情绪。
  - **多人雷达服务端同步**：[GameplayCommandType.java](src/modern/java/com/lulan/shincolle/network/GameplayCommandType.java)、[GameplayCommandHandler.java](src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java)、[TeitokuHelper.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java) 与 [DeskReferenceScreen.java](src/modern/java/com/lulan/shincolle/client/screen/DeskReferenceScreen.java) 新增 `REQUEST_DESK_RADAR`、局部缓存保护及雷达/世界规则合并显示。
- 在 [GameplayParityGameTests.java](src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 新增 `b3Part2FriendlyFirePvpModesAndCommandRevengeTargeting` 与 `b3Part2OwnerPaperKaitaiHammerAndDeskRadarSync`；`git diff --check`、`src/main/` 零改动与 JDK `17.0.3.1` `runGameTestServer --no-daemon` 通过 **`All 286 required tests passed :)`**（证据见 [B3 Part 2 results](migration_batches/2026-11-17-b3-multiplayer-team/results/b3-part2.json)）。

## 2026-11-17

### B3-MULTIPLAYER-TEAM（Part 1）：核心队伍生命周期、跨维度广播与方块同盟/OP 权限对齐

- 在不提前晋级 `ROADMAP.md` 的前提下，按最小独立切片完成 `B3-MULTIPLAYER-TEAM` 第一部分核心多人队伍权限、状态生命周期与同步修复（Part 2 PVP/友伤及道具实体交互权限留待后续）：
  - **初始冷却与 UID 反查**：[TeitokuData.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuData.java) 新增 `INITIAL_TEAM_COOLDOWN = 20`，恢复玩家初次初始化与缺失 `TeamCD` 标签时的 20-tick 初始冷却（建队/解散后仍重置为 `6000`）；[TeitokuSavedData.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuSavedData.java) 新增 `getUid(UUID)` 只读反查。
  - **建队/改名规则与同队长旧队清理**：[TeitokuHelper.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java) 在 `createOwnTeam` / `renameOwnTeam` 中对齐旧版 `tname.length() > 1` 校验；[TeamSavedData.java](src/modern/java/com/lulan/shincolle/team/TeamSavedData.java) `createTeam` 新增 `removeStaleTeamsByLeaderName` 自动清理同队长旧队伍并清洗盟友/黑名单引用。
  - **跨维度同步与双队存在性门控**：[TeitokuHelper.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java) `syncGameplayStateToRelevantPlayers` 移除同维度过滤改为向全服在线玩家广播；`isAlly` / `isBanned` 在 `ownerA != ownerB` 时强制要求双侧活跃队伍均存在。
  - **方块所有权与同盟/OP 权限**：[PlayerOwnerData.java](src/modern/java/com/lulan/shincolle/ownership/PlayerOwnerData.java) 对齐 `TeamHelper.isUsableByPlayer`：放行 level-2+ OP（`hasPermissions(2)`）及服务端/客户端同步中的同盟玩家，并在 `uid <= 0` 时经 `TeitokuSavedData.getUid(uuid)` 解析所有者 UID。
- 在 [GameplayParityGameTests.java](src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 新增 `b3TeamLifecycleCooldownAndNameValidation` 与 `b3TeamAllyBannedExistenceAndBlockOwnershipPermissions`；验证 `git diff --check`、`src/main/` 零改动及 JDK 17 `cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon` 通过 **`All 284 required tests passed :)`**（284/284，0 失败，0 跳过；阶段证据见 [B3 Part 1 results](migration_batches/2026-11-17-b3-multiplayer-team/results/b3-part1.json)）。

## 2026-11-16

### B2-PACKET-PARITY：全 84 有效旧版 PID 对照与 11 项单人/核心同步控制补齐

- 通过 Swarm 并发完成旧版 `C2SGUIPackets`（30 有效 PID + `NO_USE(34)`）、`C2SInputPackets`（15 PID）、`S2CGUIPackets`（21 PID）、`S2CEntitySync`（13 PID）、`S2CReactPackets`（5 PID）与 `S2CSpawnParticle` 共 **84 个有效 PID** 的全量逐 PID 核对：`23 MATCHED`、`33 NATIVE_EQUIVALENT`、`11 IMPLEMENTED_HERE`、`17 DEFERRED_TO_B3_OR_B6 / PARTIAL_DEFERRED`。
- 严格在既有 `GameplayCommandType`/`ServerboundGameplayCommandPacket` 与原版 `ClientboundBlockEntityDataPacket` 框架内补齐 11 项单人/通用核心缺口：
  - `TileWaypoint` & `TileDesk` & `Desk_FuncSync(77)`：[WaypointBlockEntity.java](src/modern/java/com/lulan/shincolle/blockentity/WaypointBlockEntity.java) 与 [DeskBlockEntity.java](src/modern/java/com/lulan/shincolle/blockentity/DeskBlockEntity.java) 补全 `getUpdateTag()`/`getUpdatePacket()`，Desk 持久化并同步 `radarZoomIndex`/`bookChapter`/`bookPage` 及 `SYNC_DESK_FUNC`。
  - `SyncPlayerItem(24)` & `SetFormation(31)` & `SetUnitName(36)`：[TeitokuHelper.java](src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java)、[GameplayCommandHandler.java](src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java) 与 [FormationScreen.java](src/modern/java/com/lulan/shincolle/client/screen/FormationScreen.java) 补齐跨模式切回单舰聚焦首舰（`% 3 != 0`）、`SET_FORMATION` 与按分队 `0..8` 独立命名的 `SET_UNIT_NAME`（不再受提督 Team 前置阻断）。
  - `SetUnatkClass(50)` & `HitHeight(35)`：恢复 `OPTOOL_TOGGLE_UNATTACKABLE` 服务端 level-2 OP 校验与 3 语种拒绝提示；新增 `SET_SHIP_HIT_ZONE`、[PointerClientEvents.java](src/modern/java/com/lulan/shincolle/client/PointerClientEvents.java) 4-tick 抚摸区射线计算及 [LegacyShipEntity.java](src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) 摸头区 `+30` 士气反应。
  - `PlayerSkill(12)` & `MountGUI(1)` & `MountMove(0)`：修正 [PlayerSkillHostResolver.java](src/modern/java/com/lulan/shincolle/playerskill/PlayerSkillHostResolver.java) 与 [MorphHelper.java](src/modern/java/com/lulan/shincolle/morph/MorphHelper.java) 解析舰娘坐骑与背负乘客技能宿主；补齐 [ShipCommandService.java](src/modern/java/com/lulan/shincolle/network/ShipCommandService.java) 骑乘打开舰娘背包、[MorphClientEvents.java](src/modern/java/com/lulan/shincolle/client/MorphClientEvents.java) 输入捕获及 [LegacyMountEntity.java（已撤销）](MIGRATION-DECISIONS.md#d-018)/[LegacyShipMountEntity.java](src/modern/java/com/lulan/shincolle/entity/mount/LegacyShipMountEntity.java) WASD+Jump 接管转向。
- 在 [GameplayParityGameTests.java](src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 新增 5 项 B2 `@GameTest` 并修复 `heavyCruisersKeepLegacyNightEffectsAndNePush` `updateSkyBrightness()` 夹具时序竞争；验证 `git diff --check`、JDK 17 `cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon` 全部通过，实测 **`All 282 required tests passed :)`**（282/282，0 失败，0 跳过）；[ROADMAP.md](ROADMAP.md) 将唯一 `NEXT` 推进为 `B3-MULTIPLAYER-TEAM`（证据见 [B2-PACKET-PARITY](migration_batches/2026-11-16-b2-packet-parity/)）。

### MINECRAFT-BLOCK-TAGS：补全现代原版方块采集工具与信标底座 Tag（生存挖矿掉落修复）

- 巡检世界内容与打包资源时发现：`ModBlocks` 已给 `blockpolymetalore`、`blocksmallshipyard` 等配置 `.requiresCorrectToolForDrops()`，但 `src/modern/resources/data/minecraft/tags/blocks/` 为空（相关 JSON 仅残留在不参与打包的 `src/main/resources/` 且分级与 1.12 Java 源码不完全一致）。
- 严格对照 1.12.2 各方块源码 `setHarvestLevel` 与 `isBeaconBase` 补建 5 个现代 Tag 文件：`mineable/pickaxe.json`（含 level-0 7 种 + `blockpolymetalore` + `blocksmallshipyard`，剔除 `harvestLevel(-1)` 的 `blockwaypoint`）、`mineable/shovel.json`（5 种怨念/多金属砾石方块）、`needs_stone_tool.json`（`blockpolymetalore` level 1）、`needs_diamond_tool.json`（`blocksmallshipyard` level 3）及 `beacon_base_blocks.json`（全部 7 个 1.12 `isBeaconBase` 方块）。
- 在 [GameplayParityGameTests.java](src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 新增 `blockHarvestToolTagsAndBeaconBaseMirrorLegacy` 验证工具等级门槛、`Block.getDrops` 矿石/砾石掉落及信标底座标签。
- 验证 `git diff --check`、`cleanProcessResources`、`build`、`verifyItemModelCoverage` 与 `runGameTestServer` 全部通过，实测 **`All 277 required tests passed :)`**（277 passed / 0 failed / 0 skipped）。

### B1-GUI-PARITY：10 Gui 与 9 Container 界面交互、Slot 坐标与状态对齐

- 逐文件对照旧版 1.12.2 全部 10 个 `Gui*` 与 9 个 `Container*` 源码及对应贴图：`GuiLargeShipyard`/`ContainerLargeShipyard`、`GuiVolCore`/`ContainerVolCore`、`GuiCrane`/`TileEntityCrane`、`GuiDesk`、`GuiSmallShipyard`、`GuiRecipePaper`、`GuiShipInventory`、`GuiFormation`、`GuiMorphInventory`。
- LargeShipyard 修正：移除 `(9, 83)` 误设的物品槽（旧版贴图为能量槽），完整补齐 y=116 上的 9 个托盘槽位（x=25..169）；Screen 补齐建造/提取模式切换覆盖层 `(208, 82)`、选中材料边框 `(208, 64)`、8 个加减数量微调按钮面板 `(0, 223)` 及 x=125 黄色库存数量文本。
- VolCore（LegacyCore）修正：将 3x3 燃料槽由 `(18, 22)` 调整为精确旧版坐标 `(62 + col*18, 19 + row*18)`，玩家背包/快捷栏调整为 `(8, 84)` / `(8, 142)`；Screen 高度修正为 166，燃料刻度重采样为 31px，补入 `(7, 6)` 开关按钮点击与激活覆盖层 `(12, 166)`。
- Crane 终端修正：补齐 `(7, 6)` 开关按钮与激活覆盖层 `(176, 0)`、左键递增/右键递减双向循环等待模式、电源状态 tooltip 及对应 BlockEntity/Menu 按钮协议。
- Desk 终端安全修正：`DeskReferenceScreen` 访问服务端类阻挡列表与雷达目标扫描时增加单人服务端非空保护，避免多人连线客户端 NPE。
- 其余 6 组 GUI/Container 均经逐行对照确认功能与坐标已 100% 对齐现代实现，无需改动。
- 验证 `git diff --check`、`compileJava` 与 `runGameTestServer` 全部通过，实测 **`All 276 required tests passed :)`**（276/276，0 失败，0 跳过）；在 [ROADMAP.md](ROADMAP.md) 将唯一 `NEXT` 推进为 `B2-PACKET-PARITY`（证据见 [B1-GUI-PARITY](migration_batches/2026-11-16-b1-gui-parity/)）。

### GAMETEST-BACKFILL：从 Git Dangling Blob 找回 182 项测试，回归套件恢复至 276 项全通

- 针对 2026-09-16 因 Worldgen 调试误执行 `git checkout HEAD` 导致的 182 项历史测试丢失，通过 `git fsck --lost-found` 取证成功从未提交 dangling blob `4d70aebad7532c05ebc34a588c84caedb1bd4102` 中锁定 2026-09-16 原始测试源（14,363 行）。
- 采取非破坏性补齐策略：严格保留活动工作区经 `GAMETEST-REPAIR-B4` 修复的 91 项用例与 `B5-ORE-TAGS` 新增的 3 项用例（共 94 项）；补回 182 项缺失 `@GameTest` 方法、8 项配套辅助方法与 29 项 import。
- 完整恢复 `SHIP-01` 至 `SHIP-ATTACK-PROFILE-AUDIT` 11 个批次所建立的自动化回归覆盖（涵盖路径回溯、潜艇弹道、姬级投射物、六驱骑乘、大招技能阶段、全舰种伤害与攻击通道门禁）。
- 验证 `git diff --check`、`compileJava` 与 `runGameTestServer` 全部通过，实测 **`All 276 required tests passed :)`**（276 passed / 0 failed / 0 skipped，耗时 36s），生产代码保持 0 改动，实现与测试保障全面闭环（证据见 [GAMETEST-BACKFILL](migration_batches/2026-11-16-gametest-backfill/)）。

### B5-ORE-TAGS：OreDictionary → Forge Tags、配方纠偏补全与 Crane/Task 运行期匹配收口

- 逐文件审计 1.12.2 `ModOres`、`ModRecipes`、`ConfigHandler`、全量 84 个旧版 JSON 配方（实际含 **39** 个 `forge:ore_dict` 条目）、`InventoryHelper`、`TileEntityCrane`、`ContainerCrane`、`GuiCrane`、`GuiHelper` 与 `EventHandler`。
- 新增 [LegacyItemMatchHelper.java](src/modern/java/com/lulan/shincolle/crafting/LegacyItemMatchHelper.java) 并统一 `LegacyShipBehaviorCatalog#matchesLegacyTaskItem` 与 `CraneBlockEntity#matchesTransferFilter`；为 Crane 补齐 `M/D/N`（`checkMetadata`/`checkOredict`/`checkNbt` 默认 `false`、`"meta"`/`"dict"`/`"nbt"` NBT 持久化、Menu 按钮 `6..8`、`CraneTerminalScreen` 坐标图标 `(176,13/24/46)` 与 3 语言本地化）。
- 新增 9 个 `forge:tags/items` JSON（`paper` + 8 个跨 mod 兼容空 Tag），修正 18 个现代配方的 Tag/形状/数量（含损坏的 `ammo1`/`ammo2`/`ammo3`、`blockwaypoint` 竖直、`combatration` 两行、`combatration2` 行序/生鱼、`marriagering` `abyssnugget`），补齐 26 个 1.12 遗漏配方（13 个常驻 + 13 个 `forge:not(forge:tag_empty)` 条件跨 mod 配方）。
- 修正 B5 相关测试网格并新增 3 个 B5 `@GameTest`；验证 `git diff --check`、`cleanProcessResources`、`processResources`、`compileJava`、`verifyItemModelCoverage` 与 `runGameTestServer` 全部通过，实测 **`All 94 required tests passed :)`**（94/94）；[ROADMAP.md](ROADMAP.md) 将唯一 `NEXT` 推进为 `GAMETEST-BACKFILL`（证据见 [B5-ORE-TAGS](migration_batches/2026-11-16-b5-ore-tags/)）。

### GAMETEST-REPAIR-B4：9 项失败 GameTest 真源核对修复与 B4 收口

- 逐项对照 1.12.2 源码核实 `HEAD` 基线 [GameplayParityGameTests.java](src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 全部 9 个失败用例，确认现代生产实现与 B4 `polymetal_gravel.json` 均已符合 1.12 语义。
- 在不改变生产行为的前提下完成 9 项测试的最小精确修正与测试坐标隔离（详见 [GAMETEST-REPAIR-B4 decision](migration_batches/2026-11-16-gametest-repair/decision.md)），并同步关闭 [WORLDGEN-AUDIT](migration_batches/2026-09-16-worldgen-audit/) 的 B4 测试遗留。
- 验证 `git diff --check`、`compileJava`、`processResources`、`verifyItemModelCoverage` 与 `runGameTestServer` 全部通过，实测 **`All 91 required tests passed`**（91/91）。
- 在 [ROADMAP.md](ROADMAP.md) 将唯一 `NEXT` 推进为 `B5-ORE-TAGS`，并冻结 [B5-ORE-TAGS protocol](migration_batches/2026-11-16-b5-ore-tags/protocol.md)。

### DOC-ALIGN：现状体检与全量迁移文档对齐

- 对当前工作区执行现状检查：`git diff --check`、`compileJava`、`processResources`、`verifyItemModelCoverage` 均通过，legacy `src/main/` 保持零改动，生产代码改动全部保留在工作区；`runGameTestServer` 因 [GameplayParityGameTests.java](src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 停留在基线 `HEAD`（91 项）而实测为 `82 passed / 9 failed`。
- 对齐全部控制面与批次文档：[CURRENT_STATE.md](CURRENT_STATE.md)、[ROADMAP.md](ROADMAP.md)、[MIGRATION-SHIP-MATRIX.md](MIGRATION-SHIP-MATRIX.md)、[MIGRATION-SCOPE.md](MIGRATION-SCOPE.md)、[MIGRATION-TEST-MATRIX.md](MIGRATION-TEST-MATRIX.md)、[V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/results/README.md) 与 [WORLDGEN-AUDIT](migration_batches/2026-09-16-worldgen-audit/)；将 [2026-09-04-ship-07-akagi-cahime-akatsuki](migration_batches/2026-09-04-ship-07-akagi-cahime-akatsuki/STATUS.md) 空草稿目录标记为 `SUPERSEDED`，并在 [ROADMAP.md](ROADMAP.md) 冻结唯一下一步 `GAMETEST-REBUILD`。

## 2026-09-16

### V0 SP mainline 正式收口

- 用户于 2026-09-16 人工确认 V0 单人世界验收通过，SP mainline 正式完成。
- M0-M9 全部从 `PASS (SP mainline; V0 pending)` 提升为 `Done (SP mainline)`。
- V0 验收从 `Pending` 标记为 `Done (2026-09-16 用户确认)`。
- 当时 GameTest 回归 271/271 通过，客户端启动/资源加载/进世界均无阻塞问题；后续迁移进入 backlog 阶段。批次证据见 [V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/)。

### WORLDGEN-AUDIT：Worldgen 与 Chest Loot 按旧版默认值审计完成

- 对照旧版 `ShinColleWorldGen`（Polymetal ore）、`WorldGenPolyGravel`（海底砾石）、`ChestLootTable`（10 个箱子战利品）、`ConfigLoot` 与 `ModWorldGen`。
- 确认现代侧 configured/placed feature、biome modifier 与 `ChestLootInjector` 已覆盖旧版默认 worldgen 和宝箱战利品语义（另确认 `data/forge/tags` 下已存在 16 个 Forge Tag JSON，但 `ModOres`/`InventoryHelper` 代码侧 tag 审计留待 `B5-ORE-TAGS`）。
- 修正 `polymetal_gravel.json` 目标方块列表：从 `[stone, gravel, sand, dirt]` 修正为 `[stone, gravel]`，匹配旧版 `ConfigHandler.polyGravelBaseBlock` 默认值 `{true, true, false, false}`（sand 和 dirt 在 1.12 默认关闭）。
- `compileJava`、`processResources` 通过；该会话因编辑故障将 `GameplayParityGameTests.java` 还原至 `HEAD`，配套 worldgen 测试断言更新归入 `GAMETEST-REBUILD`。批次证据见 [WORLDGEN-AUDIT](migration_batches/2026-09-16-worldgen-audit/)。

## 2026-09-05

### SHIP-ATTACK-PROFILE-AUDIT：攻击通道与特殊 hook 按旧版有效行为收口

- 对照旧版 `ShipStateHandler.initFirst()`、45 个活动实体构造函数、CV/hostile 基类和具体攻击入口，确认 45 个基础 class、64 个注册变体的 light/heavy/air profile 没有未解释差异。
- 保留 `EntityBattleshipTa` 只有轻型 Rensouhou 实现的边界：继承的 raw heavy flag 不开启 modern generic heavy hook；19 个 hostile Mob 变体遵循旧版 hostile base 的通道覆盖，Hime 风格 hostile 变体保留原始 class 语义。
- 本批没有新增生产行为；仅补充语义注释和全注册矩阵 GameTest。`git diff --check`、`compileJava`、`processResources`、`runGameTestServer` 通过，最终 `All 271 required tests passed`。批次证据见 [SHIP-ATTACK-PROFILE-AUDIT](migration_batches/2026-09-05-ship-attack-profile-audit/)。

### ALL-SHIP-OVERRIDE-AUDIT：全舰种 override 与攻击源类型按旧版收口

- 对照旧版 `ModEntity.regShipEntity`、基础实体继承层、实体 override、`IShipAttackBase#getDamageType` 和 `BuffHelper.applyBuffOnDamageByLight`，确认 45 个基础 class ID、64 个注册变体没有未解释的逐舰分派遗漏。
- 将旧版实际光照伤害表和类型映射接入 `LegacyShipCombatHelper`，在 `LegacyShipEntity.hurt` 的防御修正后执行；飞机攻击保留 `AIRPLANE` direct source，普通/大型 Rensouhou 保留 `DESTROYER`/`BATTLESHIP`。
- 现代代码只做 API 最小适配：飞机通过 `DamageSources.thrown(aircraft, owner)` 保留 direct entity，其余舰船/投射物继续使用现有 source bus。`git diff --check`、`compileJava`、`processResources`、`runGameTestServer` 通过，最终 `All 270 required tests passed`。批次证据见 [ALL-SHIP-OVERRIDE-AUDIT](migration_batches/2026-09-05-all-ship-override-audit/)。

### V0-CLIENT-GATE：客户端启动级验收

- Java 17 `runClient` 已启动 Minecraft Forge 原生窗口，日志确认 OpenGL 4.6、ShinColle 初始化、资源 reload、OpenAL 和纹理图集创建完成；截获日志未见 ShinColle 崩溃、缺失模型/贴图或 fallback 信号。
- CUA 未暴露可操作的原生客户端（`apps: []`），因此进世界、命令、GUI、战斗、投射物、反馈和存档恢复仍未验证；详细结果见 [V0-CLIENT-GATE](migration_batches/2026-09-05-v0-client-gate/)。
- 无代码变更；V0 T4 继续保持唯一待办，不能将启动级通过写成单人主线最终完成。

这里只记录短历史和批次链接。当前事实见 [CURRENT_STATE.md](CURRENT_STATE.md)，下一步见 [ROADMAP.md](ROADMAP.md)。

## 2026-09-05

### COMBAT-REGRESSION-SUITE：固定战斗回归按旧版入口收口

- 对照旧版 Nagato、Tenryuu、Tatsuta、Yamato 的具体攻击/事件入口，以及潜艇姬 `EntityAbyssMissile` 移动构造规则；确认现代生产战斗实现没有需要新增的分派或特判。
- Nagato 测试改为同一加载区块的 y=200 隔离夹具，避免并行 GameTest 邻近实体污染旧版 16/12/16 搜索；Tenryuu、Tatsuta、Yamato 的冷却循环模拟服务器递增后的非边界 tickCount；潜艇姬测试改用旧版短距/加速度规则计算 move type，保留 ARC 编号与直线运动的旧版分支。
- 本批只修正测试夹具和断言，未修改生产战斗代码；`git diff --check`、`compileJava`、`processResources`、`runGameTestServer` 均通过，最终 `All 269 required tests passed`。批次证据见 [COMBAT-REGRESSION-SUITE](migration_batches/2026-09-05-combat-regression-suite/)。

### MODERN-GAP-AUDIT / ALL-SHIP-IDLE-SUPPLY：公共资源入口按旧版继承层级补齐

- 静态扫描发现现代 128 tick idle-supply gate 只覆盖前序 9 个 class ID；对照旧版确认 Hime 类继承 `BasicEntityShipSmall/CV`，不能用现代 hostile 标志代替旧版继承关系。
- `LegacyShipBehaviorCatalog` 现在为 45 个活动 `BasicEntityShip` 注册类返回旧版 idle cost；`LegacyShipEntity` 使用同一公共路径，hostile Mob 变体保持排除，并补回 Transport Wa 的 tick 200 后移动经验。
- 新增全 45 类 GameTest 通过。整套 269 个 GameTest 中 265 个通过，Nagato、Tenryuu、Tatsuta、Yamato 的 4 个旧战斗回归失败转入独立 [COMBAT-REGRESSION-SUITE](migration_batches/2026-09-05-combat-regression-suite/)；本批未启动客户端。
- 详细旧版行级对照和结构化结果见 [ALL-SHIP-IDLE-SUPPLY](migration_batches/2026-09-05-all-ship-idle-supply/)。

## 2026-09-04

### SHIP-UNIVERSE-AUDIT：活动旧版实体全集核对

- 以旧版 `ModEntity.regShipEntity` 和实际实体源文件定义活动舰种全集：64 个注册行，包含 45 个 friendly 基础舰种和 19 个 hostile 变体；所有注册源文件存在。
- 现代 `ShipEntitySpecs` 的 64 个规格行和舰种矩阵的 45 个基础行与旧版活动集合差集均为空。`ID.java` 中 40 个只有名称映射、没有实体源/注册入口的条目记录为 `HISTORICAL_SOURCE_GAP`，不凭名称造实体。
- 本批无代码、资源或运行时变更，仅完成静态证据记录；精确结果见 [SHIP-UNIVERSE-AUDIT](migration_batches/2026-09-04-ship-universe-audit/)。

### HOSTILE-TARGET-RANGE：敌对目标范围与技能替换边界跟进

- 对照旧版 `EntityAIShipRangeTarget`、`EntityAIShipRevengeTarget`、`TargetHelper` 和 Tenryuu/Tatsuta 的固定搜索逻辑；现代低整数攻击范围现在使用等价的 `FollowMax + 2` 有效回退，普通 Boss 不扩大追击范围。
- hostile Tenryuu/Tatsuta 技能替换目标恢复旧版固定 `8/13` 搜索半径和 `SelectorForHostile` 的 LivingEntity 筛选族；现有敌对战斗反应 actionbar gate 已匹配旧版意图，无需代码变更。
- 修正两个测试夹具后，`git diff --check`、`compileJava`、`processResources`、`runGameTestServer` 通过，`All 268 required tests passed`。批次证据见 [HOSTILE-TARGET-RANGE](migration_batches/2026-09-04-hostile-target-range/)，V0 原生客户端进世界人工验收仍待完成。

### HEAVY-SPECIAL-MISS：特殊重攻击 miss 语义复核

- 对照旧版 `BasicEntityShipHostile.attackEntityWithHeavyAmmo` 与现代 `LegacyShipEntity.performCatalogSpecialAttack`、通用攻击入口；当前实现已将 `roll.miss()` 保留在投射物路径，miss 不会直接造成非零伤害。
- 既有 `heavyProjectileMissesRemainProjectileOnly` 与 `catalogHeavyMissesRemainProjectileOnly` 已覆盖确定性 miss、目标生命、投射物标记、冷却和资源消耗；本批结论为 `NO CODE CHANGE REQUIRED`。批次证据见 [HEAVY-SPECIAL-MISS](migration_batches/2026-09-04-heavy-special-miss/)。

### SP-CLOSE：M0-M9 单人主线跨模块收口审计

- 45 个唯一舰种 class ID 已有矩阵锁定和 SHIP-01 至 SHIP-09 批次证据；新增 `singlePlayerAiPriorityContractMatchesLegacyBoundaries`，将单人 AI 优先级固定为可回归契约。
- 定向对照旧版 `EntityAIShipRangeTarget`、`TargetHelper.SelectorForHostile` 与现代 hostile target/reaction 路径；敌对 Boss 追击范围和敌方飞机/命中提示边界已匹配，结论为 `LOCKED / no change`。
- 追加 45 个非 Mob 具体舰种和 19 个 hostile Mob 变体的特殊入口审计；航空、姬级被动、特殊攻击、合体、潜艇/运输和反击/post-hit motion 均与现有 modern catalog/profile 入口对齐，结果见 [ship-override-audit.json](migration_batches/2026-09-04-sp-close-singleplayer-mainline/results/ship-override-audit.json)。本次无代码变更；历史可选配置开关继续列入 parity backlog。
- `git diff --check`、`compileJava`、`processResources`、`runGameTestServer` 通过，`All 266 required tests passed`。M0-M9 记录为 `PASS (SP mainline)`；原生客户端窗口未暴露，V0 进世界人工验收仍待完成。批次证据见 [SP-CLOSE](migration_batches/2026-09-04-sp-close-singleplayer-mainline/)。

### SHIP-06：U-511/Ro-500/Submarine Hime/Yamato/Kaga runtime、模型与投射物边界批次

- class 38、39、44、46、47 的潜艇/潜艇姬/Yamato/Kaga 旧版模型源、`showEquip` 状态、scale-level 分支、双导弹、beam 和 host/miss 边界已完成现代侧最小适配。
- 修正潜艇姬侧向发射坐标与 miss 散射的 `nextFloat()` 语义；保留旧版敌对同队免伤、宿主排除和 miss 不造成伤害规则。
- `All 261 required tests passed`；Java 17 客户端启动、OpenGL、资源 reload 和单人集成连接通过，原生窗口进世界人工交互保留到 V0。批次过程和精确证据见 [2026-09-04-ship-06-submarine-yamato-kaga](migration_batches/2026-09-04-ship-06-submarine-yamato-kaga/)。

### SHIP-07：Akagi/CA Hime/Akatsuki/Hibiki/Ikazuchi runtime、骑乘燃料边界与模型批次

- class 48、49、51、52、53 的旧版实体、飞机继承、夜战/push、六驱逐骑乘状态、Raiden/gattai 燃料解体和目标/资源边界完成现代侧最小对照。
- 补齐五个旧模型的 `showEquip` 位/条件分支和 parser/bake 断言；补回 Hibiki、Ikazuchi、Inazuma 的乘员自身 no-fuel 解体回调，未引入第二套协议或模型架构。
- `All 263 required tests passed`；Java 17 客户端启动、OpenGL 4.6、资源 reload 和模型日志闸门通过，原生窗口进世界人工交互保留到 V0。批次过程和精确证据见 [2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi](migration_batches/2026-09-04-ship-07-akagi-cahime-akatsuki-hibiki-ikazuchi/)。

### SHIP-08：Inazuma/Tenryuu/Tatsuta/Atago/Takao runtime、骑乘边界与模型批次

- class 54、56、57、58、59 的旧版实体入口、巡洋舰技能阶段、攻击资源边界、骑乘/敌对 profile 和继承攻击路径完成定向对照；没有新增分派逻辑。
- 补齐五个旧模型的精确 `showEquip` 位分支，并接入现有 parser/bake 路径；敌对 Boss 远距离追击和玩家提示策略保持不变。
- `All 264 required tests passed`；Java 17 客户端启动、OpenGL、资源 reload 和模型日志闸门通过，原生窗口进世界人工交互保留到 V0。批次过程和精确证据见 [2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao](migration_batches/2026-09-04-ship-08-inazuma-tenryuu-tatsuta-atago-takao/)。

### SHIP-09：Kongou/Hiei/Haruna/Kirishima/SSNH runtime、模型与投射物边界批次

- class 60、61、62、63、72 的旧版实体入口、金刚型光环/重炮/scale、SSNH 双导弹/深度/士气/骑乘边界完成定向对照；共享 heavy miss 分支确认继续保留 projectile-style miss 语义。
- 补齐四个金刚旧模型的 `showEquip` 位分支，修正 hostile Hiei 的 scale-dependent chimney 点，并复用现有 SSNH/Northern pending-target 骑乘状态；没有新增架构、协议或 legacy 改动。
- `All 265 required tests passed`；Java 17 客户端启动、OpenGL 4.6、资源 reload 和模型日志闸门通过，原生窗口进世界人工交互保留到 V0。批次过程和精确证据见 [2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new](migration_batches/2026-09-04-ship-09-kongou-hiei-haruna-kirishima-submarine-hime-new/)。

### SHIP-05：Midway/Northern/Carrier WD/Shimakaze/Nagato special runtime 批次

- class 30、31、33、36、37 的旧版特殊 runtime、召唤物生命周期、攻击/冷却边界、模型状态位和 hostile servant 语义已完成现代侧最小适配。
- 补齐五个旧模型的 `showEquip` 状态目录；修正 Northern Hime 的 `launchHeight = height`；保留敌对 Rensouhou 的 `OnSightChase=false` 并使用现代投射物伤害源。
- `All 258 required tests passed`；Java 17 客户端启动、OpenGL、资源 reload 和单人集成连接通过，原生窗口进世界人工交互保留到 V0。批次过程和精确证据见 [2026-09-04-ship-05-special-runtime](migration_batches/2026-09-04-ship-05-special-runtime/)。

## 2026-09-03

### SHIP-04：Airfield/Battleship/Destroyer/Harbour/Isolated Hime visual/projectile 批次

- class 21、26、27、28、29 的旧版模型源解析/bake、`showEquip` 状态位、飞机/集束/railgun/黑洞投射物族和反馈边界已完成现代侧最小适配。
- 修正 Airfield/Harbour 的 `0.7F` 飞机起飞高度，追加隔离既有 ordinal 的 `LEGACY_RAILGUN` 粒子类型，并保留旧版 type-2 使用共享 missile 模型的边界。
- `All 256 required tests passed`；Java 17 客户端启动和资源 reload 通过，原生窗口未暴露给当前自动化环境，进世界人工交互保留到 V0。批次过程和精确证据见 [2026-09-03-ship-04-hime-visual-projectile](migration_batches/2026-09-03-ship-04-hime-visual-projectile/)。

### SHIP-03：Transport Wa、Submarine Ka/Yo/So、Carrier Hime combat behavior 批次

- class 16、17、18、19、20 的攻击选择、潜艇专用导弹、待机 grudge、货物补给、飞机容量/恢复和 Carrier Hime 起飞高度已按旧版入口完成现代侧最小修正。
- 补回 Transport Wa 的 AP idle cost `3`、三艘潜艇的 SS idle cost `4`，并保留 Carrier Hime 的 `0.9F` 机体起飞高度；旧版 easy-mode 倍率和友方 CV/现代 hostile spec 的资源模型差异留在 parity backlog。
- `All 254 required tests passed`；批次过程和精确证据见 [2026-09-03-ship-03-combat-behavior](migration_batches/2026-09-03-ship-03-combat-behavior/)。

### SHIP-02：Ne/Wo/Ru/Ta/Re 命令、路线与目标边界批次

- class 10、12、13、14、15 的旧版命令距离、路线回退、护卫状态、目标边界和 waypoint 旧字段已完成现代侧最小适配。
- 恢复严格小于 64 格的提督到舰船命令范围；移动坐标、护卫目标和攻击目标不再被现代侧额外距离墙截断，并保留旧版队伍全员门槛。
- `All 253 required tests passed`；批次过程和精确证据见 [2026-09-03-ship-02-command-route-target](migration_batches/2026-09-03-ship-02-command-route-target/)。

### SHIP-01：驱逐舰与重巡 Ri runtime 批次

- class 0、1、2、3、9 的旧版攻击延迟、128 tick 资源消费和 runtime save 域已完成对照迁移。
- 复核旧版库存优先级，确认待机 grudge 恢复使用 `Grudge` -> `BlockGrudge`，并加入回归断言。
- `All 250 required tests passed`；批次过程和精确证据见 [2026-09-03-ship-01-destroyer-ri](migration_batches/2026-09-03-ship-01-destroyer-ri/)。

### DOC-01：建立解耦文档体系

- 新增 Control Plane：规则、当前状态、路线、模块、架构和命名文档。
- 新增 Question Plane：批次模板与 Rensouhou/潜艇/敌对目标审计记录。
- 旧迁移文档收缩为唯一职责或兼容指针；未运行代码构建和客户端。

## 2026-09-02

### Rensouhou 行为批次

批次结论和测试证据见 [2026-09-02-rensouhou](migration_batches/2026-09-02-rensouhou/)。

### 潜艇审计

无代码变更的审计记录见 [2026-09-02-submarine-audit](migration_batches/2026-09-02-submarine-audit/)。

### 敌对目标与提示审计

静态基线和待单独复现的问题见 [2026-09-02-hostile-target-audit](migration_batches/2026-09-02-hostile-target-audit/)。

## 历史迁移

更早的逐舰实现、测试数量和提交细节属于历史记录；不要复制到当前状态或下一步队列。需要追溯时从 [MIGRATION-SHIP-MATRIX.md](MIGRATION-SHIP-MATRIX.md) 的 class ID 和批次链接进入。
