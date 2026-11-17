# SHIP-02 Frozen Protocol

## 1. Objective

Audit and, where necessary, minimally update the modern implementation so ship IDs `10`, `12`, `13`, `14`, and `15` preserve the legacy 1.12 command, route, guard, and target boundaries.

The legacy tree is the semantic source of truth. The modern tree may change only for the Minecraft 1.20.1 API surface or to complete behavior that is demonstrably missing from the legacy-equivalent path.

## 2. Legacy read set

Only these legacy files are in scope:

- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCANe.java`
- `src/main/java/com/lulan/shincolle/entity/carrier/EntityCarrierWo.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipRu.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipTa.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipRe.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostileCV.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeTarget.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipFollowOwner.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipGuarding.java`
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipRangeAttack.java`
- `src/main/java/com/lulan/shincolle/handler/ShipTargetHandler.java`
- `src/main/java/com/lulan/shincolle/handler/ShipMoveHandler.java`
- `src/main/java/com/lulan/shincolle/handler/ShipStateHandler.java`
- `src/main/java/com/lulan/shincolle/handler/CommandHandler.java`
- `src/main/java/com/lulan/shincolle/handler/GuardHandler.java`
- `src/main/java/com/lulan/shincolle/entity/IShipGuardian.java`
- `src/main/java/com/lulan/shincolle/utility/CommandHelper.java`
- `src/main/java/com/lulan/shincolle/utility/EntityHelper.java`
- `src/main/java/com/lulan/shincolle/utility/FormationHelper.java`
- `src/main/java/com/lulan/shincolle/utility/TargetHelper.java`
- `src/main/java/com/lulan/shincolle/utility/TeamHelper.java`
- `src/main/java/com/lulan/shincolle/utility/TaskHelper.java`
- `src/main/java/com/lulan/shincolle/tileentity/TileEntityWaypoint.java`
- `src/main/java/com/lulan/shincolle/tileentity/ITileWaypoint.java`
- `src/main/java/com/lulan/shincolle/capability/CapaShipSavedValues.java`

The relevant legacy entry points are the ship lifecycle/AI setup methods, `EntityAIShipRangeTarget.shouldExecute` and continuation checks, follow-owner and guarding goal checks, target/move/state/command/guard handlers, command/team/task helpers, and saved-value NBT read/write. Read only the method bodies needed to answer the batch question.

## 3. Modern read and change set

Only these modern files are in scope:

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangeTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipFollowOwnerGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipGuardGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipOwnerHurtByTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipHostilePlayerTargetGoal.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/goal/LegacyShipRangedAttackGoal.java`
- `src/modern/java/com/lulan/shincolle/network/ServerboundShipCommandPacket.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandAction.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandService.java`
- `src/modern/java/com/lulan/shincolle/teitoku/ShipWorldCacheEntry.java`
- `src/modern/java/com/lulan/shincolle/blockentity/WaypointBlockEntity.java`
- `src/modern/java/com/lulan/shincolle/blockentity/RouteNode.java`
- `src/modern/java/com/lulan/shincolle/ownership/PlayerOwnerData.java`
- `src/modern/java/com/lulan/shincolle/formation/LegacyFormationHelper.java`
- `src/modern/java/com/lulan/shincolle/formation/FormationRuntimeState.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

Modern work must stay within this set unless compilation proves that one directly adjacent declaration is required; any such exception must be recorded in `decision.md` before it is used. The three utility files above were added after the caller-only scan showed that they are direct semantic dependencies of the frozen target/route entry points.

## 4. Required comparison points

For each of the five ship IDs, record the legacy and modern result for:

1. Owner/team/dimension checks for explicit commands and targets.
2. Target acquisition range, target validity, and target clearing/continuation.
3. Priority between death/stop/sit, explicit command/route/move/guard/attack, active combat target, auto-supply, pickup, idle, and follow-owner behavior.
4. Waypoint/route node and guard state ownership, serialization, and dimension handling.
5. Packet decode and command-service behavior, including invalid target or unavailable-command feedback where the existing gameplay bus exposes it.
6. Formation state and world-cache round-trip where those states participate in command or route ownership.

## 5. Acceptance assertions

- IDs `10`, `12`, `13`, `14`, and `15` use the exact legacy owner, hostile/team, dimension, validity, and range boundaries, or have a documented minimum API adaptation.
- Explicit stop, sit, route, move, guard, and attack commands take priority over lower-priority goals wherever the legacy code gives them priority.
- Invalid targets are rejected or cleared with the legacy-equivalent result; no unrelated target policy is introduced.
- Route node, waypoint, guard entity/block state, and applicable formation/cache state survive modern save/load or cache round-trip with dimensions intact.
- Existing packet encode/decode and command-service tests pass; protocol changes are allowed only if the current bus cannot express an in-scope single-player operation.
- Focused GameTests cover the five IDs, target validity, command priority, route/guard round-trip, and failure feedback.
- Required batch commands pass: `git diff --check`, `compileJava`, `processResources`, and `runGameTestServer` under JDK 17.

## 6. Explicit non-goals

- Do not change heavy/air/special attack cadence or damage semantics.
- Do not tune hostile boss pursuit distance in this batch.
- Do not repair renderer/resource/model/projectile/UI behavior.
- Do not restore multiplayer-only permissions or complete historical packet/UI parity.
- Do not read or modify files outside the frozen sets without recording the reason first.

## 7. Batch stop boundary

After verification, update this batch's records, `MIGRATION-SHIP-MATRIX.md`, `CURRENT_STATE.md`, `ROADMAP.md`, and `MIGRATION-CHANGELOG.md`, then stop at the new `ROADMAP.md` `NEXT` pointer.
