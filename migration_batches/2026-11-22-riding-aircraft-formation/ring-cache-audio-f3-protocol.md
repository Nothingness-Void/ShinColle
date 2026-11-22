# F round 3 ring, cross-dimension recovery and recurring sound pool

Report `/home/ubuntu/attachments/ba166d4e-63a3-46a8-8290-f6dc201fa111/Batch-F-第三轮复测报告.md`.
Scope: legacy MarriageRing inventory/Baubles tick, event tick handler, teitoku
RingFly state and abilities sync; modern MarriageRingItem, RingAbilityHandler,
Curios integration, tick-event registration and teitoku RingFly persistence and
their existing GameTests. Verify the actual grant/reset chain on both logical
sides, client ability packets, no-ring/disabled/independent-flight cases. Preserve
legacy thresholds; old-save mayfly cleanup remains explicitly out of scope.

Recovery: legacy ShipCmdGetShip, CommandHelper, cached-world ship data, dimension
transfer/lifecycle cache callbacks; modern /ship get, ShipCacheSavedData,
LegacyShipEntity dimension transfer/unload/tick cache updates and direct helpers.
Use a real cross-dimension teleport/unloaded destination regression, compare
legacy behavior before fixing. Avoid making recovery load all worlds or reviving
dead ships. Compare cached UID/dimension/UUID before and after transfer.

Sound: inspect supplied report/raw logs and level.dat/server settings for exact
world time/frozen time, reachable timekeeper count and current clock dispatch.
Reuse earlier exact-hour A/B finding only if the new fixture matches. If not,
audit direct sound dispatch reachable in this test. No speculative debounce or
vanilla audio workaround. Capture limitations if raw setup files unavailable.

Forge-first minimal API adaptations; src/main read-only, no test weakening.
Forge commits local only under push hold, normal merge and push Neo after final
build and full GameTests. Batch checks once after source is complete.
