# E3/D4 follow-up result

## Pointer/crane

Legacy PointerItem left-click entity ray reaches64 blocks, adds/focuses an owned
ship, and resolves a mount to its host. The port only selected through native
AttackEntityEvent, while Empty/Block callbacks had modifier handling but no long
ray selection. Restore the shared solid-occluded64-block ray and append a server
command (old enum ordinals unchanged), reusing existing owner/team/focus handling.
The server requires a held non-caress pointer and rejects targets beyond64 blocks.
Block callback receives canceled events without removing legacy no-break behavior.

The new GameTest uses a normally tick-initialized owned TransportWa20 blocks away,
the actual command handler and actual crane route-node dispatch. It also covers
empty fleet/ray, solid occlusion, foreign/missing/forged out-of-range targets and an
actual Airfield mount hit focusing its real host. No assertion was weakened. The
initial fixture missed; center aim alone was insufficient. Normal tick initialization
plus center aim passes; the exact initial miss was not isolated further.

This proves a missing legacy long-range selection path, NOT that the user had
actually clicked a ship at that distance: the original report did not independently
verify a focus click. Fleet membership alone remains different from selection.
Pointer client interaction remains visually unverified.

## Mount intermediate waypoint stall

The helper advances an intermediate path node within0.5 blocks, but mount moveToward
previously applied its final command0.75/target1.5 arrival radius to that intermediate
point. It could stop before the helper ever advanced the node. Apply final arrival
radius to the actual destination only. Restore70 search distance for these attack
entities (legacy ShipPathNavigate70 for IShipAttackBase,48 otherwise), not the port32.

The new test ticks each actual factory-created mount with its registered host as a
real passenger, through the original finite glass fixture, then checks continued
progress, open movement, no intersection and orphan cleanup. All8 profiles pass.
The earlier helper-only route test remains unchanged and passes. The range change
matches legacy, but has NOT been established as the cause of the reported stop;
the waypoint radius mismatch is independently reachable.

Floating audit limitation: legacy BasicEntityMount.canFly reads its own LEVITATION
effect; no override/copy in the8 mount classes was found. Current mount is a
non-Living Entity and has no independent potion API; aerial helper currently always
allows flying. Do not claim full independent floating-effect parity or silently
invent host delegation. This broader port gap remains open; no architecture change
was made as part of the proven waypoint continuation correction.

## Gates and submission

Forge JDK17, equivalent bash wrapper, external Curios init, final build and full
GameTests in one invocation, after final mount-host case:

```
========= 364 GAME TESTS COMPLETE ======================
All 364 required tests passed :)
BUILD SUCCESSFUL in 1m 27s
15 actionable tasks: 7 executed, 8 up-to-date
```

Log:/home/ubuntu/shincolle-followup-forge-release.log. Forge push hold remains.
Neo merge/API/gate and stationary-owner mount capture follow this source checkpoint.
Unchanged ring behavior and F3 blockers: [ring decision](f3-ring-decision.md).
Offset numeric proof: [cart result](cart-offset-result.md). Full recovery detail:
[FOLLOWUP](FOLLOWUP.md).
