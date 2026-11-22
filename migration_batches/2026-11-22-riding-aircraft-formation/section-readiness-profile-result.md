# Section visibility and actual mount-profile continuation

## Question, scope, entry and status

Continuation of [frozen scope](broad-wall-recovery-protocol.md), after the user's
explicit instructions: fix the recovery fixture without weakening reconciliation,
collect NF07/08/02/01 versus NF10 actual traces, and do NOT port mounts to
LivingEntity. Forge remains local; Neo may push after its full gate. Previous
[results](broad-wall-recovery-result.md) remain historical, not current blockers.
Source checkpoints: Forge d4146b4 (fixture), a8af4d6 (clearance); Neo adaptation
51a0beb and ordinary merge 163c20a. Final documentation/push SHAs are in the
handoff. PR11A 5145edf and B 6c5214c remain ancestors.

## Recovery: verified visibility assumption failure, not lost registration

Original Neo diagnostic: transferred entity has the original UUID/UID,
isAddedToLevel=true and removed=false. PersistentEntitySectionManager.isLoaded
is true, but canPositionTick/isPositionEntityTicking are false and getEntity(id)
is null. At ticks 0/20/40 this persists. A FakePlayer added to the Nether at tick
40 yields players=1 but still no ticking/visibility through tick 140. Merely
having a FakePlayer in the level is NOT proof of a genuinely player-loaded chunk.

A separate real-player control in the private 1.21.1 integrated client used a
fresh Nether chunk, actual vanilla cross-dimension /tp, then teleported the
connected ServerPlayer there and advanced 80 native ticks. No manual section
promotion was used in this control:
```
REAL-CONTROL initiallyChunk=false
REAL-CONTROL beforePlayer known=true visible=null
REAL-CONTROL afterPlayer players=1 known=true visible=LegacyShipEntity['Destroyer Akatsuki'/1864, l='ServerLevel[GUI Audio Regression]', x=1333333.50, y=100.00, z=1333333.50] ticking=true
```
The displayed world name is shared; the inspected level was server.getLevel
(Level.NETHER). The player was returned and frozen afterwards.

The fixture now asserts that the destination manager knows the UUID, explicitly
applies FullChunkStatus.ENTITY_TICKING, then retains the original getEntity(id)
identity assertion and ALL duplicate reconciliation checks: distinct same-UID
live copies, >200 grace/200 boundary, older removal, younger live cache survivor,
dead-copy and reverse-age cases. Production loading/recovery code is unchanged
by this fixture adjustment. This establishes the fixture's visibility-readiness
assumption was false; the exact upstream ticket-scheduling reason for forced
headless chunks not advancing during that diagnostic remains unproven. It is
not evidence of general production entity-registration loss.

## Actual profile traces and falsification checks

Native private client, factory-created mounts/fortress and real targets, no
owner/camera movement during each run. Start (202,101,201), guard entity target
(204.5,106,215.5), glass at Z=208 with top Y=110. Fortress's actual movement goal
is target+1 Y. Each mount carries its real host; the fortress has a real,
eligible target. Native tickCount is advanced explicitly. Every tick records
position, goal, speed, path index/length/nodes/final point and next replan.
Replans are ticks 1,17,33,... (16-tick cadence), not absent or continuously reset.

| Profile / egg | Speed | Before: final feet Y / Z | After: first tick whole bbox beyond wall |
|---|---:|---|---:|
| NF07 / 32 | 0.4201600015163422 | 109.99959276580903 / 206.75 | 49 |
| NF08 / 22 | 0.4141759991645813 | 109.99958537504206 / 207.05000001192093 | 47 |
| NF02 / 23 | 0.43023999929428103 | 109.99956857251847 / 207.05000001192093 | 49 |
| NF10 / 35 | 0.4543359935283661 | 109.99938461972322 / 207.05000001192093 | 47 |
| NF01 / 33 | 0.317127999663353 | 109.99943704205958 / 207.75 | 67 |

At tick 128, paths are nonempty, index 1, and contain wall-top Y=110 nodes
followed by far-side nodes (NF07 towards Z214; others towards Z215). E.g. NF08
replans with start (203,110,207), next (203,110,208), then (203,110,209);
the current target is reachable horizontally only once feet reach Y110.
Actual feet remain fractionally BELOW 110, so the full bbox is clamped at the
front face. Replanning does not remove this asymptote.

Disproof checks: NF08 and NF10 share 1.9 x 2.1 dimensions; speed differs but both
stall in this elevated control. NF10's earlier VM pass is therefore NOT an
intrinsic profile exemption. Here all five stall before the change, and all
clear afterwards without speed/search/radius changes. The original VM
five-pass/four-fail grouping is not fully explained without its original
per-tick scene state. These controlled actual traces are not that world's
end-to-end acceptance. The older exact snapshot returning null after 450 nodes
is a separate bounded-search failure and its budget is unchanged.

## Confirmed legacy divergence and minimal change

Legacy BasicEntityMount.onLivingUpdate invokes EntityHelper.updateShipNavigator,
which dispatches ShipPathNavigate.onUpdateNavigation and the movement helper.
Once the legacy navigation gate is open and a path exists, it sends
vec3.y + 0.1D to setMoveTo, after its block-height adjustment. Modern
LegacyShipMountEntity.moveToward / fortress movement consume
LegacyShipObstaclePath.resolve directly, which previously returned raw node
Y110. Native traces show that exact path/dispatch executing at the stalled face;
the same route with the missing legacy clearance restored crosses.

Restore the legacy 0.1 Y clearance for active waypoints, but only when the
raised whole bbox fits solid blocks; otherwise keep the raw waypoint.
Unconditional +0.1 failed existing under-wall/finite-route tests, so retaining
that candidate would be wrong. This safety adaptation preserves under-ceiling
routes in the modern non-Living movement implementation. No profile-specific
values, speed changes, extra search nodes, host-effect proxy or LivingEntity
port. Own-mount LEVITATION remains an explicit deferred API gap.

New elevatedGuardRoutesClearWallTopForReportedMountsAndFortress tests all five
actual flyers with eligible guarded targets and asserts whole-bbox noCollision
on every tick and full passage beyond the wall. The new geometry gets its own
48x32x48 template; it did not fit the older 26x16x24 fixture. Existing finite-wall,
all-eight-mount and collision assertions/locations remain untouched.

## Gate evidence and tooling

Commands and coverage are authoritative in the [test matrix](../../MIGRATION-TEST-MATRIX.md).
Final Forge JDK17 combined build/full gate:
```
========= 369 GAME TESTS COMPLETE ======================
All 369 required tests passed :)
BUILD SUCCESSFUL in 2m 23s
```
Final Neo JDK21 combined build/full gate:
```
========= 377 GAME TESTS COMPLETE IN 30.55 s ======================
All 377 required tests passed :)
BUILD SUCCESSFUL in 1m
```

Neo's first new-template attempt failed with Missing test structure:
shincolle:legacyaircraftblockcollisiongametests.elevated_flight_space. The
external isolated-run init script moved gameDirectory but left
syncGameTestStructures copying to the default run directory. Fixed that init
script centrally to copy to its actual gameDirectory, then reran Neo. No repo
build/CI configuration changed. Forge was not rerun: its inputs did not change.

Neo merge briefly duplicated the independently added test and conflicted on
template DataVersion; resolved to the already-tested Neo 51a0beb snapshot.
git diff --exit-code 51a0beb HEAD -- src/modern is empty after merge. Later docs
do not alter tested source inputs. Source main/java and main/resources untouched.

Artifacts (persistent home, not temporary):
- /home/ubuntu/shincolle-elevated-clearance-forge-final.log
- /home/ubuntu/shincolle-elevated-clearance-neo-final.log
- /home/ubuntu/shincolle-native-profile-before.csv
- /home/ubuntu/shincolle-native-profile-after.csv
- /home/ubuntu/shincolle-factory-visual.log (native control and trace endpoints)
- /home/ubuntu/ShincolleVisualAgent.java (private diagnostic harness)
- /home/ubuntu/shincolle-queue-gametest.init.gradle (isolated fixture sync fix)

No new rendering screenshot verification. Private native positions plus GameTest
collision checks establish movement, not visual presentation. Pointer/crane
interactions remain visually unverified. RingFly, audio and Shimakaze are
unchanged. No Forge push. Original VM scene retest is the next boundary.
