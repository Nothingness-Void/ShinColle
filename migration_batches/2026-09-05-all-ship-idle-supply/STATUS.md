# Batch Status

- 状态：`COMPLETED`
- 开始：2026-09-05
- 最近更新：2026-09-05
- 当前阻塞：无；本批变更后的全局战斗回归已由 `COMBAT-REGRESSION-SUITE` 完成。

## Completed work

- 已读取 `BasicEntityShip.updateConsumeItem()` 的精确旧版逻辑：弹药回收、移动距离、士气、移动 grudge、Transport Wa 经验、idle grudge 和 previous position 更新。
- 已确认 `BasicEntityShipSmall/CV` 及活动 Hime 类继承公共路径；`BasicEntityShipHostile` 是独立 `EntityMob` 层级，不调用该公共资源入口。
- 已将 45 个活动 `BasicEntityShip` class 的 idle cost 统一收进 catalog，并补回 Transport Wa 移动经验。
- 新增 `allActiveShipClassesKeepLegacyIdleSupplyCosts` 覆盖全部 45 个 class、Hime 公共路径、hostile Mob 排除和 Transport 经验门槛；该新增测试通过。

## Remaining work

- 后续独立战斗回归已完成；全局最终结果为 `All 269 required tests passed`。

## Evidence links

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/all-ship-idle-supply.json](results/all-ship-idle-supply.json)
