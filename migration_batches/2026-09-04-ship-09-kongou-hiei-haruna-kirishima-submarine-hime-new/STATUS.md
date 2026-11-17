# SHIP-09 Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE (SP mainline boundary)` |
| Protocol | Frozen before implementation read |
| Scope | IDs `60`, `61`, `62`, `63`, `72`; Kongou-class and new submarine Hime |
| Legacy tree | Read-only |
| Verification | `git diff --check`, `compileJava`, `cleanProcessResources processResources`, and `runGameTestServer` passed; `All 265 required tests passed` |
| Client smoke | Java 17 startup, OpenGL 4.6, resource reload, and model log gate passed; in-world manual interaction remains V0 |
| Decision | [decision.md](decision.md) |

## Work log

- Batch directory created from the batch template.
- Protocol frozen from matrix rows `60`, `61`, `62`, `63`, and `72` before reading implementation code.
- The shared heavy miss boundary was frozen as conditional scope: inspect and change it only if a selected attack reaches the affected helper.
- Targeted legacy comparison found the Kongou-family runtime, combat, target, rider, and dimensions paths already represented by the existing catalog; no new normal-attack dispatch was added.
- Added the four legacy battleship model `showEquip` mappings to `LegacyShipModelStateCatalog`, corrected the hostile Hiei chimney point, and routed SSNH riding through the shared legacy pending-target state.
- The batch was implemented as one slice and verified once; no repeated client launch was used during implementation.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/ship-09.json](results/ship-09.json)
