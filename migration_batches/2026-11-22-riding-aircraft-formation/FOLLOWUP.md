# Live follow-up checkpoint (08:48)

Forge HEAD7035885 (offset fix local, push held); Neo HEAD5eb457ff4e9d535510b27c6a49e483c4781f8c44
merges it, not pushed yet. Offset production/test changes committed on both.
Neo gate build+370/370 passed (cart-offset-neo-gametest.log49s); Forge offset
build passed but intermittent existing hostile range/Raiden tests prevented a
green full gate. Detached unchanged4d7e58b362/362 passes, NOT proof failures
preexisting. New sources below have NOT been compiled or tested yet.

## Completed factory capture and corrected cart

Private client PID236617, window58720265, gameDir/home/ubuntu/shincolle-queue-visual.
Never close original window56623113. Home agent source ShincolleVisualAgent.java,
reload agent jar shincolle-visual-reload-agent.jar; launcher `java --add-modules
jdk.attach -cp /home/ubuntu ShincolleVisualAgent 236617 MODE` with JDK21. Compile
both home agent .java files with javac after edits. Home factory log authoritative.
Use tools computer to target own active window (verified PID236617).

Valid PNGs now under /home/ubuntu/screenshots:
- shincolle-121-factory-carrier-glass.png: real carrier50 AIR_HEAVY launch, shown
  approaching/routing on near side. At30ticks native fullBBox.maxZ208==glass
  face208; at40ticks backs/turns on near side maxZ207.531. Entity1592.
- shincolle-121-factory-northern-fort-glass.png: NorthernHime33 factory launch,
  full bbox.maxZ208 exactly at glass, continuing positive-Z intent, actor1593.
- shincolle-121-shimakaze-cart-normal-legacy-offset.png
- shincolle-121-shimakaze-cart-bored-legacy-offset.png
  Actual rail at215,100,201; cart1594 Y100.0625, Shimakaze1595 Y100.2025 diff.14.
  Bored still protrudes white limbs, intentionally no geometry change without
  legacy divergence. Normal front/side image corrected; both show current pose.
  Numbers and source chain in cart-offset-result.md; must append final evidence.

QA camera WAS falling after each teleport (creative but not flying), causing bad
early frames. Fixed HOME HARNESS ONLY `fly` mode sets flying/mayfly/NoGravity on
private QA player. All four final PNGs captured AFTER that fix; camera stationary.
Current camera217.9,100,204 yaw136 pitch17, normal cart pose, world frozen.
Factory wall scene includes plane/fort and mount1591+Airfieldhost1590 at202,101,201.
Mount still stops; remaining stationary-owner mount wall photo deferred to D4.
`pose0`/`pose4` mode changes pose and steps1, `inspect` logs native metadata;
`rail` setup already done; `client` logs matchingIDs1587..1595 client ticks/pos.

## Newly queued / WIP sources (Forge only)

E3 report pointer-crane-protocol.md. Uncommitted:
- PointerItem.pickCommandTarget(Player): legacy64-block entity ray, occluded by
  collider, excluding own vehicle/host, pickable/non-invulnerable entities.
- PointerClientEvents.handlePointerLeftClick Empty/Block now ray-picks BEFORE
  modifier-only empty handling and sends appended POINTER_LEFT_CLICK enum.
- PointerCommandEvents.handleRayHit bounded64 server validates held pointer and
  mode; reuses existing owned membership/focus logic. Near native attacks still
  use existing event. Common LeftClickBlock now cancels SERVER only so it cannot
  pre-cancel client subscriber and skip ray callback.
- GameplayCommandHandler new enum dispatch. Enum appended (old ordinals stable).
- MorphAndTeamParityGameTests new method pointerLongRaySelectsOwnedTransportAndDispatchesToCrane:
  actual shared ray at20 blocks, real server packet handler, Wa18 and crane,
  empty/occluded/foreign/missing/out-of-range checks. Uses y150 to avoid existing
  y100 family fixtures. ShipCommandService.handle returns boolean (already fixed
  mistaken initial enum expectation). commandGuardBlock sets RouteNode for crane.
Verified legacy PointerItem left ray64 + add/focus, getMouseOverEntity clip before
entity. Modern only native AttackEntityEvent handled selection; Empty/Block had
modifiers only. Source disproved idea canceled AttackEntityEvent blocks vanilla
packet: Neo MultiPlayerGameMode sends attack packet BEFORE player.attack.
User exact leftclick distance NOT established: report root as missing long-range
selection regression, not a confirmed cause for an unobserved close-range click.

D4 mount-waypoint-protocol.md. Uncommitted:
- LegacyShipMountEntity.moveToward checks final stopDistance against DESTINATION,
  not intermediate node, and only zero-checks intermediate offset.
- LegacyShipObstaclePath range32->70 (legacy ShipPathNavigate uses70 for
  IShipAttackBase,48 otherwise; IShipCannonAttack extendsIShipAttackBase).
- LegacyAircraftBlockCollisionGameTests adds actual factory mount.tick with real
  host/passenger all8 eggs, finite glass route, open motion, orphan cleanup. Old
  D3 helper-driver test retained unchanged. Not run yet! Root candidate evidence:
  waypoint helper advances only within.5 while mount command stop.75 / target
  stop1.5 deadlocks before intermediate point; real tick test now exercises it.
Floating gate audit unfinished: all legacy mounts inherit canFly=mount's own
LEVITATION. Modern mount extends Entity, no independent potion API. Do NOT invent
host delegation without checking subclasses/delegation. Need inspect legacy
entity/mounts/*.java canFly/LEVITATION (wrong singular directory glob failed;
correct plural path listed in last output). New helper still uses true; can't
claim exact floating parity yet. D4 heavy elevated-target audit started but no
verdict: EntityAirplaneT->Takoyaki, old AI range16 sight+dist(targetY+2), random
orbit(12,4), modern range16/sight/3D has blended direct+obstacle movement. Need
disprove regression before calling legacy-identical; no speculative fix made.

## F3 new queue, NOT implemented

ring-cache-audio-f3-protocol.md and report path in task list.
- RingFly removal bug reachable: TeitokuEvents.onRingScan active fallback retains
  old active when stack empty; resets flying but NEVER data.setRingFlying(false).
  Fix flag cleanup + tests. Grant already sends ServerPlayer.onUpdateAbilities,
  so do NOT blindly claim missing server abilities packet or grant mayfly.
  MarriageRingItem.inventoryTick runs both sides. Curios tickOptional is SERVER
  only. RingClientEvents currently fog only. Vanilla LocalPlayer.aiStep clears
  flying onGround AFTER super.aiStep/inventory tick; vertical controls use
  abilities.flying (NOT mayFly). Packet listener sets server flying to
  client-flying &&player.mayFly; legacy may have same check. Legacy ring grants
  ONLY isFlying, not allowFlying; old tests assert !mayfly. Need actual client
  counter-check / ordering comparison before changing flight semantics. Candidate
  client END tick using same grant may restore both-side Curios grant, but NOT
  verified, no ring source changed yet. Old-save mayfly leave as-is.
- /ship get already checks cached dimension before live/spawn. Suspect stale
  origin cache on CHANGED_DIMENSION remove before first destination tick. Modern
  remove caches origin, no onAdded callback. Neo Entity.onAddedToLevel is called
  synchronously in ServerLevel.addEntity AFTER successful manager.addNewEntity,
  even if destination not ticking. Forge equivalent onAddedToWorld likely can
  update cache immediately; verify API and transfer order. BUT legacy CacheDataShip
  also uses ship.world.provider (not early-mutated entity.dimension); setDead
  caches it, and ServerProxy.checkShipIsDupe ensures uniqueness. Need compare
  transfer/dupe detection, reproduce actual transfer before calling regression.
  Do NOT call just stale-origin cache a confirmed modern-only regression.
- F3 raw logs/world paths /home/ubuntu/Documents/batch-f-third and batch-f-retest/server
  do NOT exist here; attachment dir only report. Cannot verify timeFrozen/time6000
  for 445 warnings from report alone. Old exact-hour behavior known but reachability
  for THIS session unverified; leave audio open and request rawtime/log state.

Next: finish focused source batches then ONE final build/full GT each branch,
fix actual failures without weakening assertions; commit localForge, normal merge
Neo/adapt APIs and gate, restart private client only if needed for new schema,
capture remaining stationary mount scene. Update batch results/core docs/matrices,
normal Neo push only. No src/main edits, no Forge push, no force pushes, no PR10 CI.

## Latest08:53 native ring counter-check (no production ring/cache changes yet)

Home agent now has `ring` and `ring-meta` modes. `ring` builds a private7x7x4
water pool at228..234/100..103/199..205, floor99; gives active custom-component
ring slot0, marriage12, sets survival via ServerPlayer.setGameMode (creative then
survival to force proper client game-event sync), clears mayfly/flying/NoGravity,
teleports bottom231.5,100.01,202.5 and UNFREEZES world. `ring-meta` logs SERVER and
CLIENT pos, creative/spectator, flying/mayfly and cap flags. Current newest ring
setup compilation/attach background shell660? actual last exec shell returned ID
in next tool output; use get_output if needed. World will remain running now.

Initial inventory baseline with marriage12/default threshold6 reproduced the
server `flying=false RingFly=true mayfly=false` snapshot BUT CLIENT flying=true.
Holding space (xdotool keydown --window58720265 space; keyup ALWAYS afterward)
raised actual server+client Y100.0->103.930774925. Thus server flying0 alone is NOT
proof of no water flight. That initial harness changed ServerPlayerGameMode
directly and client HUD still creative, so do not claim full survival proof yet;
just corrected harness to ServerPlayer.setGameMode wrapper (sends client mode
event). Repeat baseline once using corrected mode, metadata creative=false on
both sides, space held2s then released; inventory F3 failure may not reproduce.
DO NOT grant mayfly speculatively: modern vertical input only requires flying,
NOT mayFly; legacy1.12 PlayerSP has same onGround flight reset after super update
as modern. Native client mayFly packet validation is also old-like, not causal
evidence. Server onUpdateAbilities already exists at grant. `getEffectiveRingAbility`
already uses synced clientRingAbility; sysTag RingAbility included in sync, so
server/client threshold mismatch hypothesis weakened (not settled without report
actual logs). Client default inventory effects execute both sides.

More concrete Curios gap: tickOptionalCuriosMarriageRing currently only invoked
server at END event; legacy MarriageRing.onWornTick directly calls onUpdate BOTH
sides. Could restore client Curios tick by changing private helper param from
ServerPlayer to Player/public wrapper and invoking at client tick END; test/VM
before claiming. Need see whether current private client actually has Curios9.6
and ring slot configured; can read runtime API and equip using official handler.
No Curios baseline run yet. Ring removal flag cleanup remains definite source bug.

08:55 update: all THREE native cases now ran correctly with ServerPlayer.setGameMode
sync, BOTH server/client creative=false spectator=false, mayfly=false, NoGravity=false:
inventory marriage12/default6 at Y100.01->103.173393725; Curios-only marriage12/default6
Y100->103.237158632; Curios-only marriage0/server ringAbility[0,0,30,20,0] + full sync
Y100->104.072178047. All used actual heldSpace, original source (no ring patch).
Server flying=false snapshots coexist with real ascent and client flying=true.
So reported actual no-flight failure NOT REPRODUCED; don't invent missing sync or
grant mayfly. Need discriminate configured scenario rather than broad production
flight patch. RingFly removal definite bug still pending. NativeMeta/source logs
at/home/ubuntu/shincolle-factory-visual.log; valid survival screenshot copied to
/home/ubuntu/screenshots/shincolle-121-ring-inventory-survival-ascent.png. Current
QA world UNFROZEN, survival player at pool surface, Curios ring equipped, config
testoverride zeros and marriage0. Home modes ring-curios/ring-zero added.

Forge new-source batch compile first failed wrong registry BLOCKCRANE; corrected
test to actual BLOCK_CRANE (no assertion change). Second build+GT shellc13f4f
finished exit1; read /home/ubuntu/shincolle-followup-forge-gates.log for GT failure.
Build+GT includes364 expected (362+newPointer+newActualMount) before ring changes.

IMPORTANT09:03 correction overriding all earlier tentative RingFly-bug notes:
legacy EventHandler.java735-767 clears ONLY capabilities.isFlying on ring loss,
does NOT setRingFlying(false) or resetRingActive. CapaTeitoku.java807-820 setters
are plain assignments, no hidden flag cleanup. Existing parity test
GameplayParityGameTests.ringScanAndLiquidDigMatchLegacy explicitly asserts stale
RingFly=true after removal/rightclick. Trial cleanup made that test fail. Restored
all ring source/tests to HEAD (git diff empty for those two files); do NOT weaken
legacy assertion. RingFly persistence is LEGACY-IDENTICAL. Need lead/user decision
whether new explicit cleanup overrides strict no-new-behavior rule, before fixing.

ForgePointer GT still failed ray after aim; added real transport.tick for UID /
properly initialized dimensions and detailed eye/look/box/pick/invuln/tracked/hit
diagnostic, shellf1d023 running final3 log. If same ray fails THIRD time, stop loop
and escalate diagnostic as required. No assertion weakened. FakePlayer source
checked47.4.18 sources jar; does NOT override eyes/look (only tick/network/no harm),
so fake eye override candidate disproved. NativeGetEntity tracking/unloaded target
chunk still possible, will be shown diagnostic. Registry BLOCK_CRANE corrected.
Client block-hook now receiveCanceled=true while common cancellation restored
BOTH sides (keeps legacy no-break, avoids callback registration-order issue).
ModernRing no edits remain;364 tests again. Latest final3 after InputTest changes.

F3 raw logs still inaccessible: standard Neo run/logs/latest.log is ONLY06:40
headless GameTest log, NOT client's08:08–08:34 F3 session. Attachments onlyreport.
Do NOT use standard run log as evidence for445 audio warnings or infer frozenhour.

09:12 source checkpoint: final pointer fixture tick+center aim and additional
actual Airfield mount-host packet case passed; final Forge build/full364/364 in
shincolle-followup-forge-release.log1m27. Ring source/tests identical HEAD; see
f3-ring-decision.md (legacy retains RingFly, needs behavior-change permission).
About to commit Forge locally then normal-merge/gate/push Neo. No source input
changed after final gate; only result/core/matrix docs. HomeAgent updated with
freeze/clean-ring/view:x:y:z:yaw:pitch/step:n/reload-followup; private world now
frozen, ring+overrides cleaned, creative camera restored. NativeActorIDs need fresh
setup after Neo checked mount/path hot-swap; then fly/view and fixed-camera steps.

Cache audit update: legacy CacheDataShip uses ship.world.provider dimension, so
the idea early vanilla entity.dimension mutation fixes old cache is DISPROVED.
Legacy ServerProxy.checkShipIsDupe does eventually remove older duplicate ONLY
when BOTH entities ticksExisted>200 and alive. Modern lacks that exact guard;
cannot treat initial legacy cache window as modern-only without actual comparison.
Neo ServerLevel.addEntity synchronously invokes onAddedToLevel AFTER successful
manager.addNewEntity (source checked); override could refresh destination cache
without waiting first entity tick, but production not edited pending regression
decision/repro. Forge counterpart onAddedToWorld must be checked/adapted.
