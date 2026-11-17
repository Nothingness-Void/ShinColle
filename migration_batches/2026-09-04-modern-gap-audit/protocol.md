# Frozen Protocol

## Legacy references

- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipSmall.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostileCV.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/`
- `src/main/java/com/lulan/shincolle/entity/carrier/`
- `src/main/java/com/lulan/shincolle/entity/cruiser/`
- `src/main/java/com/lulan/shincolle/entity/destroyer/`
- `src/main/java/com/lulan/shincolle/entity/hime/`
- `src/main/java/com/lulan/shincolle/entity/submarine/`
- `src/main/java/com/lulan/shincolle/entity/transport/`

## Modern references

- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipStatTables.java`
- `MIGRATION-SHIP-MATRIX.md`

## Acceptance assertions

- [ ] 每个活动基础 class ID 都有现代规格、攻击档案和行为入口。
- [ ] 现代活动舰种入口没有明确的 TODO、UnsupportedOperation、空占位分派或静默 fallback；疑点必须列出文件和行号。
- [ ] 旧版声明的 per-ship override 与现代 catalog/profile 的覆盖关系可逐项说明。
- [ ] 若发现真实缺口，创建后续最小实现批次；若无缺口，记录 `COMPLETED_NO_CHANGE`。
- [ ] `git diff --check` 通过。
- [ ] 本批不运行客户端、编译或 GameTest；没有代码变更时不重复自动化回归。

## Frozen non-goals

- 不把通用实现误判为缺口，也不以名称相似代替旧版语义证据。
- 不恢复没有旧版实体源文件的历史 ID-only 名称。
- 不用一次性重构替代逐舰/行为族的最小迁移。
