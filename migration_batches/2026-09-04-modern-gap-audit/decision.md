# Decision

## Finding

静态扫描发现 `LegacyShipEntity.tickLegacyMovementAndIdleConsumption()` 通过 catalog 的负值 gate 只覆盖前序 9 个 class ID，导致其余活动注册类不会执行旧版 `BasicEntityShip.updateConsumeItem()`。现代 `ShipEntitySpec.hostile` 不能直接作为公共路径 gate，因为旧版 Hime 类继承 `BasicEntityShipSmall/CV`，只有独立 `BasicEntityShipHostile` Mob 变体应排除。

## Follow-up

该缺口已冻结为 [ALL-SHIP-IDLE-SUPPLY](../2026-09-05-all-ship-idle-supply/)，完成了 45 个活动 `BasicEntityShip` class 的旧版 idle cost、Hime 公共路径、hostile Mob 排除和 Transport Wa 经验分支。全局剩余 4 个战斗测试失败不属于本缺口，转入独立战斗回归批次。

后续 `COMBAT-REGRESSION-SUITE` 已完成旧版入口对照并修正测试夹具/断言；全局 269 个 required GameTest 已全部通过。因此本批的具体 finding 已关闭，不再重复读取或运行 idle-supply 审计。

## Evidence

- [protocol.md](protocol.md)
- [results/modern-gap-audit.json](results/modern-gap-audit.json)
- [ALL-SHIP-IDLE-SUPPLY follow-up](../2026-09-05-all-ship-idle-supply/)
