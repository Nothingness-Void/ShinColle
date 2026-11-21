# SHIP-04 Decision Record

## Decision

`COMPLETE (SP visual/projectile boundary)`

## Evidence rule

Record each audited behavior as one of:

- `MATCHED`: modern behavior matches the legacy semantic rule with only a documented MC API adaptation.
- `CHANGED`: a minimal modern-side correction was required and is listed with focused test evidence.
- `BLOCKED`: the comparison cannot be completed without an external decision or a file outside the frozen set.

An exact match must still be recorded so a later batch does not reread the same code.

## Per-ship record

| ID | Ship | Model source/bake | Attack/projectile family | Feedback/render boundary | Result |
| ---: | --- | --- | --- | --- | --- |
| 21 | `EntityAirfieldHime` | `CHANGED`: exact legacy source/bake and state bit 1 | `CHANGED`: AIRPLANE/BOMB selection and legacy `0.7F` launch height | `MATCHED`: existing combat/reaction/particle bus; startup smoke clean | `CHANGED` |
| 26 | `EntityBattleshipHime` | `MATCHED`: exact legacy source/bake; no equipment bits | `MATCHED`: type-3 cluster heavy missile family | `MATCHED`: cluster impact remains on existing projectile/feedback path | `MATCHED` |
| 27 | `EntityDestroyerHime` | `CHANGED`: exact legacy source/bake and state bits 0-5 | `MATCHED`: inherited range/melee selection and blindness attack effect | `MATCHED`: existing reaction path; startup smoke clean | `CHANGED` |
| 28 | `EntityHarbourHime` | `MATCHED`: exact legacy source/bake; no equipment bits | `CHANGED`: direct type-2 railgun family, `3.0` speed, `0.7F` aircraft height, appended railgun particle | `CHANGED`: railgun trail/head feedback is isolated as `LEGACY_RAILGUN` | `CHANGED` |
| 29 | `EntityIsolatedHime` | `CHANGED`: exact legacy source/bake and state bit 6 inverse leg rule | `MATCHED`: type-5 black-hole/static effect family | `MATCHED`: black-hole effect remains on static-effect and particle bus | `CHANGED` |

## Scope decisions

- SHIP-02 command/route/target and SHIP-03 combat/resource mappings are reused and not reopened.
- The hostile Boss far-chase and hostile player-notification report remain separate target/feedback reproduction work unless a selected visual path directly disproves the existing audit.
- A model or resource path is changed only after the frozen old model/renderer source and the modern lookup are both read.
- Any direct legacy projectile/model helper discovered during comparison must be added here and to `protocol.md` before it is read.

## Batch conclusion

- The five selected rows now have explicit model source, projectile family, feedback boundary, and focused regression coverage.

> **已撤销（见 D-016）**：本条旧解析模型/目录作为完整动画、死亡、情绪和发光等价实现的口径已撤销；原数值/部件审计仅留作历史证据。替代实现与边界见 [客户端还原决定](../2026-11-22-parity-fix-client/decision.md)。

- `LegacyShipModelStateCatalog` reproduces the audited 1.12 `showEquip` visibility bits; unknown parts remain visible, matching the old model default.
- `LEGACY_RAILGUN` is appended to the particle enum so existing ordinal values remain stable. It is the minimum modern visual adaptation for the old type-2 railgun-head particle call.
- The client evidence proves startup and resource reload. In-world manual interaction was not available through the current desktop automation surface and is recorded as a V0 follow-up, not as an unobserved pass.
