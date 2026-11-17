# Batch Decision

- 结论：`MIGRATED / PASS`
- 影响矩阵：[MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md) 的 45 个基础行继续保持 `LOCKED`，新增本批证据入口。
- 关联模块：实体注册、公共生命周期、逐舰 override、AI/目标、攻击/资源、模型渲染。

## Facts established

- 本批次在读取旧版实现前已冻结协议。
- 旧版 `ModEntity.regShipEntity` 对应 45 个基础 class ID、64 个注册变体；modern `ShipEntitySpecs` 覆盖完整，未发现未解释的 per-ship 注册遗漏。
- 旧版 `BuffHelper.applyBuffOnDamageByLight` 在舰船/召唤物受击入口执行，使用目标光照、攻击源 `getDamageType()`、受击方 `getDamageType()` 和夜视加成；现代等价入口是 `LegacyShipEntity.hurt` -> `LegacyShipCombatHelper`。
- 旧版实际运行时保留了光照表变量的 day/night 取值顺序，现代实现按旧版代码顺序迁移，没有按注释反转。
- 旧版飞机攻击源类型为 `AIRPLANE`，普通/大型 Rensouhou 分别为 `DESTROYER`/`BATTLESHIP`；现代飞机伤害 source 携带 aircraft direct entity，helper 可正确识别。

## Code changes

- `LegacyShipCombatHelper` 新增旧版 damage type 映射、光照矩阵、夜视系数和 source 识别。
- `LegacyShipEntity.hurt` 在防御修正后接入旧版光照伤害修正。
- `LegacyShipAircraftEntity` 调用携带飞机实体；`LegacyShipEntity` 保留原有调用兼容重载。
- `GameplayParityGameTests` 新增全舰种 damage type 和光照公式回归。

## No-change audits

- 45 个基础 class ID：已覆盖。
- 64 个 modern registered specs：已覆盖；hostile variant 通过 class ID 归一化路径承载。
- 未解释的逐舰 dispatch gap：无。
- 运输舰 `UNDEFINED`、`BattleshipRe`/设施类 `AVIATION`、Rensouhou 大小类型和飞机 `AIRPLANE`：已保留为显式旧版例外。

## Follow-up

- 本批差异已在同一批次完成 modern 最小修复和回归；下一步返回 `SP-CLOSE / V0 T4`，不重复遍历已收口舰种。
