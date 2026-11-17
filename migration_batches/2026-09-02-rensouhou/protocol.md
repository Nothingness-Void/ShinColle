# Frozen Protocol

## Legacy references

- Rensouhou 的宿主状态到模型变体的映射。
- 旧版视线/遮挡判断和追击导航节奏。
- 旧版攻击间隔和目标有效性规则。

## Modern references

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- 对应 GameTest 行为断言。

## Acceptance assertions

- [x] Ta 反向模型状态规则与旧版宿主状态一致。
- [x] 被遮挡时不直接攻击，导航节奏保持旧版边界。
- [x] 攻击间隔由集中 helper 提供，并保持旧版结果。
- [x] `git diff --check`、构建、资源处理和 GameTest 通过。

## Frozen non-goals

- 不把 Rensouhou 分派逻辑重新塞回公共实体。
- 不借此批次修改其他舰种。
