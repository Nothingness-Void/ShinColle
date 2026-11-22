# F3 ring: source verdict and native counter-check

## Water flight

Legacy MarriageRing.onUpdate explicitly runs BOTH sides and grants only
capabilities.isFlying, never allowFlying. onWornTick delegates to that same
method. Modern MarriageRingItem already sends ServerPlayer.onUpdateAbilities on
grant; there is no missing server abilities packet in this code path.

Native private integrated-client checks (original ring source, survival on both
sides, mayfly=false, NoGravity=false) did not reproduce failure:

| Equipment/config | Actual server+client Y after held Space |
| --- | --- |
| Inventory, marriage12/default threshold6 | 100.01 -> 103.173393725 |
| Curios-only, marriage12/default threshold6 | 100 -> 103.237158632 |
| Curios-only, marriage0/synced threshold0 | 100 -> 104.072178047 |

Server snapshots can simultaneously report flying=false while client reports
flying=true and the player actually ascends. Thus server NBT flying=0 alone does
not establish no water flight. These checks do NOT exclude a dedicated-server,
different-client-configuration or different input/tick-state failure. No mayfly
grant or speculative client tick workaround was added.

Evidence: /home/ubuntu/shincolle-factory-visual.log and
/home/ubuntu/screenshots/shincolle-121-ring-inventory-survival-ascent.png.

## RingFly removal

Legacy EventHandler.java735-767 clears capabilities.isFlying on loss and assigns
hasRing, but does NOT clear RingFly or inactive state for a missing stack.
CapaTeitoku.java807-820 has plain-assignment setters: no hidden cleanup.
Modern GameplayParityGameTests.ringScanAndLiquidDigMatchLegacy already explicitly
asserts this retained RingFly behavior. The stale flag is legacy-identical, not a
verified migration regression. Trial cleanup was removed and assertions retained.

**Decision required:** does the explicit new cleanup request authorize a behavior
change despite the standing rule to leave legacy-identical behavior unchanged?
Until clarified, no ring source/tests changed and old-save mayfly remains untouched.

## Other F3 investigations

Unloaded-Nether recovery remains open. Modern remove(CHANGED_DIMENSION) writes the
origin cache, command validates cached dimension then respawns when the live lookup
fails; no destination-added cache callback exists. Legacy setDead/CacheDataShip
also use the source world, and cache updates wait for ticking. However legacy
ServerProxy.checkShipIsDupe removes an older duplicate only when BOTH copies are
alive and older than200 ticks. The unloaded-destination ticking behavior and exact
duration of the reported duplicate were not verified. Do not label the initial
window a confirmed modern-only regression or change command recovery speculatively.

Audio remains unclassified for THIS F3 fixture. The attachment contains only the
report; its referenced raw logs/world are unavailable here. Standard Neo run/logs/
latest.log is a06:40 headless GameTest log, not the08:08–08:34 client session.
Previous frozen exact-hour behavior is proven, but its reachability in this F3
session is unverified. Need the F3 level.dat/time-rule snapshot and matching log.
