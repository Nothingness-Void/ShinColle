# Decision Record: B3-MULTIPLAYER-TEAM

- **已撤销**：本批中将创造模式玩家排除于 `friendlyFire` 免伤门禁之外，以及仅按“友好舰船”而非主人归属判断 `dmgSvS` 条件的决定；其他结论不变。复核与落实见 [战斗与投射物批次](../2026-11-22-parity-fix-combat-projectile/decision.md)。

- 结论：`COMPLETED`（Part 1 与 Part 2 全部收口，全套 `286/286` GameTest 全绿通过）
- 影响矩阵：M2、M3、M4、M6、M7、M8

## Facts established

### Part 1
- `CapaTeitoku.java#L59` 新建玩家与缺字段初始 `teamCooldown` 为 `20` ticks（`INITIAL_TEAM_COOLDOWN`），建队/解散后重置为 `6000`；现代 `TeitokuData` 误将缺省/初值设为 `6000`。
- `ServerProxy.java#L840,L858` 要求队伍名归一化长度 `> 1`（不少于 `2` 字符）并清理同队长名下的旧队伍。
- `ServerProxy` 队伍广播不限制同维度；`TeamHelper#checkIsAlly` / `checkIsBanned` 跨队比较时要求双侧活跃队伍对象均存在；`TeamHelper#isUsableByPlayer` 放行所有者、同盟与 level-2+ OP。

### Part 2
- `AttackHandler.java#L215-222` 中 `ConfigHandler.dmgSvS` 为百分比缩放（`amount * dmgSvS * 0.01F`）且仅在攻击方为友好舰船/召唤物/坐骑时生效；同时 `BasicEntityShip` / `BasicEntitySummon` 在 `!friendlyFire` 时免疫非主人非创造玩家及同盟伤害，反伤效果不触发于同盟。
- `TargetHelper.Selector#L123-145` 定义 `shipAttackPlayer` `0..3`（0: 不打玩家；1: 仅敌对/拉黑队伍；2: 敌对+中立即 `!isAlly`；3: 除主人外所有玩家含同盟），`SelectorForHostile#L359` 遵循 `mobAttackPlayer`；而 `RevengeSelector` 与 `C2SGUIPackets.AttackTarget` 允许复仇及指挥攻击中立非同盟目标。
- `InteractHelper.java#L200-346` 与 `ShipCalc.getKaitaiItems` 定义：潜行右键 `KaitaiHammer` 触发 `emotion 8`/10 格内邻舰 `emotion 6` 与分舰种 4 格解体材料掉落（大型敌对/姬级受 `easyMode` 缩减，Raiden/驱逐/SS 按配置计数）；左键一击回收生成蛋；`OwnerPaper` 移交触发 `T_T`（`emotion 2`）与 `MARRY` 音效。
- `GuiDesk.java#L2164-2171` 每 `64` ticks（`ConfigHandler.radarUpdate`）向服务端请求雷达列表以补充多人模式下超出客户端追踪距离的舰船、物品及设施。
- 无改动审计：`LegacyShipBeamEntity.java#L181-185` 对非 `isLegacyProjectileDamageAllowed` 目标调用 `hurt(..., 0.0F)` 属于有意保留的 1.12.2 击退行为（由 `GameplayParityGameTests#L13323` 断言），经 `isLegacyFriendlyFireAllowed` 在 `hurt` 层拦截实际伤害与同盟目标后无需修改 `LegacyShipBeamEntity`。

## Code changes

### Part 1
- `TeitokuData.java`, `TeitokuSavedData.java`, `TeamSavedData.java`, `TeitokuHelper.java`, `PlayerOwnerData.java`。

### Part 2
- [ShinColleConfig.java](../../src/modern/java/com/lulan/shincolle/config/ShinColleConfig.java) & [ShinColle.java](../../src/modern/java/com/lulan/shincolle/ShinColle.java)：建立并注册 `COMMON_SPEC` 与 `CLIENT_SPEC`（带未加载兜底与测试覆写接口）。
- [LegacyShipEntity.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java)：`hurt` 加入非主人/创意玩家 `friendlyFire` 门禁、`isLegacyFriendlyFireAllowed` + 友方舰 `dmgSvS * 0.01F`、同盟免反伤；新增 `canCommandOrRevengeEngage` 与 `canEngage` 0..3 `shipAttackPlayer`/`mobAttackPlayer` 及 `aiAllowPvp && isBanned`；`OwnerPaper` 设置 `emotion(2)` 与 `MARRY`；`performKaitaiKill` + `performKaitai` 对齐两套锤子模式与掉落/情绪。
- [LegacyShipBehaviorCatalog.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java)：新增 `isLegacyFriendlyFireAllowed`（接入 `isLegacyProjectileDamageAllowed`）与 `createLegacyKaitaiDrops`。
- [LegacyShipRensouhouEntity.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java) & [LegacyShipAircraftEntity.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java)：`hurt` 加入所有者放行、非主人 `!friendlyFire` 与同盟免疫。
- [LegacyShipProjectileEntity.java](../../src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java)：`isLegacyContactTarget` 增加 `!ship.isAlliedTo(candidate)`。
- [LegacyShipOwnerHurtByTargetGoal.java](../../src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtByTargetGoal.java)、[LegacyShipOwnerHurtTargetGoal.java](../../src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtTargetGoal.java) & [ShipCommandService.java](../../src/modern/java/com/lulan/shincolle/network/ShipCommandService.java)：切换为 `canCommandOrRevengeEngage`。
- [KaitaiHammerItem.java](../../src/modern/java/com/lulan/shincolle/item/KaitaiHammerItem.java) & [OwnerPaperItem.java](../../src/modern/java/com/lulan/shincolle/item/OwnerPaperItem.java)：左键调用 `performKaitaiKill`；`resolveTransferTarget` 在 `PlayerList.getPlayers()` 后追加 `getAllLevels() -> players()` 回退查询。
- [GameplayCommandType.java](../../src/modern/java/com/lulan/shincolle/network/GameplayCommandType.java)、[GameplayCommandHandler.java](../../src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java)、[TeitokuHelper.java](../../src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java) & [DeskReferenceScreen.java](../../src/modern/java/com/lulan/shincolle/client/screen/DeskReferenceScreen.java)：补齐 `REQUEST_DESK_RADAR`、局部缓存保护与多人雷达/世界规则同步合并。
- [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java)：新增 `b3Part2FriendlyFirePvpModesAndCommandRevengeTargeting` 与 `b3Part2OwnerPaperKaitaiHammerAndDeskRadarSync`。

## No-change audits

- `LegacyShipBeamEntity.java`：保留既有 `0.0F` 非伤害击退边界，不破坏现有 GameTest 契约。

## Follow-up

- `B3-MULTIPLAYER-TEAM` 正式收口，按 [ROADMAP.md](../../ROADMAP.md) 推进唯一 `NEXT` 至 `B6-INTERMOD-DETAILS`（`ShinColleConfig` 其余运行期接入、9 条 `/ship*` 指令、动态光方块、`ShipKills` / `Raiden`、Curios 与外置 mod 文档边界）。
