# Decision

## Result

`NO CODE CHANGE REQUIRED`。当前工作区已经保留旧版的重攻击 miss 语义：

- 旧版 `BasicEntityShipHostile.attackEntityWithHeavyAmmo` 先按距离执行 miss roll，miss 时只散布投射物目标点，随后生成 `EntityAbyssMissile`，不直接对目标调用伤害。
- 现代 `LegacyShipEntity.performCatalogSpecialAttack` 在 `justLaunch` 分支保留 `roll.miss()`，把它传入 `launchCompatProjectile`；直接 `hurt` 分支只在非 `justLaunch` 路径执行。
- 现代通用攻击入口同样把 `justLaunch` 或 projectile profile 导向投射物，不把非零 roll damage 直接施加到目标。

因此，审查项描述的“heavy special miss 被直接当成命中”在当前工作区不可复现，属于已有实现/测试覆盖的历史快照问题，不应追加重复修复。

## Existing regression evidence

- `heavyProjectileMissesRemainProjectileOnly`：强制确定性 miss，断言目标生命不变、投射物 `isMissedShot()` 为真。
- `catalogHeavyMissesRemainProjectileOnly`：覆盖 catalog 特殊重攻击，断言目标生命不变、投射物标记 miss、冷却生效且重弹药消耗。
- 最近批次回归结果：`All 266 required tests passed`。

## Evidence

- [protocol.md](protocol.md)
- [STATUS.md](STATUS.md)
- [results/heavy-special-miss.json](results/heavy-special-miss.json)
- [results/](results/)
