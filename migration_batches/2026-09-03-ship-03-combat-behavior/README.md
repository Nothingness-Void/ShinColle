# Batch: 2026-09-03-ship-03-combat-behavior

## Status

`COMPLETE`

## Question

运输舰 Wa、潜艇 Ka/Yo/So 与航母姬的单人主线攻击、战斗节奏、miss/crit、资源消耗、召唤/恢复和掉落行为，是否已经按 1.12 语义迁移到现代公共实体入口？

## Result

- `LegacyShipBehaviorCatalog` now maps Transport Wa's AP idle cost (`3`) and Submarine Ka/Yo/So's SS idle cost (`4`) into the shared modern runtime tick.
- `LegacyShipBehaviorCatalog.aircraftLaunchHeightFactorFor` now preserves Carrier Hime's legacy `height * 0.9F` launch height for both aircraft channels.
- The five selected rows retain their legacy attack-channel, pickup, ammunition, submarine Yo missile, finite-aircraft, and capacity boundaries.
- The shared projectile path already preserves projectile-style heavy misses by carrying `missedShot` into the projectile entity; no unrelated direct-damage rewrite was made.

## Evidence

- Machine-readable verification: [ship-03.json](results/ship-03.json)
- Final regression: `All 254 required tests passed`
- Client smoke: not applicable; this batch changed only shared server-side catalog behavior and GameTest code.

## Scope

- class ID：`16`、`17`、`18`、`19`、`20`
- 行为族：`transport`、`submarine`、`hostile carrier hime`
- 模块：M2、M4、M5、M6、M7
- 目标：只补齐本五行单人可到达的行为、攻击、冷却、miss/crit、资源和掉落缺口。

## Out of scope

- GUI、网络协议、路线命令、模型、renderer、screen 和资源路径；这些必须由各自批次负责。
- 未列入本批的舰种行为，不从共享实现推断为已完成。
- 敌对 Boss 过远追击策略和完整多人权限。
- 完整 1.12 parity、全部 inter-mod 和非主线历史兼容。

## Follow-up boundaries

- The legacy `easyMode` cargo-consumption multiplier has no matching modern setting in the frozen source set; it remains a parity backlog item.
- Legacy Carrier Hime inherits the friendly `BasicEntityShipCV` resource lifecycle while the modern spec is hostile. The batch keeps the existing hostile runtime split and records exact cross-model resource parity as a decision item rather than silently changing all hostile ships.
- Carrier safe-block launch fallback and mounted launch-height adjustment remain shared aircraft follow-up work.

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
