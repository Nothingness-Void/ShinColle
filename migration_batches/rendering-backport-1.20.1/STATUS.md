# Rendering backport evidence

Based on parity integration 5b95cfe. Rendering source commits: cae1ae9,
3fa3514, a1050d1. The subsequent user-approved Hime land-leg override is
documented in [decision.md](decision.md) and MIGRATION-DECISIONS.md.
Only src/modern and docs changed; no build/CI or src/main changes.

## Final Forge gates, JDK 17

`/home/ubuntu/shincolle-120-render-build.log`:

```text
> Task :verifyItemModelCoverage
> Task :check
> Task :build

BUILD SUCCESSFUL in 16s
8 actionable tasks: 5 executed, 3 up-to-date
```

`/home/ubuntu/shincolle-120-render-gametest.log`:

```text
[04:09:45] [Server thread/INFO] [minecraft/GameTestServer]: ========= 344 GAME TESTS COMPLETE ======================
[04:09:45] [Server thread/INFO] [minecraft/GameTestServer]: All 344 required tests passed :)
BUILD SUCCESSFUL in 44s
```

No weakened assertions. Two model-mapping tests and two block-rendering tests
are added to the 340-test parity baseline. Forge adapters use its original
dimensions fields, RGBA renderer and Item.initializeClient API.

Resource check:

```text
Render resource checks passed: 0 case-fold collisions; legacy geometry, textures and item render paths intact
```

`/home/ubuntu/shincolle-120-render-pose-audit.log`:

```text
All 45 model bodies match legacy after three API aliases and the approved Hime land-leg override
Shared pose, visibility flags and outer pre-render call order verified
Measured 1536 model/state/pose cases at shadow plane Y=0
Vertex audit passed; CSV: migration_batches/rendering-backport-1.20.1/ship-pose-audit.csv
```

Complete controlled geometry sample: [CSV](ship-pose-audit.csv).
Review applicability: both f6db0ec and 3a06c51 are unnecessary on the shipped
Forge implementation/dependencies; [checked evidence and limits](decision.md).

## Client and branch synchronization

Forge client smoke passed: default-state Hime has grounded legs and the desk,
crown and small shipyard have their restored world silhouettes, with the desk
held. Screenshot: /home/ubuntu/screenshots/shincolle-120-render-backport-smoke.png.
Full Forge water/toggle/inventory interaction matrices and a native Windows
client were not exercised; the detailed land/water screenshots are required
on NeoForge. NeoForge merge/gates are recorded on that branch after completion.

The machine-local Maven Central mirror init script bypassed initial HTTP 429s;
it is outside the repo and no build files changed. The headless CPU auditor
disables interactive JLine so its stdout remains valid CSV (1536 validated
data rows); this is not a gameplay or build-environment change.
