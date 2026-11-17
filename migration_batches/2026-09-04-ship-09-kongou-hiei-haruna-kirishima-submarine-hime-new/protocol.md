# Frozen Protocol

本协议在读取 SHIP-09 实现源码前冻结。只处理矩阵当前 `NEXT` 的 class `60`、`61`、`62`、`63`、`72`，结论不得外推到其他舰种。

## Legacy references

只列出为回答本批次问题必须读取的旧版文件、类、方法或资源路径：

- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBKongou.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBKongouMob.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBHiei.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBHieiMob.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBHaruna.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBHarunaMob.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBKirishima.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBKirishimaMob.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySSNH.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySubmHime.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelBBKongou.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelBBHiei.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelBBHaruna.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelBBKirishima.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelSSNH.java`
- `src/main/java/com/lulan/shincolle/client/model/ModelSubmHime.java`

## Modern references

只列出预计修改或验证的现代文件：

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStats.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipModelStateCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipModelTransformCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileMoveType.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/mount/LegacyShipMountEntity.java`
- `src/modern/java/com/lulan/shincolle/client/model/entity/LegacyShipModel.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyModelSourceParser.java`
- `src/modern/java/com/lulan/shincolle/client/model/legacy/LegacyStaticModel.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [ ] 四艘金刚型友方/敌对实体的 combo、owner aura、cannon family、目标有效性、cadence、miss/crit、资源消耗和 runtime/save 与对应旧版入口一致，或记录最小 API 适配。
- [ ] SSNH/新潜艇姬的 dual missile、depth path、morale、riding、目标/宿主边界与旧版入口一致，或记录最小 API 适配。
- [ ] 选中实体的旧版模型 source、texture lookup、`showEquip` 状态和 parser/bake 路径有断言，不静默 fallback。
- [ ] 若选中攻击经过共享 `heavy + canMiss` 路径，miss 必须在直接伤害前保留旧版 projectile-style 语义，并有回归断言。
- [ ] `git diff --check` 通过，且 `src/main/java`、`src/main/resources` 无改动。
- [ ] 按 [MIGRATION-TEST-MATRIX.md](../../MIGRATION-TEST-MATRIX.md) 运行本批次所需检查；客户端改动只在整批结束后烟测一次。

## Frozen non-goals

- 不把既有 `LegacyShipEntity` 生命周期协调改造成新的实体架构；逐舰分派仍进入 catalog 或同级 helper。
- 不读取或修改未列出的舰种实现来“顺手”扩大批次；发现联动缺口时只记录到下一批或 parity backlog。
- 不改变敌对 Boss 的远距离寻敌、追击或玩家通知策略，除非旧版对照证明 SHIP-09 直接调用错误。
