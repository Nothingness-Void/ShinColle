# 战斗与投射物一致性修复

## 冻结范围

- 目标：复核并恢复用户指定的七项 1.12.2 战斗语义；不修改旧版源码。后续按用户要求提交并推送本专题分支。
- 旧版入口：`BasicEntityShipHostile.attackEntityWithHeavyAmmo`、`BasicEntityAirplane.attackEntityWithHeavyAmmo`、`EntityAbyssMissile.onImpact`、`CombatHelper.calcMissRate/getAttackDelay/flareTarget`、`AttackHandler`、`BuffHelper.getPotionDamage`、`EquipCalc`。
- 现代入口：`LegacyShipEntity`、`LegacyShipCombatHelper`、`LegacyShipProjectileEntity`、`LegacyShipAircraftEntity`、`ShipEquipmentBehaviorState` 及派生加成调用者。
- 验收：miss 只偏移目标点、不禁止碰撞伤害；装备不产生额外命中/暴击/冷却/制导倍率；隐身无额外 miss；照明不附加发光效果；有主人的舰船/召唤物/坐骑伤害仅受击端缩放一次；药水按最大生命与等级计算并跳过 DEF；创造模式不绕过友伤开关。
- 非目标：其他审计项、Boss 系统、AI 系统与客户端渲染重构。
- 检查方法见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#舰娘核心回归)，输出和限制见 [验证结果](STATUS.md)。

## 结论入口

单批次复核结论与例外统一记录在 [decision.md](decision.md)；跨批次投射物契约统一记录在 [D-006](../../MIGRATION-DECISIONS.md#d-006投射物-miss-语义不可丢失)。

## 检查过程

- 初次编译通过；首次全量 GameTest 为 301/302，新增药水断言失败，原因是测试直接改 MAX_HEALTH 后，受伤扣士气会按舰船真实属性刷新生命上限。
- 第二次测试仍为同一药水断言失败：人工设为 1000 的上限在受伤管线刷新后降回低级舰船真实上限，最高级药水将其击杀；测试改用 150 级舰船的真实最大生命代入公式，不再注入临时最大生命属性。
- 验证限制统一见 [STATUS.md](STATUS.md#未验证)。
