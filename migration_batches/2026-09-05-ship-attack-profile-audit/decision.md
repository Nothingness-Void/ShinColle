# Batch Decision

## Result

`LegacyShipAttackProfile` 已覆盖活动注册集合的有效攻击通道。此次对照没有发现需要新增现代生产行为的差异，批次结论为 `LOCKED / no change`（保留必要的语义注释和回归测试）。

## Legacy-to-modern mapping

| 范围 | 旧版事实 | modern 处理 |
| --- | --- | --- |
| 普通 `BasicEntityShip` 默认值 | `ShipStateHandler.initFirst()` 默认打开轻攻、重攻、空袭四个开关；非 CV 类再由构造函数关闭航空通道 | `LegacyShipAttackProfile` 以显式 class stem 和 CV 继承语义解析，不用 hostile 标志替代旧版继承关系 |
| friendly 轻攻关闭 | `EntityCarrierWo`、`EntityTransportWa`、`EntityCarrierHime`、`EntityCarrierKaga`、`EntityCarrierAkagi` | profile 的 light channel 保持关闭 |
| friendly 重攻关闭 | 上述五类，另有 `EntityCarrierWDemon` 和 `EntityBattleshipTa` | profile 的 heavy channel 保持关闭 |
| friendly 航空通道 | `EntityCarrierWo`、`EntityBattleshipRe`、`EntityCarrierHime`、`EntityAirfieldHime`、`EntityHarbourHime`、`EntityIsolatedHime`、`EntityMidwayHime`、`EntityNorthernHime`、`EntityCarrierWDemon`、`EntityCarrierKaga`、`EntityCarrierAkagi` | profile 的 air light/heavy 使用同一旧版 CV/航空 class 集合 |
| hostile Mob 变体 | `BasicEntityShipHostile.getAttackType(int)` 对轻、重、空袭查询均返回 true；航空仍受 hostile CV 基类影响 | 19 个 `+2000` Mob 规格启用 light/heavy，并按 friendly 对应 class 保留 air flags |
| Hime 风格 hostile 变体 | 不是 Mob base 的替换构造函数，继续保留原始 Hime class 的开关 | 不用统一 hostile 默认覆盖原始 class profile |
| `EntityBattleshipTa` | raw heavy 位继承默认值，但具体旧版实体只有 `attackEntityWithAmmo` 的 Rensouhou 轻攻击实现，没有 heavy entry | effective profile 维持 `light=true, heavy=false, air=false`，避免现代 generic heavy path 发明旧版不存在的行为 |

## Special hook boundary

`LegacyShipBehaviorCatalog.shouldUseCombatHook` 的特殊入口只在对应旧版攻击入口存在且 profile 通道可用时启用。全注册 GameTest 对每个基础规格遍历 `LegacyShipAttackKind`，拒绝“hook 已启用但 profile channel 被关闭”的组合；`Battleship Ta` 的 heavy hook 保持关闭。

特殊重攻击的 `canMiss + justLaunch` 仍走现有 projectile 兼容路径，miss 不转化为直接命中；该语义由既有重攻击回归覆盖，本批只验证 profile 没有重新打开错误通道。

## Scope note

本批覆盖 protocol 冻结的活动注册集合。旧版实际源码与注册入口是 `EntityCARi.java`（由 `init/ModEntity.java` 以 `CARI` 注册）；`ID.java` 中的 `EntityHeavyCruiserRi` 是名称映射，现代规格沿用该规范化 stem。它属于本批活动集合与矩阵 ID 9，不作为来源缺口处理。

## Evidence

- 机器可读结果：[attack-profile-audit.json](results/attack-profile-audit.json)
- 新增回归：[GameplayParityGameTests.java](../../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java)
- 旧版 light/heavy/air profile：[LegacyShipAttackProfile.java](../../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java)
- 特殊 hook boundary：[LegacyShipBehaviorCatalog.java](../../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java)
