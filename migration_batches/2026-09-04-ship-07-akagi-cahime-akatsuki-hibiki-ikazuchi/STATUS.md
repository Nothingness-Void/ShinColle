# SHIP-07 Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE` |
| Protocol | Frozen before implementation read |
| Scope | IDs `48`, `49`, `51`, `52`, `53`; carrier/cruiser/destroyer runtime and combat |
| Legacy tree | Read-only |
| Verification | `All 263 required tests passed` |
| Client smoke | Java 17 startup, OpenGL 4.6, resource reload and model-path log gate passed |
| Decision | `COMPLETE (SP mainline boundary)` |

## Work log

- Batch directory created from the batch template.
- Protocol frozen from matrix rows `48`, `49`, `51`, `52`, and `53`.
- Direct dependencies were appended before reading: Akatsuki requires `EntityDestroyerInazuma` for the six-destroyer gattai order/placement boundary; Akagi requires `BasicEntityAirplane`, `EntityAirplaneZero`, and `EntityAirplaneT` for its light/heavy aircraft construction contract.
- Zero and T inherit their combat initialization and return behavior from `EntityAirplane` and `EntityAirplaneTakoyaki`; both base sources were appended before inspection.
- The old Akagi, CA Hime, Akatsuki, Hibiki and Ikazuchi `showEquip` branches are now represented by `LegacyShipModelStateCatalog` without replacing the legacy source parser.
- The old rider-side no-fuel overrides are now represented in the catalog: Hibiki clears Akatsuki gattai, and Ikazuchi/Inazuma clear Akatsuki or Raiden state before the normal rider-state refresh.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/ship-07.json](results/ship-07.json)

## Verification

- `git diff --check`: passed; no legacy source or resource file changed.
- `compileJava`: passed with JDK `17.0.3.1`.
- `cleanProcessResources processResources`: passed; no new resource path was introduced.
- `runGameTestServer`: passed, `All 263 required tests passed`.
- Client smoke: Java 17 startup, NVIDIA OpenGL 4.6, ShinColle initialization and resource reload passed; no model bake, missing texture or silent fallback log was found.
- Native client window was not exposed to the available automation surface, so in-world manual interaction remains a V0 task.
