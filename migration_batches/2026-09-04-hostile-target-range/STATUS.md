# Hostile Target Range Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE; V0 PENDING_NATIVE_CLIENT` |
| Legacy tree | Read-only |
| Scope | Hostile target range and feedback boundary |
| Decision | [decision.md](decision.md) |

## Work log

- Batch created from the migration batch template.
- Protocol frozen before reading implementation code.
- Compared the hostile range and selector rules against the frozen 1.12
  `EntityAIShipRangeTarget`, `EntityAIShipRevengeTarget`, `TargetHelper`, and
  Tenryuu/Tatsuta skill-target entries.
- Applied the minimum modern changes: retain the legacy low integer attack-range
  fallback and keep hostile Tenryuu/Tatsuta replacement searches on their
  legacy fixed-radius selector path.
- Re-ran the corrected batch fixtures after the first fixture-only failure.
- The existing hostile combat reaction actionbar gate matched the legacy intent;
  no feedback code change was required.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/hostile-target-range.json](results/hostile-target-range.json)
