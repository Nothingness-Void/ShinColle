# PR17/18 review follow-ups

Local and remote starting HEADs verified clean and matching:
Forge e34f5de0f469d693206827d28674002114e61ea3,
Neo ae383c420a05e492886042d0c73a516f8feacf4e.

## Finding 1 — real

Historical 0aad501 friendly EntityDestroyerAkatsuki:157, Hibiki:127,
Ikazuchi:143 and Inazuma:149 call type20 with zero velocities.
ParticleHelper:301–305 spawns three ordinary SMOKE_NORMAL particles through
world.spawnParticle, at y+i*0.1. Their hostile counterparts call type43 with
scale1+0.8*scaleLevel (AkatsukiMob:110, HibikiMob:107, IkazuchiMob:108,
InazumaMob:109). This disproves the previous audit's shared-type43 claim.

Modern client tick dispatch reaches the shared 51–54 normalized-class table
after unchanged model/rider/fuel gates. It had incorrectly selected the
limiter-overriding custom smoke for friendly variants too. Split ordinary
SMOKE_CHIMNEY_NORMAL from existing type43; use vanilla SMOKE without bypass,
retain type43 and its scale for hostile variants. No gate/position change.
New focused coverage checks all four variants and scale levels0–3, vanilla
limiter flag, triple count, spacing, velocity and empty spec. Corrected the
older friendly-kind expectation against the historical source.

## Finding 2 — real; the caller table, not scaled(), was wrong

Historical 0aad501 EntityBBHieiMob:109–116 uses literal rotated X=-0.6,
Y=1.17, particle scale1, independent of encounter scale. Its constructor
does assign scaled smokeX1/smokeY1 fields, but the active client callback
does not read them. Modern class61 used those scaled anchor values plus
particle scale1+level. Restore the callback's fixed anchor and scale;
do not alter the general LegacyShipSmoke.scaled helper.

Hostile chimney audit, historical spawnAttackParticleAt callbacks:

| Family | Legacy particle scale | Evidence |
| --- | --- | --- |
| Nagato | 1+level | EntityBattleshipNGTMob:121 |
| Yamato | 1+level | EntityBattleshipYMTMob:115 |
| Kongou, both chimneys | 1+level | EntityBBKongouMob:125,127 |
| Hiei | 1 | EntityBBHieiMob:116; literal anchor too |
| Haruna | 1+level | EntityBBHarunaMob:117 |
| Kirishima | 1+level | EntityBBKirishimaMob:117 |
| Four hostile destroyers | 1+0.8*level | Finding1 callbacks above |

All other audited hostile particle-scale mappings already match and remain
unchanged. New tests cover each battleship family at levels0–3, both Kongou
points, Hiei's fixed anchor, custom options/limiter, and out-of-range Hiei
inputs. The older Hiei assertion was disproven by the actual legacy callback
and corrected rather than preserving the copied unused-field expectation.

## Commits and final gates

| Finding | Forge | Neo |
| --- | --- | --- |
| 1 | b2003d110a3cec25f245548d021388b4a1cbb15c | e224e8747562a84267d3cfab03fe15be7746123d |
| 2 | 35c89367954cac7443d9342e93a93377676b01ab | d4894a9c147afe48350c7d36618a92f1406e5ef1 |

Full requested build + GameTests after both fixes, JDK17/21:
Forge all381 passed, BUILD SUCCESSFUL in1m4s;
Neo all390 passed, BUILD SUCCESSFUL in1m2s.
[Exact excerpts](results/review-gates.txt).
No source/resource input changed after these passing gates.
One push per branch after the separate fix commits and documentation commit.

The first Forge full run failed only craneRouteTransfersFilteredItemsBetweenChestAndShip:
crane load row should move only matching chest items into ship cargo; actual=null.
The exact assertion was also recorded before this review in
/home/ubuntu/shincolle-pointer-owner-forge-final.log. No crane implementation
or crane fixture change was made. Production smoke dispatch is client-gated
and the new tests manipulate only pure emission data; no direct server-crane
production path invokes these changes. A single full rerun passed all381.
This does not establish the precise crane flake cause or claim to fix it.

Client visuals were not rechecked for this review; original smoke screenshots
predate these per-ship corrections and do not verify their limiter/scale
appearance. No other production scope, legacy, build/CI or version changes.
