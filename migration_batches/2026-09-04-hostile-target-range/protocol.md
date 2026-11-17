# Frozen Protocol

## Legacy references

只读取以下旧版入口：

- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeTarget.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRevengeTarget.java`
- `src/main/java/com/lulan/shincolle/utility/TargetHelper.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTenryuuMob.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTatsutaMob.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTenryuu.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTatsuta.java`
- 目标扫描周期、AABB/攻击距离、目标有效性、创造/无敌/隐身筛选，以及敌方起飞/命中反馈分支。
- 追加读取基类仅用于核对 `FollowMax` 的默认值和旧版低攻击距离回退规则。

## Modern references

只读取以下现代入口，必要时只修改这些文件：

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangeTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipHostilePlayerTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtByTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangedAttackGoal.java`
- `src/modern/java/com/lulan/shincolle/network/ClientboundCombatReactPacket.java`
- `src/modern/java/com/lulan/shincolle/network/CombatFxDispatcher.java`
- `src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [ ] hostile target扫描周期、范围和AABB与旧版入口一致。
- [ ] 创造、无敌、观察者、隐身、死亡、越界和 hostile ship 目标边界与旧版一致；已有目标失效后必须停止追击/攻击。
- [ ] 敌方飞机、投射物和命中反应不得产生不应显示给玩家的 actionbar 提示。
- [ ] 若发现差异，只做旧版语义所需的最小现代 API 适配，并追加确定性 GameTest。
- [ ] `git diff --check` 通过，且 legacy Java/resources 无差异。
- [ ] 有代码变更时，批次完成后运行 `compileJava`、`processResources`、`runGameTestServer`；无代码变更时记录现有回归证据，不重复长测试。

## Frozen non-goals

- 不以用户截图为依据猜测距离常量；所有修改必须先绑定旧版实现。
- 不为了让 Boss 更容易被玩家观察而放宽旧版目标筛选。
- 不把静态匹配结论写成已完成实机体验；V0 仍需真实客户端操作。
