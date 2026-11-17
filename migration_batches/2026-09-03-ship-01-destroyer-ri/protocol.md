# Frozen Protocol

本协议在读取实现前冻结。后续若发现问题超出范围，创建新批次，不扩大本批次。

## Legacy references

- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerI.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerRo.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerHa.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerNi.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCARi.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/handler/AttackHandler.java`
- `src/main/java/com/lulan/shincolle/crafting/EquipCalc.java`

只读取上述类中与 `onLivingUpdate`、`attackEntityWithAmmo`/攻击入口、资源消耗、冷却、目标筛选、runtime/NBT、死亡/脱战和队伍状态直接相关的方法；模型和无关 GUI 入口不在本批次读取范围。基类只读用于解释五个目标类实际继承的通用语义。

## Modern references

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStats.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java`
- `src/modern/java/com/lulan/shincolle/teitoku/ShipWorldCacheEntry.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [ ] 五个 class ID 的 light attack cadence 与 legacy 触发条件逐项对应。
- [ ] ammo/grudge/fuel 等资源扣除、资源不足和 cooldown 结果与 legacy 对应。
- [ ] friendly/hostile、同 owner、无目标和无效目标边界保持 legacy 语义。
- [ ] runtime/save 缺字段、旧字段、默认值和恢复后的战斗状态有断言。
- [ ] 新增逐舰分派只进入 catalog/helper，不向公共实体增加新的 class-id 分派。
- [ ] 新增或修改的行为有批次 GameTest；未改变的域有明确 no-change 记录。
- [ ] 批末通过 T0、T1、T2；本批无资源/客户端改动，因此不运行 T1-R/T1-M/T3。

## Frozen non-goals

- 不凭现代代码的“看起来合理”推导旧版行为。
- 不修复本批之外的 Boss、投射物、模型、客户端和其他舰种问题。
