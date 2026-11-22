# Broad-wall / recovery / PR11 / crane handoff

## State and commits

Forge source HEAD9a34c55 is LOCAL ONLY (push hold). Neo source HEAD837edb5 is
LOCAL ONLY; remote remains77bd8ba. No new push on either branch. Ordinary merges.
src/main/** unchanged. Shared-history commits:
- PR11A5145edf: protocol1->2.
- PR11B6c5214c: owner/team/sneak target consumption, modifier fall-through;
  conditional native short-reach callback avoids double own-ship selection.
  Repeated LeftClickBlock event/hold behavior unchanged.
- Recovery470f762: legacy delayed duplicate guard, actual transfer/recovery GT.
- Crane9a34c55: empty-buffer fluid wait, normal/empty/partial container GT.
- Neo992e509 integrates A/B with Neo event imports and Optional API.
- Neo5444260 merges recovery/crane;837edb5 adapts new test APIs.

## Final checks: Forge green, Neo BLOCKED

Forge JDK17, bash ./gradlew (wrapper not executable), existing Curios init:
```text
========= 368 GAME TESTS COMPLETE ======================
All 368 required tests passed :)
BUILD SUCCESSFUL in 1m 17s
```
(The literal spaced output is in /home/ubuntu/shincolle-recovery-crane-forge-final.log.)

Neo JDK21, existing isolated GameTest init, build phase completes; combined gate:
```text
========= 376 GAME TESTS COMPLETE IN 32.94 s ======================
1 required tests failed :(
BUILD FAILED in 1m 5s
```
/home/ubuntu/shincolle-recovery-crane-neo-final.log; only failure:
```text
unloadednethertransferrecoveryretainsonlyyoungercopyafterlegacygrace failed at5733407,-60,-12196336!
forced destination must expose transferred id=2120
uuid=2d2729b6-758e-4113-90bd-a43c066621c3
original=2d2729b6-758e-4113-90bd-a43c066621c3
removed=false pos=(1227200.5,100.0,1000000.5)
```
Do NOT call Neo green or push it. Earlier A/B-only Neo gate was373/373 green59s;
that does NOT cover later recovery/crane source.

## Recovery: initial window is legacy; permanent duplicate guard was missing

Actual creative FakePlayer /execute in Nether run tp into an initially UNLOADED
chunk, then /ship get before destination ticking, reproduces two same-UID objects.
Console was invalid (legacy registration intentionally player-only), fixed to a
creative FakePlayer. Cache still names origin before destination ticks; recovery
at origin creates a second live ship. This initial window exists in legacy
ShipCmdGetShip and is intentionally retained.

Legacy ServerProxy.checkShipIsDupe requires both living copies aged>200, and
deletes the OLDER one (equal age deletes current). Restored in refreshShipCache,
looking up only the cache-named existing level/entity, without loading all worlds.
Dead callback skips guard; survivor is cached after discard callbacks. Separate GT
covers exact200 grace, dead cached copy, and cached-older/current-older branches.
No dead revival and no dimension-load scan added.

Neo validation is unfinished: captured original remains not-removed with correct
UUID/UID but getEntity(id) returns null after its chunk is forced. Therefore the
test never reaches guard assertions there. Forge had two visibility failures at
fixed20 ticks, then passed unchanged while crane source changed; replaced the
arbitrary20-tick callback with succeedWhen(actual tracking), preserving all
assertions. Final Forge passes, Neo still times out160 ticks. UUID mismatch is
disproved. Pure async IO delay is NOT confirmed (longer conditional wait failed).
Stop repeated visibility checks and hand off diagnosis; do not weaken/relocate.

NEXT: inspect Neo PersistentEntitySectionManager section visibility, captured
entity added-to-level state and chunk FULL/ENTITY_TICKING readiness. Use a real
loaded-Nether/player control if necessary. Do not change production guard merely
to pass this fixture. No claim that Neo recovery reconciliation is verified.

## Broad wall / profile diagnostics: no production mount change

Native private client236617, frozen again. Other window56623113 untouched.
Trace at ticks295: mount202.49616887043277,102.49616877147062,207.05000001192093,
camera202,105,194, stable command BlockPos202,102,214, guard=null, path=NULL.

Controlled exact-size Airfield1.9x1.3 replay at THAT stalled position: all8
combinations step0/3, flyfalse/true, solid goal/cleared goal visit exactly450
nodes and return NULL. From202,101,201 they return wall-side partial points
201,100,206(false) or201,102,206(true). Legacy ShipPathFinder has the SAME450
dequeue cap; BasicMount step3 and own LEVITATION are verified source parameters.
This shows shared bounded-search failure at this snapshot, not full legacy
stochastic-stuck/navigation runtime equivalence. No budget increase shipped.

New batchD: NF05/06/09/10/11 passed; NF07/08/NF02/NF01 remain OPEN. Mapping from
earlier D3 report and current catalog:
|Case|Entity|bbox WxH|finder voxel WxHxD|
|---|---|---|---|
|NF01|Fortress|0.5x0.5|1x1x1|
|NF02|Airfield|1.9x1.3|2x2x2|
|NF07|Midway|2.5x2.9|3x3x3|
|NF08|Carrier|1.9x2.1|2x3x2|
|NF05 PASS|Harbour|1.9x1.6|2x2x2|
|NF06 PASS|Isolated|1.6x2.2|2x3x2|
|NF09 PASS|Battleship|1.9x3.1|2x4x2|
|NF10 PASS|CarrierWaterOni|1.9x2.1|2x3x2|
|NF11 PASS|Submarine|1.8x1.6|2x2x2|

Contact centers explain the reported z values: wall9 minus half-width gives
Fortress8.75 and Airfield8.05. That proves contact, not why routing failed.
NF08 and PASS NF10 have identical bbox, so bbox/height ALONE cannot explain the
split. All mounts share finder70, hardcoded flying=true and repath16; movement
uses clamp((.4+host.moveSpeed*.2)*hostCombatModifier,.2,.8), not a profile speed.
Fortress uses max(.18,host.moveSpeed*.1+.3). Actual per-profile current speeds and
live nodes/indices/repath timestamps from the new VM scenes are NOT yet captured.

Additional CANONICAL11x10 wall atz208, x197..207, y100..109, floor99,
start202,101,201, goal204,106,214, isolates bbox/step/fly (NOT exact new VM
repro): every flying=true case consumes450 and ends FRONT of wall: Fortress
204,106,207; Airfield/Carrier203,106,206; Midway203,106,205.
Legacy step3+flying=false reaches wall's FAR side in partial path: Fortress
204,100,214; Airfield203,100,213; Carrier202,103,213; Midway202,103,213.
Other profiles also get far-side partial points with flyingfalse. Changing0->3
step alone does NOT change flying=true endpoints. Thus flying eligibility can
change search result, but does NOT yet explain NF08 vs NF10's different actual
results. Next live differential should compare NF08 and NF10 initial bbox position,
goal, actual host speed and each16-tick replan (same bbox is the useful control).

Independent LEVITATION remains OPEN. Legacy BasicMount extends BasicSummon extends
EntityCreature; canFly reads its OWN potion. Modern mount is a non-Living Entity,
so no independent vanilla potion state exists. BasicSummon's host-only setBuffMap
comment refers to CUSTOM buffs, not potion effects! Host proxy would change
semantics. Trial LivingEntity adaptation was removed before checks: faithful
port also needs attributes/damage/equipment/tick audit; old attackEntityFrom has
nontrivial immunities/dodge. Do not silently inherit new health/damage behavior.
Need lead decision on a focused faithful living-mount API port rather than host
effect sharing. Full per-profile report download returned curl HTTP401; supplied
local overflow contains only summary/attachment URLs. Raw latest paths not read.

Node evidence:/home/ubuntu/shincolle-factory-visual.log. Private harness:
ShincolleVisualAgent.java / QAPathProbe.java under /home/ubuntu (NOT committed).
Shimakaze+0.14 was independently confirmed by batchD; protrusion stays unchanged.

## Crane empty-buffer waits: verified historical migration regression

Important: historical0aad501 InventoryHelper.checkFluidContainer(IInventory,...)
only short-circuits NULL INVENTORY; NULL target fluid is a wildcard and still
scans buckets. Its Crane callers pass null when tank empty. Current read-only
src/main has a later renamed helper that also short-circuits null target fluid;
do not misclassify historical behavior from that newer helper. src/main untouched.

Modern completion skipped every container if tank empty. Removed shortcut and
used container's own fluid for wildcard fill simulation. UntilFull waits for
empty/part-filled container; UntilEmpty waits for drainable filled container,
even if no pump/work happened. Caller checkCraneEnding is unconditional after
rate-gated transfer, so no-pump path reaches this predicate. Existing nonempty
buffer behavior is unchanged.

CraneFluidWaitGameTests invokes actual production ending dispatch for empty
bucket/full wait, partial ship tank/full wait, full bucket/full finish, filled
bucket/empty wait with no work, empty bucket/empty finish, nonfluid and empty
inventory completion. No full-route client replay yet; user-facing changes are
visually unverified. Existing crane transfer/cycle tests retained.

## Audio / ring closed by lead's later batchF evidence

Lead confirms saturation ONLY daytime frozen1000, stops1001: legacy-identical;
skip further task3. F1 ring water flight passes live. RingFly explicitly stays
legacy behavior. No ring/audio source change.

Supplementary already-completed normal run:6 real timekeepers, UIDs146..151,
alive, configtrue, daylight advances6031->13957, frozenfalse throughout endpoint
checks; wall1795339290639->1795339686913 (396.274s). Master/neutral/hostile/player
volumes1, ship/timekeep1. OpenAL Null Output (VM software backend).
0 pool warnings,0 failed handles,0 unknown sounds,0 Stop-invalid-name in session.
Log:/home/ubuntu/shincolle-normal-audio-session.log (baseline276 lines).
No subjective physical audio claimed. No new screenshots; pointer/crane client
interaction remains visually unverified.
