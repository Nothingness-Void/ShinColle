# 08:00 queue result — source gates green, visuals incomplete

## Scope and decisions

1. D3: [verified fixture/barrier chain](D3-resolution.md). Neo2200559 pushed.
   No production far-coordinate precision claim or weakened assertions.
2. B2: [actual kill, entity-region reload and legacy comparison](../2026-11-22-inventory-book-layout/round2-result.md).
   LegacyShipEntity restores saved Health after variant reconstruction; corpses
   remain death-locked. LegacyShipCombatHelper caps overflow at Float.MAX_VALUE
   to avoid reachable Neo Infinity-Infinity damage-container NaN during /kill.
   Tests cover friendly/hostile corpses, injured survivors, missing health,
   death-time lock, finite damage and identical normal formula/random draw.
   No registry/ticket respawn dispatch found; no changes to teams/tickets/eggs.
3. [Dynamic line-abreast result](line-abreast-result.md): all6 slots, pickup off,
   targets/arrival/path fields match independent old formula every tick. Original
   live stall NOT reproduced; no production movement change. Fixture ownership
   override/isolated goal/manual native movement are explicit limitations.
4. [OpenAL audit](openal-result.md): no mod stop/custom SoundInstance path;
   vanilla/unattributed, not an attributed vanilla bug. No audio workaround.
5. Old stale mayfly explicitly left unchanged as requested.

## Final source checks

Forge JDK17: bash ./gradlew --no-daemon -I
/home/ubuntu/shincolle-curios-forge.init.gradle build, then runGameTestServer.
Logs /home/ubuntu/shincolle-line-clock-forge-{build,gametest}.log:

```text
BUILD SUCCESSFUL in 19s
========= 362 GAME TESTS COMPLETE ======================
All 362 required tests passed :)
BUILD SUCCESSFUL in 1m 33s
```

Neo JDK21: ./gradlew --no-daemon -I
/home/ubuntu/shincolle-queue-gametest.init.gradle build, then runGameTestServer.
Logs /home/ubuntu/shincolle-queue-final-neo-{build,gametest}.log:

```text
BUILD SUCCESSFUL in 17s
========= 370 GAME TESTS COMPLETE IN 37.12 s ======================
All 370 required tests passed :)
BUILD SUCCESSFUL in 1m 10s
```

An intermediate earlier full run failed an unchanged crane UID lookup. Final
full suites pass without any crane changes; that intermediate cause was not
established. No re-run after documentation-only edits (source inputs unchanged).

Forge source44bfc38 local; remote remains339857f under explicit push hold.
Neo source4302073 pushed, includes Forge44bfc38 by ordinary merge. Subsequent
commits only synchronize these evidence documents. No force-push/src/main edits.
PR10 CI failure notification was NOT investigated: CI loop not delegated.

## VM evidence — NOT all requested cases verified

Private Neo client launched from b778b26 production source (later changes only
add trace GameTest/API imports). World: /home/ubuntu/shincolle-queue-visual/saves/
GUI Audio Regression. Existing other client56623113 untouched.

Actual empty-hand right-click of owned Airfield Hime mount succeeded. Native
`execute on vehicle if entity @s[type=shincolle:legacy_ship_mount] run say QA-mount-linked`
returned `[entity.shincolle.legacy_ship_mount] QA-mount-linked` at07:58:13.
Screenshot /home/ubuntu/screenshots/shincolle-121-airfield-player-mounted.png
shows both player and Hime aboard. Not evidence of wall collision.

Diagnostic cart frames only:
- /home/ubuntu/screenshots/shincolle-121-shimakaze-cart-normal-diagnostic.png
- /home/ubuntu/screenshots/shincolle-121-shimakaze-cart-bored-diagnostic.png

These are NOT passing pose checks: boots protrude and most body is concealed;
requested server NBT emotion0/4 is not independent proof of final client pose.
No new offset change without a verified legacy divergence.

Aircraft/fortress client setup failed to preserve entities after stepping40ticks
(native `data get ... Pos`: No entity was found). Stopped repeated setup attempts;
this is an invalid visual fixture, not proof of a production pathing defect.
QA IDs initially missing due unsupported2023 egg; changed to registered2053 via
a temporary friendly Hime ID carrier. Later IDs exist59/60/103 but entities still
gone; their full create-to-removal chain was NOT traced. No causal claim made.
Mount moved away when owner camera was repositioned, so no valid wall snapshot.
Native mouse-control trial did not keep its heading toward wall; not a collision
check. Thus aircraft/fortress/mount GLASS screenshots remain OUTSTANDING.

Private QA datapack wallqa contains setup/spawn/complete functions, not shipped.
Log /home/ubuntu/shincolle-queue-wall-client.log. Client remains open and frozen
with player aboard at approximately668,343,-35, not the arena. Recommend replacing
this NBT visual fixture with verified factory-created owner/target summons, then
capture only requested wall/pose cases. Do not classify the missing images as
passing from the build/GameTests or incidental renders.
