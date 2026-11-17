# SP-CLOSE Status

| Field | Value |
| --- | --- |
| Status | `M0-M9 AUDITED; V0 PENDING` |
| Protocol | Frozen before implementation read |
| Scope | M0-M9 single-player mainline plus V0 evidence |
| Legacy tree | Read-only |
| Verification | `git diff --check`, `compileJava`, `processResources`, and `runGameTestServer` passed; `All 266 required tests passed` |
| Client smoke | Existing Java 17 startup/resource/model smoke remains valid; native in-world V0 is pending (subsequently confirmed PASS on 2026-09-16 in [V0-CLIENT-GATE](../2026-09-05-v0-client-gate/STATUS.md)) |
| Decision | [decision.md](decision.md) |

## Work log

- Batch directory created from the batch template.
- Protocol frozen after reading `CURRENT_STATE.md`, `ROADMAP.md`, `MIGRATION-SCOPE.md`, `TRACKS.md`, `ARCHITECTURE.md`, `NAMING.md` and `MIGRATION-FLOW-CHECKLIST.md`.
- Read only the protocol-listed legacy and modern target/feedback entries; no whole-repository rediscovery was performed.
- Compared legacy `EntityAIShipRangeTarget` and `TargetHelper.SelectorForHostile` with the modern hostile target goals and combat reaction gate. The hostile chase/feedback issue is `LOCKED / no change`.
- Audited 45 concrete legacy ship classes and 19 hostile Mob variants against the modern specs/catalog entries; all special-entry mappings are `MATCHED / code change NONE`. See [results/ship-override-audit.json](results/ship-override-audit.json).
- Added one cross-track GameTest for the documented single-player AI priority contract; no runtime behavior or protocol was changed.
- Completed one batch-level static/build/GameTest verification pass.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/sp-close.json](results/sp-close.json)
- [results/](results/)
