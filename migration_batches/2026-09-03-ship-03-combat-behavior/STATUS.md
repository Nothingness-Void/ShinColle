# SHIP-03 Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE` |
| Protocol | Frozen before implementation read |
| Scope | IDs `16`, `17`, `18`, `19`, `20`; combat behavior and runtime boundaries |
| Legacy tree | Read-only |
| Modern tree | Frozen modern read/change set in `protocol.md` |
| Client smoke | Not required unless client/resource/render files enter the batch |
| Verification | T0, T1, T2 passed; `All 254 required tests passed` |
| Decision | `CHANGED` with documented parity boundaries |

## Work log

- Batch directory created from the batch template.
- Protocol frozen from matrix rows `16`, `17`, `18`, `19`, and `20`.
- Method-level comparison confirmed the transport cargo loop, submarine role/visibility/pickup paths, Carrier Hime melee missile hook, generic CV capacity/recovery path, and shared projectile miss handling.
- Added the missing legacy idle-grudge mappings for class `16` and classes `17`-`19`.
- Added Carrier Hime's legacy `0.9F` aircraft launch-height factor.
- Added focused coverage for all five selected rows; the final full run passed all 254 required tests.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [ship-03.json](results/ship-03.json)
