# SHIP-02 Decision Record

## Decision

`CHANGED`

## Evidence rule

Record each audited behavior as one of:

- `MATCHED`: modern behavior matches the legacy semantic rule with only a documented MC API adaptation.
- `CHANGED`: a minimal modern-side correction was required and is listed with its test evidence.
- `BLOCKED`: the comparison cannot be completed without an external decision or a file outside the frozen set.

An exact match must still be recorded so the next batch does not reread the same code.

## Per-ship record

| ID | Ship | Command | Route/guard | Target | Result |
| ---: | --- | --- | --- | --- | --- |
| 10 | `EntityCANe` | `CHANGED`: shared command service now uses strict `<64` commander range | `CHANGED`: route/guard state uses legacy last-waypoint rules | `MATCHED`: target command has no player-to-target cap; normal engagement validation remains | `CHANGED` |
| 12 | `EntityCarrierWo` | `CHANGED`: same legacy command boundary covers carrier command entry | `CHANGED`: shared route/guard state and dimension invalidation | `MATCHED`: target command has no player-to-target cap; normal engagement validation remains | `CHANGED` |
| 13 | `EntityBattleshipRu` | `CHANGED`: same legacy command boundary covers battleship command entry | `CHANGED`: shared route/guard state and dimension invalidation | `MATCHED`: target command has no player-to-target cap; normal engagement validation remains | `CHANGED` |
| 14 | `EntityBattleshipTa` | `CHANGED`: same legacy command boundary covers battleship command entry | `CHANGED`: shared route/guard state and dimension invalidation | `MATCHED`: target command has no player-to-target cap; normal engagement validation remains | `CHANGED` |
| 15 | `EntityBattleshipRe` | `CHANGED`: same legacy command boundary covers battleship command entry | `CHANGED`: shared route/guard state and dimension invalidation | `MATCHED`: target command has no player-to-target cap; normal engagement validation remains | `CHANGED` |

## Scope decisions

- The hostile boss far-chase report is deferred to a dedicated target-range reproduction batch.
- Attack cadence, recovery, and post-attack motion remain outside this batch.
- The legacy read set was expanded only to direct utility dependencies of the frozen target/route callers; no unrelated legacy tree scan is permitted.
- The frozen modern set was expanded only for `LegacyShipRangedAttackGoal` (target continuation), `PlayerOwnerData` (legacy waypoint owner UID), and the legacy `IShipGuardian` contract (ship-side last-waypoint state).

## Facts established

**后续取代（AI部分）**：本节及Code changes对跟随/守卫的阶段性匹配说明不再作为完整等价证据；旧现代跟随门槛、固定半径/每tick moveTo已撤销，见[AI移动批次](../2026-11-22-parity-fix-ai-movement/decision.md)。命令距离与路点权限等独立结论保留。

- `FormationHelper.applyTeam*` uses a strict commander-to-ship distance below 64 blocks. The modern command service now uses the same boundary for individual commands and filters group members using that boundary.
- Legacy move coordinates are not rejected because they are far from the player. Legacy guard and attack command entry points also do not add a player-to-target distance wall; target type, alive state, ownership/team rules, and ship engagement rules remain separate checks.
- Formation mode preserves the old all-member gate: if any current-team ship is not commandable, the formation move/guard call does not partially apply.
- The follow goal remains active while the owner is far away even when the current navigation path has ended, allowing the legacy re-path behavior.
- Legacy route traversal remembers the previous waypoint, can choose the outgoing link that is not the previous node, and remains at the current guard node when no outgoing link exists. A command in another dimension clears the command and navigation state.
- Waypoint legacy arrays use the old positive-Y sentinel rule only during legacy-array fallback; modern absolute positions may use the full modern coordinate range.
- Legacy waypoint `pid` ownership is enforced through the player's migrated UID when no modern UUID owner is present; unassigned waypoints remain editable.

## Code changes

- [ShipCommandService.java](../../src/modern/java/com/lulan/shincolle/network/ShipCommandService.java) restores the legacy command range and target-coordinate boundaries, including the formation all-member gate.
- [LegacyShipFollowOwnerGoal.java](../../src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFollowOwnerGoal.java) restores continued follow behavior after a path ends while the ship is still distant.
- [LegacyShipEntity.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) adds runtime last-waypoint/backtracking state and clears invalid cross-dimension commands.
- [WaypointBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/WaypointBlockEntity.java) and [PlayerOwnerData.java](../../src/modern/java/com/lulan/shincolle/ownership/PlayerOwnerData.java) migrate legacy waypoint ownership and route fields.
- [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) adds focused assertions for the five matrix variants and legacy waypoint migration.

## No-change audits

- No per-ship attack cadence, recovery, servant lifecycle, post-attack motion, renderer, resource, GUI, or hostile boss far-chase policy was changed by this batch.
- The five selected IDs use the same shared command/route/target path; no speculative per-ship dispatch was added.
- `src/main/java` and `src/main/resources` remain unchanged.

## Evidence

- [ship-02.json](results/ship-02.json)
- Final T0/T1/T2 verification: `git diff --check`, `compileJava`, `processResources`, and `runGameTestServer` passed.

## Follow-up

- Mark IDs `10`, `12`, `13`, `14`, and `15` as `LOCKED` only for the domains listed in this record.
- Promote the next matrix group, IDs `16`, `17`, `18`, `19`, and `20`, to `NEXT` for the next batch; do not reopen this batch unless a regression appears.
