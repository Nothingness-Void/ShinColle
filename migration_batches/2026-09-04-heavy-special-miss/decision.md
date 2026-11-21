# Decision

## Result

**已撤销**：此前以发射时保留 miss 标记、没有直接扣血来认定 `NO CODE CHANGE REQUIRED` 的完整一致性结论。以下保留历史检查范围；其未覆盖撞击后伤害，不能证明投射物 miss 语义一致。更正见 [D-006](../../MIGRATION-DECISIONS.md#d-006投射物-miss-语义不可丢失)。

- 旧版 `BasicEntityShipHostile.attackEntityWithHeavyAmmo` 先按距离执行 miss roll，miss 时只散布投射物目标点，随后生成 `EntityAbyssMissile`，不直接对目标调用伤害。
- 现代 `LegacyShipEntity.performCatalogSpecialAttack` 在 `justLaunch` 分支保留 `roll.miss()`，把它传入 `launchCompatProjectile`；直接 `hurt` 分支只在非 `justLaunch` 路径执行。
- 现代通用攻击入口同样把 `justLaunch` 或 projectile profile 导向投射物，不把非零 roll damage 直接施加到目标。

上述历史检查仅证明发射阶段没有直接对目标扣血，不再作为“不应追加修复”的依据。

## Existing regression evidence

- `heavyProjectileMissesRemainProjectileOnly`：强制确定性 miss，断言目标生命不变、投射物 `isMissedShot()` 为真。
- `catalogHeavyMissesRemainProjectileOnly`：覆盖 catalog 特殊重攻击，断言目标生命不变、投射物标记 miss、冷却生效且重弹药消耗。
- 最近批次回归结果：`All 266 required tests passed`。

## Evidence

- [protocol.md](protocol.md)
- [STATUS.md](STATUS.md)
- [results/heavy-special-miss.json](results/heavy-special-miss.json)
- [results/](results/)
