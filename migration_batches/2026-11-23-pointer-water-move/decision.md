# Pointer water movement — result and parity decision

## Classification and minimal change

Confirmed migration regression, reproduced with six real owned ships and their
registered goal selectors. Restore only LegacyShipFloatGoal's mutex from MOVE
to TARGET (the fourth flag, legacy bit 8). No priority, speed, buoyancy, arrival
radius, formation, ray, search budget or coordinate-conversion changes.

Legacy read-only EntityAIShipFloating.java:42 uses setMutexBits(8), while
EntityAIShipGuarding.java:87 uses 7. Historical compilable 0aad501 has the same
two declarations at the same lines. They do not overlap. Modern
LegacyShipGuardGoal.java:44-51 claims MOVE/LOOK/JUMP, but the pre-fix floating
goal claimed MOVE. LegacyShipEntity.registerGoals registers floating at 0 and
guard at 1 (Forge lines 724-726; Neo 725-727). GoalSelector's per-flag lock
therefore prevents guard from starting while floating owns MOVE.

This is reachable: LegacyShipEntity.canUseLegacyFloatingGoal (Forge
2772-2779; Neo 2774-2781) enables floating for submerged ships with no path.
The command is already assigned and canExecuteGuardGoal is true for every
slot in the reproduction, but floating blocks the very guard tick that would
create a path. Different depths/floating thresholds explain why only some
members are affected. Guard tick performs tryMoveToXYZ at
LegacyShipGuardGoal.java:103-105; correcting the independent mutex lets that
happen, after which floating's existing noPath condition disables it.

## Controlled reproduction before production edits

First Neo run, unchanged production: water arrival after 384 ship ticks was
[true, true, false, false, false, true]; land control passed. Failure:
```text
sixwatershipsstartpointermoveandarrive failed at -9037198, -60, 4653788! all ships must reach legacy guard tolerance: water=true arrived=[true, true, false, false, false, true]
```

Second unchanged-production run added an independent legacy-mutex oracle:
the same fixture only assigns floating its historical fourth flag. This
attempt to disprove the scheduling hypothesis ruled out destination/path
geometry as necessary causes: all six then started paths and arrived.
```text
Pointer water=true legacyMutex=true started=[true, true, true, true, true, true] arrived=[true, true, true, true, true, true]
Pointer water=true legacyMutex=false started=[true, false, false, false, false, false] arrived=[true, true, false, false, false, true]
Pointer water=false legacyMutex=false started=[true, true, true, true, true, true] arrived=[true, true, true, true, true, true]
```

Logs are retained outside OS temporary directories:
- /home/ubuntu/shincolle-pointer-water-before-neo.log
- /home/ubuntu/shincolle-pointer-water-oracle-neo.log

## Dispatch and rejected alternatives

- Modern PointerItem.use:85-107 clips OUTLINE + Fluid.ANY for 64 blocks and
  retains a liquid block's Y; dry blocks offset by hit face. useOn:129-135
  applies the same liquid/RouteNode offset rule to its supplied clicked block.
  Legacy PointerItem.java:553 onward calls
  BlockHelper.getPlayerMouseOverBlockOnWater; BlockHelper.java:434-454 stops on
  liquid. Legacy likewise keeps liquid coordinates and offsets dry hits.
  Tests independently clip a real pool/land ray and assert the chosen Y/block.
  Client interaction/packet transport itself was not replayed.
- PointerClientEvents / PointerCommandEvents handle left-click selection,
  not right-click block movement. PointerItem.sendMoveOrGuardCommand creates a
  ServerboundShipCommandPacket (sprint move, ordinary guardBlock); service
  handleInternal and GameplayCommandHandler's compatibility MOVE_TO_POS both
  reach ShipCommandService.moveToPos:165-201.
- That service resolves all commandable members and assigns each target.
  Legacy C2SGUIPackets.SetMove -> FormationHelper.applyTeamMove:1083 onward
  also iterates the team and applies guard targets. Existing single/selected/
  formation policy was not changed. Reproduction uses fleet mode 2, six ships,
  formation 0: all six receive identical executable targets, disproving
  flagship-only dispatch and formation offsets as necessary causes.
  Existing full-suite formation/selection tests remain required.
- LegacyShipEntity.commandMoveTo (Neo 2942-2960; Forge two lines earlier)
  clears follow and stores the position; LegacyShipGuardGoal uses it for path
  creation. Ordinary guardBlock uses that same goal.
- Real ship movement uses ShipPathNavigate / ShipPathFinder / ShipMoveHelper,
  not LegacyShipObstaclePath. The latter's actual consumers are mounts,
  aircraft and floating fort projectiles; it is not this reproduction's path.
- ShipPathNavigate:48-65 keeps range 70 for ships and floors destinations.
  ShipPathFinder permits fluid nodes and 3D options, with the existing
  450-node bound. Legacy uses the same range and fluid-aware 3D search.
  Destinations here are about 17 blocks away. Stuck/repath code is unchanged;
  the pre-fix failure occurs before several ships can create a path.
- Native GameTest pool/land fixtures execute at negative Y; fractional depth
  floor conversions and existing negative-height tests remain intact. The
  mutex-only oracle succeeds at the same negative/far coordinates, so the
  previous negative-Y adaptation is not necessary to explain this failure.

## Coverage and fixture limits

PointerWaterMoveGameTests adds four tests: six water ships, six land ships,
independent legacy mutex oracle, and mutex/dry/sitting contract. Ships use eggs
53/54/55/56/38/39 at different submersion depths. Actual registered goals, ship
ticks, navigation and movement execute; no direct guard-goal ticking or
teleport-to-target shortcut. Assert every slot receives its command, all paths
start within 32 ticks, and each ship reaches the legacy maximum follow/guard
hysteresis tolerance within 384 ticks. Empty selected-group dispatch must return
NO_COMMANDABLE_SHIPS. Arrival means each member reaches tolerance, not that
idle movement is frozen afterward.

Forge's first fixture run failed both land and water on alternating entity IDs,
including the oracle. Bytecode inspection of 1.20 Mob.serverAiStep showed
scheduler parity uses MinecraftServer.getTickCount()+entity ID, not entity age.
The synchronous fixture had held that server clock constant. Forge-only
fixture now advances and restores that clock in try/finally, alongside the
entity age normally advanced by ServerLevel; reconciliation/assertions were
not weakened. Neo uses its existing entity-clock fixture.

An unrelated Neo drop-marker/radar test failed during both pre-fix runs.
A new detached, unchanged 4380e97 baseline worktree reproduced the identical
failure (383 tests, one failure); no unrelated test or production edit was made.
The final full Neo run passed it. Its intermittent root cause remains unproven.
Baseline log: /home/ubuntu/shincolle-pointer-water-baseline-neo.log.

## Final full gates

Forge JDK17, bash ./gradlew build runGameTestServer:
```text
========= 378 GAME TESTS COMPLETE ======================
All 378 required tests passed :)
BUILD SUCCESSFUL in 50s
```
Log: /home/ubuntu/shincolle-pointer-water-final-forge-clock.log

Neo JDK21, ./gradlew build runGameTestServer:
```text
========= 387 GAME TESTS COMPLETE IN 19.32 s ======================
All 387 required tests passed :)
BUILD SUCCESSFUL in 38s
```
Log: /home/ubuntu/shincolle-pointer-water-final-neo.log

Both final water/land and oracle summaries show all six started and arrived.
Default gates, no init-script overrides or repository build/CI edits. No
production/test inputs changed after the corresponding passing gate.

src/main remains read-only and unchanged. No version bump or new mechanic.
No visual verification or actual 1.12 runtime replay; legacy compatibility is
established by the historical/read-only mutex declarations and independent
scheduler oracle, not a claim to have run the original player's session.
