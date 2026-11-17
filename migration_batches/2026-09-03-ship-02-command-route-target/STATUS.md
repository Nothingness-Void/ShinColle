# SHIP-02 Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE` |
| Protocol | Frozen before implementation read |
| Scope | IDs `10`, `12`, `13`, `14`, `15`; command, route, guard, and target boundaries |
| Legacy tree | Read-only |
| Modern tree | Frozen modern read/change set in `protocol.md` |
| Client smoke | Not required unless client/resource/render files enter the batch |
| Verification | T0, T1, T2 passed; `All 253 required tests passed` |
| Decision | `CHANGED` |

## Work log

- Batch directory created and protocol frozen.
- Corrected the modern `RouteNode` package path after filename-only verification; no implementation content was read before the correction.
- Added the directly referenced legacy `TargetHelper`, `EntityHelper`, and `FormationHelper` semantic sources after caller-only inspection showed they are required for a strict comparison.
- Added the direct legacy guardian contract and modern ranged-goal/owner-data declarations needed to verify route memory, follow continuation, and migrated waypoint ownership.
- Restored the legacy 64-block commander-to-ship gate, unbounded move coordinates, and unbounded player-to-guard-target command path.
- Restored route last-waypoint/backtracking behavior, no-outgoing-link guard retention, and cross-dimension command invalidation.
- Added legacy waypoint ownership, stay-mode, route-link, and paired-chest NBT fallbacks.
- Added focused coverage for IDs `10`, `12`, `13`, `14`, and `15`; the final full run passed all 253 required tests.

## Evidence

- [ship-02.json](results/ship-02.json)
- [decision.md](decision.md)
