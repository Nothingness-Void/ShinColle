# SHIP-04 Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE` |
| Protocol | Frozen before implementation read |
| Scope | IDs `21`, `26`, `27`, `28`, `29`; hime models, projectiles and visual feedback |
| Legacy tree | Read-only |
| Verification | T0/T1/T1-R/T2 pass; 256 required GameTests pass |
| Client smoke | Startup and resource reload pass; in-world manual portion retained for V0 because the native window was not exposed to automation |
| Decision | `COMPLETE (SP visual/projectile boundary)` |

## Work log

- Batch directory created from the batch template.
- Protocol frozen from matrix rows `21`, `26`, `27`, `28`, and `29`.
- The modern resource pipeline was identified as a direct dependency: `build.gradle` copies the read-only legacy `Model*.java` sources into runtime resources for the parser.
- The model audit exposed a direct superclass dependency: the selected models extend `ShipModelBaseAdv`, so `ShipModelBase.java`, `ShipModelBaseAdv.java`, `IShipEmotion.java`, and `EmotionHelper.java` were added before reading their pose/equipment semantics.
- The entity audit exposed a direct superclass dependency: the selected Airfield/Harbour/Isolated Hime classes extend `BasicEntityShipCV`, so its inherited aircraft selection and launch rules were added before reading that behavior.
- The attack audit exposed two direct projectile dependencies: Isolated Hime's type-5 static field and Battleship Hime's type-3 cluster missile are implemented by `EntityProjectileStatic` and `EntityAbyssMissile`, so both were added before reading their impact rules.
- Method-level comparison completed for all five rows; no legacy source or resource file was modified.
- Added exact legacy model-state visibility rules for Airfield, Destroyer, and Isolated Hime; Battleship and Harbour keep their legacy always-visible equipment boundary.
- Corrected Airfield and Harbour aircraft launch height to the legacy `0.7F` factor.
- Preserved Battleship cluster and Isolated black-hole projectile families; mapped Harbour's legacy type-2 railgun head to an appended modern particle type without changing existing particle ordinals.
- Added focused GameTest coverage for model stems, parser/bake output, aircraft/projectile families, cluster/black-hole dispatch, and legacy `showEquip` bit rules.
- Ran one dev client launch after the batch: Java 17, GL initialization, resource reload, and ShinColle initialization completed without a ShinColle model/texture/fallback error. The environment did not expose the native game window for world interaction.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/ship-04.json](results/ship-04.json)

## Remaining work

- V0 still needs a manual in-world pass for friendly/hostile/boss models, projectiles, particles, and feedback because this environment exposed only the client process/log, not an operable native window.
- Hostile Boss far-chase and player-facing hostile notification remain outside this batch and retain their separate audit/backlog entries.
- The next code batch is `SHIP-05`; its scope is defined only by the `NEXT` rows in [MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md).
