# SHIP-09 Decision

状态：`COMPLETE (SP mainline boundary)`

本文件记录五个选定舰种与 1.12 语义的逐域结论。结论只覆盖本批冻结范围；未覆盖的行为不得写成已完成。

| ID | 舰种 | runtime/save | attack/cadence/resource | target/rider boundary | render/feedback |
| ---: | --- | --- | --- | --- | --- |
| 60 | Kongou | MATCHED: shared BB runtime/save and legacy dimensions | MATCHED: no-air BB channels, type-18 effect, heavy missile profile and hostile cannon gate | MATCHED: legacy BB mount offsets and shared target path | MATCHED: `ModelBBKongou` `showEquip`, source parser/bake, friendly and hostile smoke points |
| 61 | Hiei | MATCHED: shared BB runtime/save and legacy dimensions | MATCHED: no-air BB channels, type-19 saturation effect, heavy missile profile and hostile cannon gate | MATCHED: legacy BB mount offsets and shared target path | MATCHED: `ModelBBHiei` `showEquip`, source parser/bake, corrected hostile chimney point |
| 62 | Haruna | MATCHED: shared BB runtime/save and legacy dimensions | MATCHED: no-air BB channels, type-27 luck effect, heavy missile profile and hostile cannon gate | MATCHED: legacy BB mount offsets and shared target path | MATCHED: `ModelBBHaruna` `showEquip`, source parser/bake, hostile smoke point |
| 63 | Kirishima | MATCHED: shared BB runtime/save and legacy dimensions | MATCHED: no-air BB channels, type-15 night-vision effect, `1.5x` melee multiplier and hostile cannon gate | MATCHED: legacy BB mount offsets and shared target path | MATCHED: `ModelBBKirishima` `showEquip`, source parser/bake, hostile smoke point |
| 72 | Submarine Hime New / SSNH | MATCHED: depth `1`, morale cadence and shared runtime fields | MATCHED: light dual missile, ammo/grudge/morale costs, miss scatter and projectile hook | MATCHED: fuel gate, pending target, 32-tick path cadence, 200-tick timeout, owner/passenger/host boundaries | MATCHED: `ModelSSNH` source/state/parser/bake and dual-missile feedback path |

## No-change audit

- 四个 Kongou-family 普通攻击没有旧版专属自动 special hook；保留共享 ranged/heavy 路径，combo 仍只由既有 morph hook 触发。
- `LegacyShipEntity.performCatalogSpecialAttack` 的 `justLaunch()` 分支已经把 `roll.miss()` 传入 `LegacyShipProjectileEntity`，并在直接伤害前保留 projectile-style miss；`catalogHeavyMissesRemainProjectileOnly` 回归确认 miss 不造成直接伤害且投射物带 missed 标记。本批未重复实现第二套 miss 分派。
- 敌对 Boss 远距离追击和玩家起飞/命中提示属于冻结的批次外范围；本批选定实体对照未证明存在直接错误调用，因此保持现状。
- `verifyItemModelCoverage` 未触发：本批修改的是 legacy model state/parser/bake 路径，不是 item/model registry。

## Follow-up

- 本批实际覆盖域已在矩阵锁定；SHIP-09 之后不再外推到其他舰种。
- 下一入口为 `SP-CLOSE`：只审计 Phase 5-10 单人主线收口与非目标 backlog，不在本批次内提前实现。
