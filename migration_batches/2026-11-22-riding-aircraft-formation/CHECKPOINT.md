# Queue checkpoint08:00 — gates green, VM partial

Current recovery entry: [queue-final-result.md](queue-final-result.md).
Forge push hold remains in effect. No current source/build/GameTest blocker.
Remaining: three wall scenes and acceptable Shimakaze normal/bored visual checks;
original live line-abreast stall not reproduced. Actual player right-click mount
verified. Private QA client remains open/frozen; do not close window56623113.

## Historical06:53 checkpoint (superseded, preserved investigation trail)

> Superseded07:09: see D3-resolution.md;365/365 now pass after correcting the
> too-small enclosed fixture, without relocating or weakening the original test.

## Current source/history
- Forge branch devin/parity-fix-integration: d375353c06f3179b856c9fbda0e358228199743f pushed, following603a32d.
- Neo devin/mc-1.21.1-neoforge: local merge c6fc3fd then bb8eb5882618830eb82ccb04d7acb9b5ed95d317; not pushed. Forge d375353 is an ancestor. No merge in progress.
- No src/main edits. Only checkpoint documentation remains dirty after source commits.

## Last required gates
Forge JDK17, bash ./gradlew because wrapper is not executable, external init
/home/ubuntu/shincolle-curios-forge.init.gradle for mapped Curios5.14.1:
build SUCCESSFUL in12s, 8 tasks5 executed3 up-to-date;
all357 required GT passed; SUCCESSFUL in54s, 11 tasks3 executed8 up-to-date.
Logs /home/ubuntu/shincolle-queue-forge-round2-{build,gametest}.log.

Neo JDK21 with external init /home/ubuntu/shincolle-queue-gametest.init.gradle:
build SUCCESSFUL in9s, 5 tasks3 executed2 up-to-date;
365 GAME TESTS COMPLETE IN17.70s, 1 required failure; BUILD FAILED in38s.
Logs /home/ubuntu/shincolle-queue-neo-round2-{build,gametest}.log.
Private gameDir /home/ubuntu/shincolle-queue-gametest avoids another old client
rewriting new config keys; server.properties and run/gameteststructures copied
there. Real Curios9.6 jar and configured ring slot present. Do not close someone
else's original client window56623113.

## Blocking D3 diagnostic
floatingfortandalleightmountstyleskeepsolidsandobstacleroutes failed at
-8040284,-60,-10361191:
finite wall must have a usable solid-aware route: entity.shincolle.legacy_ship_mount
(-8040282.949999988,-55.0,-1.0361182949999988E7)
-> (3.449999988079071,0.0,5.449999988079071).

Three Neo runs reached this route assertion; stop repeating speculative fixes.
The shared helper was changed Forge-first from feet-ray to block-only swept
AABB (no entity collision change). D3 fixtures now register real hosts/targets
and assert host lookup, exercising actual dimensions for all8 variants. Failure
messages shortened to avoid 1.21 GameTest's1024-character lectern-page limit.
Forge passes; Neo now stalls at a horizontal corner rather than the previous
underside. Root cause remains unverified; do not weaken collision/route assertions.

Vanilla1.21.1 GameTestServer.startTests picks random X/Z in±14999992,Y=-59,
regardless of spawnPos; Forge near-origin pass is not proof against far-coordinate
path failure. ShipPathPoint distances subtract ints before float conversion and
ShipPath waypoint world coordinates use doubles: those two candidates checked,
not an explanation. Legacy int-target findPath uses x/z+0.5F; endpoint precision
exists but is NOT a verified cause. Next action: inspect failing style, actual
path nodes/current index/final partial-path point, compare equivalent near/far
geometry before changing helper or legacy tolerances. No additional speculative
path edit was made after the third Neo failure.

## New B round2
/kill ships reappear after save/reload is OPEN, new protocol:
../2026-11-22-inventory-book-layout/round2-protocol.md (currently Forge only).
Initial modern-source observation: readAdditionalSaveData ends in
refreshFromVariant(true); clampPreservedHealth returns0 for health0/death-locked,
and tick enforces health0 if deathTime>0. Thus an unconditional attribute-refresh
revival is not established. No kill/reload reproduction, legacy verdict,
team/ticket/recovery reachability trace, production change or new GT yet.

## Remaining visuals and investigative limits
Stable waypoint shots exist:
- /home/ubuntu/screenshots/shincolle-121-queue-waypoint-stone.png
- /home/ubuntu/screenshots/shincolle-121-queue-waypoint-wrench.png
Still missing aircraft/fort/all8mount wall, actual player right-click mounting,
Shimakaze minecart screenshots. No new client launched after last Neo edits.
Private copied visual world and init /home/ubuntu/shincolle-queue-visual.init.gradle
are available; unfinished QA-only datapack queue_fixture is not production code.
Its startup JVM enables localhost-only JDWP57111 for binding real runtime host
IDs if needed; remove it if not using debugger. Private QA world has selected
morph profile0 but inactive runtime, solely for optional legacy-gate checking.
No visual morph verdict was made.

Current ring code never grants mayfly, clears ring-driven flying on disable and
removal, preserves preexisting mayfly. Stale grants in old player saves have no
proven ownership marker and are NOT claimed repaired. Actual line-abreast stall
cause and single OpenAL Stop remain OPEN; no invented fixes. Morph unlock-only
does not satisfy legacy active-morph gate. Full recipe/cast audits and per-item
table remain in STATUS.md. New PR10 CI-failure notification was not investigated:
CI loop not delegated.

## Next
D3 diagnosis first; B2 kill/reload investigation second; final changed-input
gates, missing screenshots, authoritative docs and Neo push only after green.
