# SHIP-03 Decision Record

## Decision

`CHANGED`

## Evidence rule

Record each audited behavior as one of:

- `MATCHED`: modern behavior matches the legacy semantic rule with only a documented MC API adaptation.
- `CHANGED`: a minimal modern-side correction was required and is listed with focused test evidence.
- `BLOCKED`: the comparison cannot be completed without an external decision or a file outside the frozen set.

An exact match must still be recorded so a later batch does not reread the same code.

## Per-ship record

| ID | Ship | Attack/selection | Cadence/result | Resource/recovery | Drop/runtime | Result |
| ---: | --- | --- | --- | --- | --- | --- |
| 16 | `EntityTransportWa` | `MATCHED`: melee-only; no light/heavy/air channel; legacy pickup remains enabled at range 8 | `MATCHED`: no attack override; shared cadence remains the applicable path | `CHANGED`: AP idle cost `3` now reaches the shared runtime tick; timed cargo use and same-owner supply loop match; legacy `easyMode` multiplier is `BLOCKED` because no modern setting exists | `MATCHED`: shared death/recovered-egg and runtime save path | `CHANGED` |
| 17 | `EntitySubmKa` | `MATCHED`: light/heavy range channels, no air channel, pickup range 4; no custom cannon override in legacy source | `MATCHED`: shared range-AI delay, target validity, and miss/crit calculation are retained | `CHANGED`: SS idle cost `4` now reaches the shared runtime tick; ammo/grudge attack accounting remains shared legacy accounting | `MATCHED`: shared death/recovered-egg and runtime save path | `CHANGED` |
| 18 | `EntitySubmYo` | `MATCHED`: light/heavy range channels, no air channel, pickup range 4, and dedicated light missile profile | `MATCHED`: legacy light missile launch, target validity, and projectile-style result path are retained | `CHANGED`: SS idle cost `4` now reaches the shared runtime tick; light missile resource accounting remains the legacy base cost | `MATCHED`: shared death/recovered-egg and runtime save path | `CHANGED` |
| 19 | `EntitySubmSo` | `MATCHED`: light/heavy range channels, no air channel, pickup range 4; no custom cannon override in legacy source | `MATCHED`: shared range-AI delay, target validity, and miss/crit calculation are retained | `CHANGED`: SS idle cost `4` now reaches the shared runtime tick; ammo/grudge attack accounting remains shared legacy accounting | `MATCHED`: shared death/recovered-egg and runtime save path | `CHANGED` |
| 20 | `EntityCarrierHime` | `MATCHED`: melee missile plus air channels; no light/heavy cannon channel; legacy mount and aura hooks remain | `CHANGED`: melee missile hook and finite aircraft/capacity path are retained; aircraft launch height corrected to `0.9F` | `CHANGED`: finite aircraft inventory/recovery remains active and launch height is corrected; exact old friendly-CV ammo/grudge/fuel lifecycle is `BLOCKED` by the legacy friendly-base/modern hostile-spec model split | `MATCHED`: princess boss profile, shared hostile loot, death/recovered-egg and runtime save paths | `CHANGED` |

## Scope decisions

- `SHIP-02` command/route/target boundaries are reused and not reopened.
- The hostile Boss far-chase report remains a separate reproduction batch.
- The old Carrier Hime heavy-special miss finding does not reproduce in the current selected path: projectile-style attacks carry `missedShot` into the projectile entity, where a miss cannot hit or resolve damage.
- The generic heavy-special miss rule is in scope only where one of these five legacy attack paths reaches it; otherwise record the finding as a follow-up without changing unrelated behavior.
- Any direct legacy helper dependency discovered during method-level comparison must be added here and to `protocol.md` before it is read.

## Code changes

- [LegacyShipBehaviorCatalog.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java) adds the class `16` AP and classes `17`-`19` SS idle-grudge mappings and the class `20` `0.9F` aircraft launch-height factor.
- [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) adds `ship03TransportSubmarinesAndCarrierHimeKeepLegacyCombatBoundaries`.

## Evidence

- [ship-03.json](results/ship-03.json)
- Final T0/T1/T2 verification: `git diff --check`, `compileJava`, `processResources`, and `runGameTestServer` passed.

## No-change audits

- No GUI, network protocol, route command, renderer, resource, particle, or client behavior was changed.
- No `LegacyShipEntity` class-id dispatch was added; the two corrections stay in the existing catalog entry points.
- `src/main/java` and `src/main/resources` remain unchanged.

## Follow-up

- Promote the next five matrix rows to `NEXT` for the visual/projectile-linked `SHIP-04` batch.
- Keep easy-mode cargo multipliers, Carrier Hime safe-block/mounted aircraft offsets, and exact friendly-CV versus hostile-spec resource parity in the full parity backlog or a dedicated later decision batch.
