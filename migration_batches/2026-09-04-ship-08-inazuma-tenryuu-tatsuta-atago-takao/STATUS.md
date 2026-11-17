# SHIP-08 Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE (SP mainline boundary)` |
| Protocol | Frozen before implementation read |
| Scope | IDs `54`, `56`, `57`, `58`, `59`; destroyer/cruiser runtime and combat |
| Legacy tree | Read-only |
| Verification | `git diff --check`, `compileJava`, `cleanProcessResources processResources`, and `runGameTestServer` passed; `All 264 required tests passed` |
| Client smoke | Java 17 startup, OpenGL, resource reload, and model log gate passed; in-world manual interaction remains V0 |
| Decision | [decision.md](decision.md) |

## Work log

- Batch directory created from the batch template.
- Protocol frozen from matrix rows `54`, `56`, `57`, `58`, and `59` before reading code.
- Inazuma's direct Akatsuki gattai/dismount dependency is included in the frozen legacy source list.
- The five hostile `*Mob` entry points and the actual cruiser model source names were added after path discovery, before reading those files.
- Targeted comparison found the selected runtime, combat, target, and rider paths already matched the existing modern catalog; no new per-ship dispatch was needed.
- The only implementation gap in this batch was the five legacy model `showEquip` branches; those exact bit mappings now route through `LegacyShipModelStateCatalog` and the existing parser/bake path.
- The batch was implemented as one slice and verified once; no repeated client launch was used during implementation.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/ship-08.json](results/ship-08.json)
