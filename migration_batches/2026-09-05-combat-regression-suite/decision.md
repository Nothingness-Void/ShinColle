# Decision Record

## Result

结论：四个失败都不是现代生产战斗逻辑缺失，不需要新增战斗架构或舰种特判。本批只修正了测试夹具与错误断言，生产代码保持旧版语义。

### Nagato

- 旧版入口：`src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipNGT.java` 的 128 tick morale event；目标搜索使用 `getEntitiesWithinAABB(LivingBase.class, 16D, 12D, 16D)`，目标为 Northern Hime 或 `ShipType.DESTROYER`，低于 `5100 * 1.5F = 7650` 时每个目标增加 150。
- 现代入口：`LegacyShipBehaviorCatalog.tickNagatoMoraleEvent`，保持同一范围、筛选、阈值和增量。
- 失败原因：GameTest 的默认结构并行运行时，原测试点 y=100 的旧版范围内混入了其他测试实体，实际触发了 8 个额外匹配目标；把生产范围缩小会破坏旧版语义。测试夹具改为同一加载区块的 y=200 隔离点，保留旧版 16/12/16 查询。
- 旧版顺序：`BasicEntityShip.onLivingUpdate()` 先执行公共 `updateConsumeItem()`，再进入具体舰种 128 tick 事件；预期士气修正包含一次固定 5 点 idle morale cost。

### Tenryuu and Tatsuta

- 旧版入口：`EntityCLTenryuu.attackEntityWithHeavyAmmo` 与 `EntityCLTatsuta.attackEntityWithHeavyAmmo`。两者首次调用进入 phase 0 的准备阶段，下一次调用进入 phase -1 的技能阶段；技能剩余次数分别为 `3 + int(level * 0.03F)` 与 `2 + int(level * 0.015F)`，每次调用都先执行旧版弹药、怨念和士气消耗。
- 现代入口：`LegacyShipBehaviorCatalog.performTenryuuDash` 与 `performTatsutaControl`，阶段和 level 缩放保持对应旧版入口。
- 失败原因：测试用例在冷却循环中直接调用实体 `tick()`；该调用不会经过 `ServerLevel` 的外层 tickCount 递增，因此新接入的公共 128 tick 资源入口被重复视为边界 tick，提前耗尽资源。测试在首次攻击后把 `tickCount` 置为非边界值，模拟实际服务器冷却期间的 tick 序列；未加入生产规避逻辑。

### Yamato

- 旧版入口：`EntityBattleshipYMT.attackEntityWithHeavyAmmo`。phase <= 0 时扣资源并进入 phase 1；phase > 0 时创建 beam、扣资源并清回 phase 0，beam 负责后续命中。
- 现代入口：`LegacyShipBehaviorCatalog.performYamatoBeam`，保持两阶段状态、资源门、beam 创建和 phase 清理。
- 失败原因：与 Tenryuu/Tatsuta 相同，是 GameTest 直接 tick 的固定 tickCount 夹具造成公共 128 tick 消耗重复执行；测试改为非边界 tick 后恢复旧版两阶段断言。

### Submarine Hime projectile assertion

- 旧版入口：`src/main/java/com/lulan/shincolle/entity/hime/EntitySubmHime.java` 的 `attackEntityWithAmmo/summonMissile`，以及 `src/main/java/com/lulan/shincolle/entity/other/EntityAbyssMissile.java` 构造函数。深度选择先给出 ARC；实体构造随后在总距离小于 4 格时改为 DIRECT，在水平距离不超过 4 格时保留 moveType=ARC 但将加速度置零，在弧线加速度超过 0.15 时才改为 DIRECT。miss 还会替换目标点为随机散射位置。
- 现代入口：`LegacyShipBehaviorCatalog.performSubmarineHimeDoubleMissile` 与 `LegacyShipProjectileEntity.configureLegacyArcMotion`，已实现同一距离、加速度和 miss 目标点语义。
- 失败原因：测试把所有随机 miss 的 projectile 都硬编码期望为 DIRECT，忽略了旧版“ARC 类型编号 + 直线运动”的短水平距离分支。测试改为调用按旧版公式计算的 `expectedLegacyHimeMissileMove`，不改变 projectile 实现。

## Verification

- `git diff --check`：通过。
- `compileJava`：通过。
- `processResources`：通过。
- `runGameTestServer`：通过，`269/269 required tests passed`。

## Evidence

- [protocol.md](protocol.md)
- [results/combat-regression-suite.json](results/combat-regression-suite.json)
- [results](results/)
