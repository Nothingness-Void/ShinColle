# Verification record

## Forge

JDK17, bash ./gradlew --no-daemon build, then runGameTestServer, after the final
source edit. Logs: /home/ubuntu/shincolle-120-gui-audio-{build,gametest}.log.

```text
BUILD SUCCESSFUL in 15s
8 actionable tasks: 5 executed, 3 up-to-date
[04:55:20] [Server thread/INFO] [minecraft/GameTestServer]: ========= 345 GAME TESTS COMPLETE ======================
[04:55:20] [Server thread/INFO] [minecraft/GameTestServer]: All 345 required tests passed :)
BUILD SUCCESSFUL in 48s
11 actionable tasks: 3 executed, 8 up-to-date
```

Directed source guard:

```text
Desk radar text: exact legacy body after API aliases; no truncation/scaling added
Large shipyard: no invented title/status overlays; only legacy material/fuel message priority
Timekeeper: historical per-server-tick exact-hour replay exists, including a frozen hour; unchanged
```

New GameTest covers all 72 large-shipyard message states. Only production files
changed are LargeShipyardScreen and LargeShipyardMenu; no audio/radar behavior
changed.

## NeoForge

Merged Forge source commit e234d5b as 8d4efa3. Only branch-specific test
adaptations: NeoForge GameTest annotations and makeMockPlayer(GameType.CREATIVE).
JDK21 build and full runGameTestServer after the final source edit:

```text
BUILD SUCCESSFUL in 10s
5 actionable tasks: 3 executed, 2 up-to-date
[04:59:54] [Server thread/INFO] [minecraft/GameTestServer]: ========= 353 GAME TESTS COMPLETE IN 23.03 s ======================
[04:59:54] [Server thread/INFO] [minecraft/GameTestServer]: All 353 required tests passed :)
BUILD SUCCESSFUL in 41s
8 actionable tasks: 1 executed, 7 up-to-date
```

Logs: /home/ubuntu/shincolle-121-gui-audio-{build,gametest}.log. The same
directed source guard passed on NeoForge. No src/main changes on either branch.

## Live audio counter-check

The existing New World was locked by another client (PID160452); left it alone.
Created a local isolated snapshot named GUI Audio Regression. The original
reported log is preserved separately; the diagnostic client stdout is
/home/ubuntu/shincolle-121-gui-audio-client.log. Its original spawn was now far
below the ships, so explicitly returned to the reported scene position
(102.5,100,113.5). The frozen daylight rule and 14 enabled timekeepers were
unchanged. Client config timeKeeping=true, volumeTimekeep=volumeShip=1.0.

At identical position, only clock changed, clearing queued voices with
stopsound between controls:

```text
Clock controls: [('[05:02:25]', 6001), ('[05:03:06]', 6000), ('[05:03:30]', 6001)]
6001 control at ships: pool=0, handles=0
6000 exact-hour replay: pool=1025, handles=1025
6001 control after stopsound: pool=0, handles=0
Quiet control started [05:03:30] ; observation 2026-11-22T05:04:04.930734+00:00
```

First quiet interval beside ships starts 05:02:47 (19 seconds); replay lasts
24 seconds; final quiet control lasts 34 seconds. Reproduces the ordinary
per-tick exact-hour legacy dispatch, not an orphaned custom looping instance.
No debouncing/audio behavior change made. This is not an exhaustive sound audit.

## Client GUI evidence and remaining scope

NeoForge targeted visual checks at 2560x1920, GUI scale8 passed (F1 hides only
the unrelated tutorial toast/HUD). Radar still displays full overflowing
Isolated Island Princess names, as legacy does. Large shipyard shows material
counts, build buttons, timer and the legacy Not Enough Fuel message, with no
invented title, structure or core text over the controls.

- /home/ubuntu/screenshots/shincolle-121-radar-scale8-legacy-overflow.png
- /home/ubuntu/screenshots/shincolle-121-large-shipyard-scale8-legacy-layout.png

Visuals were checked on Linux VM, not native Windows or a real 1.12.2 client.
This batch does not certify actual old-save upgrades or multiplayer. User
world/client was not closed; only our isolated diagnostic client is closed.
PR #10 CI-failure notifications were not delegated for investigation here.
