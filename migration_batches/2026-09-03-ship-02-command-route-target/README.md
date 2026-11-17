# SHIP-02 Command, Route, and Target Boundaries

## Status

`COMPLETE`

## Batch question

For ship IDs `10`, `12`, `13`, `14`, and `15`, do the modern command, route, guard, and target boundaries preserve the legacy 1.12 behavior with only the minimum Minecraft API adaptation?

## Scope

- `10` `EntityCANe` / `EntityHeavyCruiserNe`
- `12` `EntityCarrierWo`
- `13` `EntityBattleshipRu`
- `14` `EntityBattleshipTa`
- `15` `EntityBattleshipRe`
- Explicit command ownership, target validity, target range, route/waypoint state, guard state, and priority boundaries.

## Working rule

Read only the files and methods frozen in `protocol.md`. Implement only modern-side changes. Record exact matches as `decision.md` evidence instead of creating speculative code.

## Completed work

- Restored the legacy strict commander-to-ship command gate: a ship must be within 64 blocks using the old strict comparison.
- Removed modern-only player-to-move-target and player-to-guard-target distance caps; preserved the legacy formation all-member gate.
- Restored follow-goal continuation while a distant ship is still seeking a navigation path.
- Added runtime last-waypoint memory, legacy backtracking, no-outgoing-link behavior, and cross-dimension command invalidation.
- Added legacy waypoint NBT migration for `pid`, `wpstay`, `lastPos`, `nextPos`, and `chestPos`, including UID-based owner editing checks.
- Added focused command, route, and waypoint migration GameTests covering all five selected variants.

## Evidence

- Machine-readable verification: [ship-02.json](results/ship-02.json)
- Final regression: `All 253 required tests passed`
- Client smoke: not applicable; this batch changed no client, resource, renderer, particle, or screen files.

## Frozen non-goals

- Combat cadence, attack profiles, ammunition recovery, and resource recovery; these belong to `SHIP-03` or later.
- Servant lifecycle, aircraft recovery, and post-attack motion.
- Models, rendering, particles, screens, projectile visuals, recipes, and world generation.
- A new hostile boss target-range policy; the previously reported far-chase behavior is a separate reproduction and decision batch.
- Multiplayer permission parity and complete legacy packet-heavy GUI pages.

## Exit

The batch exits only after the five ship rows have a recorded command/route/target result, focused GameTest coverage is added or an exact existing test is cited, the batch verification commands pass, and the state documents point to the next batch.
