# Frozen Protocol

本协议在读取 SP-CLOSE 实现源码和资源前冻结。它只验证单人主线完成定义，不把已有批次的详细结论复制到根文档；每个检查项必须引用既有批次证据或本批新增证据。

## Required evidence

1. M0-M1：注册、实体创建/移除/同步和存档 round-trip 的现代入口可追溯，legacy 目录无改动。
2. M2：fuel、ammo、grudge、morale、repair、rescue、cooldown、supply 的缺字段、旧字段、默认值和保存读取行为有断言。
3. M3：Pointer/ShipInventory 的 command packet、service、menu 命令覆盖 encode/decode、权限、目标无效、cooldown 和资源不足反馈。
4. M4：follow、guard、route、formation、target、supply、pickup 的优先级和边界有 GameTest；最终优先级为 death/stop/sit > explicit command > active combat target > auto supply > pickup > idle/follow。
5. M5-M7：45 个 class 的已锁定行为域和既有 SHIP-01 至 SHIP-09 证据没有未解释的 `PARTIAL`/`NEXT`；共享攻击结果覆盖 hit/miss/crit、资源、cooldown、summon、loot、projectile/reaction/player feedback。
6. M8：所有单人可到达的 model source、texture、projectile、screen、particle 和 sound 入口经过资源/客户端日志闸门，无静默 fallback。
7. M9：abyssmetal、polymetal、grudge、ammo、combat ration、shipyard/build 关键材料各有可发现来源；recipe、loot、advancement、encounter 和 DeskReference 形成单人指引闭环。
8. V0：真实客户端进世界操作按 `MIGRATION-FLOW-CHECKLIST.md` 逐项执行并记录真实结果；自动化无法执行的项目不得标绿。

## Modern entry files

只读取回答上述问题所需的现代入口；发现新的直接调用时先记录到本批 `decision.md`，再扩大范围：

- `src/modern/java/com/lulan/shincolle/ShinColle.java`
- `src/modern/java/com/lulan/shincolle/registry/ModEntityTypes.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStats.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEquipmentBehaviorState.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEquipmentProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFollowOwnerGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipGuardGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangeTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtByTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipPickItemGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFloatGoal.java`
- `src/modern/java/com/lulan/shincolle/network/ServerboundShipCommandPacket.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandAction.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandService.java`
- `src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java`
- `src/modern/java/com/lulan/shincolle/menu/ShipInventoryMenu.java`
- `src/modern/java/com/lulan/shincolle/client/screen/ShipInventoryScreen.java`
- `src/modern/java/com/lulan/shincolle/ownership/PlayerOwnerData.java`
- `src/modern/java/com/lulan/shincolle/teitoku/ShipWorldCacheEntry.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileMoveType.java`
- `src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java`
- `src/modern/java/com/lulan/shincolle/network/CombatFxDispatcher.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Resource entry points

- `src/modern/resources/assets/shincolle/`
- `src/modern/resources/data/shincolle/`
- `build.gradle` resource-copy and verification tasks.

## Legacy references

既有 SHIP-01 至 SHIP-09 协议和批次证据是逐舰 legacy 来源的索引；本批只在发现未解释缺口时读取对应旧版入口。共享边界优先使用：

- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/EntityShip.java`
- `src/main/resources/`

## Acceptance assertions

- [ ] M0-M9 每个单人主线 track 都有代码/资源入口、回归断言和批次证据。
- [ ] 矩阵所有主线行没有未解释的 `PARTIAL` 或 `NEXT`；parity backlog 单独列出。
- [ ] `git diff --check` 通过，且 `src/main/java`、`src/main/resources` 无改动。
- [ ] Java 17 下 `compileJava`、`processResources`、必要资源验证和 `runGameTestServer` 通过。
- [ ] V0 真实进世界流程逐项记录；任何未执行项目保持未完成状态。
- [ ] 完成前不提交、推送或关机；完成后仍遵守用户是否明确要求这些操作的约束。

## Frozen non-goals

- 不借助测试数量、矩阵 `LOCKED` 或客户端启动成功替代真实 V0 进世界验收。
- 不为补齐文档而重新读取全部 legacy/modern 文件；只沿本协议和已有批次索引定向读取。
- 不把无法复核的旧版推测写成完成结论。

## Scope amendment: concrete ship override inventory

Before reading legacy concrete ship class bodies, this batch also freezes a
read-only, targeted inventory of the 45 non-Mob concrete ship classes under
`src/main/java/com/lulan/shincolle/entity/{battleship,carrier,cruiser,destroyer,hime,submarine,transport}/`. The inventory will capture
only class declarations, override/method signatures, and the minimum bodies
needed to resolve a unique behavior entry. It will be compared with the
modern `ShipEntitySpecs`, `LegacyShipBehaviorCatalog`, and
`LegacyShipAttackProfile` entry points. This is an audit of semantic coverage,
not permission to edit legacy files and not a request to recreate a second
dispatch layer.

The audit result must be recorded in this batch's `decision.md` and
`results/`, including any no-change conclusion and any concrete modern gap
found. If a gap is found, implementation remains limited to the modern side
and is tested as one batch after all targeted comparisons are complete.
