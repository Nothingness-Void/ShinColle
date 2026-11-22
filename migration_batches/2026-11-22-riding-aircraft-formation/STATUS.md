# Consolidated VM queue

Implementation review complete except the live line-abreast settling cause.
Final gates, commits and requested NeoForge VM screenshots pending below.

| Item | Verdict | Legacy evidence / change |
| --- | --- | --- |
| Waypoint world visibility | fixed | BlockWaypoint INVISIBLE; TileEntityWaypoint tool-gated marker; no TESR registration. Item model retained. |
| Waypoint harvest | fixed | BlockWaypoint.removedByPlayer inserts one item, drops at player only when inventory full; harvestBlock/quantityDropped suppress vanilla duplicate. Ownership retained. |
| Ring mayfly leak | fixed | MarriageRing.onUpdate and EventHandler use temporary isFlying/RingFly, never allowFlying; disabling/loss/water-exit clear owned flight. Does not guess ownership of preexisting leaked permissions. |
| Wrench/all recipes | fixed | itemWrench uses4 AbyssMetal0. Full list below;83 valid equivalents match. |
| Negative-Y floor | fixed | Legacy nonnegative-Y block sampling requires floor below0; no intentional legacy X/Z truncation changed. CSV inventories all casts. |
| Hostile egg timing | fixed | BasicEntityShipHostile.onDeathUpdate drops at Death_Time, default400; modern tickDeath restores deadline, flags and probability. Friendly loot unchanged. |
| Radar/build bounds | fixed | GuiDesk py>0 validity gate now current level minimum, missing entity still hidden; guard/task/route/path bounds likewise. |
| Ship inventory layout | legacy-identical | GuiShipInventory anchor218,100, Re-Class modelPos(-6,25,0,40), scale, control labels and unclipped preview match. No layout redesign. |
| Desk book | fixed text; remaining layout legacy-identical | Restored115 English values from en_US.lang; all37 explicit page entries retain legacy text/image/icon placements and 0.8 text scale. |
| Aircraft collision/path | fixed | BasicEntityAirplane disables entity collision only; normal move and ShipPathFinder retain solids. Removed noPhysics and restored obstacle path steering. |
| Mount right-click | fixed | BasicEntityMount is living/pickable and interacts to ride; modern false isPickable blocked client selection before existing interact. |
| Shimakaze minecart | fixed 1.21 attachment; pose legacy-identical | Both normal/bored ModelDestroyerShimakaze bodies match; old/Forge cart anchor0 versus 1.21 anchor0.1875 compensated in NeoForge only. |
| Line-abreast settling | OPEN | Formulas, thresholds4/7, pickup64, repath/stuck32, turn60 and explicit extra movement match legacy. Live oscillation cause not established; no speculative change. |

## Complete recipe differences

1. targetwrench6→4 abyssmetal, exact old shaped grid.
2. blockdesk any wool→black wool.
3. Add toyairplane→5 abyssmetal1.
4. Add eight experience bottles + grudge0→grudge1.
5. Remove invented blockgrudgexp→9 grudge1 inverse recipe.
6. Keep historical blockLargeShipyard JSON absent: no registered corresponding
   legacy/modern standalone block/item, and existing regression forbids inventing it.
7. NeoForge only: eight recipes' c:obsidians narrowed to c:obsidians/normal to
   preserve Forge/legacy ordinary obsidian, excluding crying obsidian.

Full aliases and normalized ingredient/count/output audit:
../2026-11-22-ring-flight-recipes/decision.md and check_recipe_parity.py.
