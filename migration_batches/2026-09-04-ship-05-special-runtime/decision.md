# SHIP-05 Decision Record

## Decision

`COMPLETE (SP special runtime boundary)`

## Evidence rule

Record each audited behavior as one of:

- `MATCHED`: modern behavior matches the legacy semantic rule with only a documented MC API adaptation.
- `CHANGED`: a minimal modern-side correction was required and is listed with focused test evidence.
- `BLOCKED`: the comparison cannot be completed without an external decision or a file outside the frozen set.

An exact match must still be recorded so a later batch does not reread the same code.

## Per-ship record

| ID | Ship | Runtime state | Attack/servant behavior | Target/cadence | Render/feedback boundary | Result |
| ---: | --- | --- | --- | --- | --- | --- |
| 30 | `EntityMidwayHime` | `MATCHED`: carrier inheritance, heal and same-owner absorption aura remain catalog-dispatched | `MATCHED`: light/heavy/air channels and recovery boundaries remain explicit | `MATCHED`: legacy target ownership and cadence gates retained | `CHANGED`: exact Midway `showEquip` state rule added; source/bake path remains legacy-backed | `CHANGED` |
| 31 | `EntityNorthernHime` | `CHANGED`: air launch height corrected to `1.0F`; FloatingFort lifecycle and dismount boundary remain matched | `MATCHED`: heavy-only FloatingFort hook and resource/cooldown path retained | `MATCHED`: riding search, dismount, fuel/model gates and delayed impact retained | `CHANGED`: exact Northern `showEquip` bits added | `CHANGED` |
| 33 | `EntityCarrierWD` | `MATCHED`: friendly carrier role, light-only heavy-disabled profile and owner aura retained | `MATCHED`: dual aircraft profile and inventory/recovery path retained | `MATCHED`: carrier target and owner boundary retained | `CHANGED`: exact Carrier WD equipment bit added | `CHANGED` |
| 36 | `EntityDestroyerShimakaze` | `MATCHED`: six-shot friendly and ten-shot hostile servant pool limits retained | `CHANGED`: hostile Rensouhou keeps projectile damage; five-shot burst and summon retirement remain catalog-owned | `CHANGED`: hostile servant does not inherit view-chase from the host; target validity remains host-gated | `CHANGED`: exact Shimakaze equipment, hair-anchor and ear state rules added | `CHANGED` |
| 37 | `EntityBattleshipNGT` | `MATCHED`: morale event, engine-state and food boundaries retained | `MATCHED`: AP phase strike, AOE and heavy-only hook retained | `MATCHED`: phase cadence and boss hostile boundary retained | `CHANGED`: exact Nagato head/equipment state rules added | `CHANGED` |

## Scope decisions

- SHIP-01 through SHIP-04 locked domains are reused and not reopened without a direct regression.
- Any direct legacy helper discovered during comparison must be added to `protocol.md` and this status record before it is read.
- A modern behavior change requires a focused assertion tied to the old method or field; shared behavior is not evidence for a special row by itself.

## Batch conclusion

- The five selected rows now have an explicit runtime, attack/servant, target/cadence, and render/feedback result.
- `LegacyShipModelStateCatalog` now covers the exact legacy `showEquip` boundaries for all five selected model sources; unknown parts remain visible as in the old model default.
- The hostile Rensouhou correction is limited to the old hostile servant path. It does not alter friendly servant damage or the shared ship combat dispatcher.
- The previously supplied heavy-special miss concern was rechecked: `LegacyShipEntity.performCatalogSpecialAttack` already carries `roll.miss()` into the launched projectile, whose impact path is non-damaging on a miss. No duplicate rewrite was made.

## Remaining work

- V0 still needs a manual in-world pass for selected friendly/hostile/boss models, projectiles, particles, GUI and feedback.
- Full 1.12 parity and unrelated rows remain outside this batch; the next code batch is selected only from the new `NEXT` rows in `MIGRATION-SHIP-MATRIX.md`.
