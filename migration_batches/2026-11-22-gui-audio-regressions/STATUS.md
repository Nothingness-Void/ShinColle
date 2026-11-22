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
changed. NeoForge gates and requested scale-8 screenshots follow after merge.
