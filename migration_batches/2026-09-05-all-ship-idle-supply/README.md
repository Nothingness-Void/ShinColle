# ALL-SHIP-IDLE-SUPPLY

本批次只处理活动 45 个 `BasicEntityShip` 注册类共享的旧版待机/移动资源消耗入口。

## 问题

现代 `LegacyShipEntity` 的 128 tick 资源入口由 `LegacyShipBehaviorCatalog.legacyIdleGrudgeConsumptionFor` gate；前序切片只有 9 个 class ID 能进入，其他已注册类不会执行旧版公共 `BasicEntityShip.updateConsumeItem()` 逻辑。旧版 Hime 类继承 `BasicEntityShipSmall/CV`，不能因为现代规格的 `hostile` 标志而排除；真正不进入公共路径的是独立的 `BasicEntityShipHostile` Mob 变体。

## 目标

以旧版公共实现为语义真源，把所有活动 `BasicEntityShip` class ID 接入同一现代入口；不为每个舰种复制一套逻辑，也不把 `BasicEntityShipHostile` Mob 变体误接入。

## 记录

- [protocol.md](protocol.md)
- [STATUS.md](STATUS.md)
- [decision.md](decision.md)
- [results](results/)

## 结果

- 45 个活动 `BasicEntityShip` class 的 idle cost 已进入同一 catalog 路径，Hime 类按旧版继承关系保留公共资源消耗。
- 19 个 hostile Mob 变体继续排除；Transport Wa 的移动经验分支已补回。
- 本批新增回归通过；整套 269 个 GameTest 中 265 个通过，4 个既有战斗回归另立后续批次处理。详见 [results/all-ship-idle-supply.json](results/all-ship-idle-supply.json)。
