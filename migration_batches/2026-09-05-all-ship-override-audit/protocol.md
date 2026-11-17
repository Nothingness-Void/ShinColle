# Frozen Protocol

## Legacy references

- `src/main/java/com/lulan/shincolle/init/ModEntity.java`：`regShipEntity` 全集注册入口。
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`：公共生命周期、攻击、资源和存档入口。
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`、`BasicEntityShipCV.java`、`BasicEntityShipHostile.java`、`BasicEntityShipHostileCV.java`：继承层与 friendly/hostile 边界。
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeTarget.java`、`EntityAIShipRangeAttack.java`、`EntityAIShipSkillAttack.java`、`EntityAIShipCarrierAttack.java`、`EntityAIShipAircraftAttack.java`、`EntityAIShipGuarding.java`、`EntityAIShipFollowOwner.java`、`EntityAIShipPickItem.java`、`EntityAIShipSit.java`、`EntityAIShipRevengeTarget.java`：公共 AI/目标语义。
- `src/main/java/com/lulan/shincolle/utility/TargetHelper.java`：目标有效性与玩家边界。
- `src/main/java/com/lulan/shincolle/utility/CombatHelper.java`：按攻击/受击 damage type 与光照计算伤害的旧版公式。
- `src/main/java/com/lulan/shincolle/utility/BuffHelper.java`、`src/main/java/com/lulan/shincolle/utility/EntityHelper.java`：旧版伤害修正调用链与实体移动类型判断。
- `src/main/java/com/lulan/shincolle/reference/ID.java`、`src/main/java/com/lulan/shincolle/reference/Values.java`：damage type 常量和相关旧版数值。
- `src/main/java/com/lulan/shincolle/entity/destroyer/*.java`、`cruiser/*.java`、`battleship/*.java`、`carrier/*.java`、`submarine/*.java`、`hime/*.java`：已注册基础实体和 hostile Mob 的实际 override 来源。
- `src/main/java/com/lulan/shincolle/entity/transport/EntityTransportWa.java`：运输舰的旧版 damage type 继承/覆盖来源。
- `src/main/java/com/lulan/shincolle/entity/BasicEntityAirplane.java`、`BasicEntityMount.java`、`BasicEntitySummon.java`：旧版非舰船攻击源的 damage type 接口与受击入口。
- `src/main/java/com/lulan/shincolle/entity/other/EntityAirplane.java`、`EntityRensouhou.java`、`EntityRensouhouS.java`、`EntityRensouhouMob.java`、`EntityAbyssMissile.java`、`EntityFloatingFort.java`、`EntityProjectileBeam.java`、`EntityProjectileStatic.java`：旧版飞机、召唤物和投射物实际伤害源。

## Modern references

- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStats.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipBeamEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipFloatingFortEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAirplaneEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/mount/LegacyShipMountEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/*.java`
- `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipRenderCatalog.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelSourceParser.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [ ] 旧版注册变体全集与现代 `ShipEntitySpecs` 数量、名称、friendly/hostile 边界一致。
- [ ] 每个旧版实体源文件中的 constructor/runtime/combat/AI/model 相关 override 都能定位到 modern 入口，或记录为明确 parity backlog。
- [ ] 不存在未解释的 per-ship 分派遗漏或错误默认回退。
- [ ] 若发现真实单人主线差异，先在本批次冻结后续最小修复范围，再修改 modern 代码。
- [ ] `git diff --check` 通过，legacy 源码和资源保持未修改。

## Frozen non-goals

- 不改变已经由旧版对照批次锁定的数值或目标范围。
- 不把“有默认共享行为”误判为缺失；只有旧版明确 override 且 modern 无承载才算缺口。
- 不因为客户端自动化不可用而修改生产逻辑。
