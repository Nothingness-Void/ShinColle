# Decision

## Decision

- `EntityAIShipRangeTarget` first updates only on the legacy eight-tick cadence,
  uses the integer attack range for its horizontal search box, and falls back
  to `max(2, FollowMax + 2)` when that integer range is below two. The legacy
  base default is `FollowMax = 12`, so the effective fallback is `14`.
- Modern `aiFollowRange` already carries that effective compatibility default.
  `getHostileTargetSearchRange()` now uses it only for the same low-range case;
  normal hostile bosses continue to use the integer compatibility attack range.
- Legacy hostile Tenryuu and Tatsuta skill replacement searches use fixed
  `8` and `13` block AABBs respectively, with `TargetHelper.SelectorForHostile`.
  The modern catalog now keeps that skill search radius separate from the
  normal hostile target leash and limits hostile replacement candidates to the
  legacy LivingEntity target family: players or friendly ships. It does not
  widen ordinary target acquisition.
- The existing `ClientboundCombatReactPacket` participant/attacker gate is
  already consumed by `GameplayClientEffects`; hostile boss aircraft launch and
  hit reactions therefore do not produce player actionbar feedback. No code
  change was needed for that path.
- The first GameTest run exposed two incorrect fixtures, not a runtime
  regression: the Tenryuu candidate was outside its fixed eight-block search
  box, and the low-range case had not actually reduced the compatibility range.
  Both fixtures were corrected before the passing rerun.

## Scope boundary

The static and server-side evidence closes this targeted behavior batch. It does
not replace the pending V0 native-client in-world acceptance, which remains the
only release gate outside this batch.

## Evidence

- [protocol.md](protocol.md)
- [results/hostile-target-range.json](results/hostile-target-range.json)
