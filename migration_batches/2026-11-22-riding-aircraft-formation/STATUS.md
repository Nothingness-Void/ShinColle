# Consolidated VM queue

Authoritative latest: [08:00 result and outstanding VM checks](queue-final-result.md).
Forge362/362, Neo370/370 green after dynamic formation test. Neo source4302073
pushed; Forge44bfc38 remains local under the explicit push hold. D3 fixture and
B2 production regressions resolved; original line-abreast stall not reproduced.
Earlier gate snapshots below are historical, not current blockers.

Latest07:09: D3 resolved, Neo build10s and365/365 full GT42s green. The actual
cause was the9³ GameTest barrier enclosure, not proven gameplay precision.
See [verified chain and unchanged assertions](D3-resolution.md). B2 remains next.

Authoritative latest: [CHECKPOINT06:53](CHECKPOINT.md). Forge357/357 is pushed;
Neo build passes but365 GT have1 required D3 mount-route failure, not pushed.
New B round2 kill/reload resurrection is OPEN; no verdict or production fix yet.
Earlier progress below is superseded where the checkpoint explicitly differs.
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
| Shimakaze minecart | attachment compensated; visual OPEN | Normal/bored model source matches legacy; old/Forge cart anchor0 versus 1.21 anchor0.1875 compensated in Neo only. New diagnostic frames still show boots outside cart and body mostly concealed; not a passing visual check or verified cause. |
| Line-abreast settling | controlled trace matches; original stall OPEN | Six real slots×384ticks compare independent formula/4 and7 hysteresis/path fields and reach squared7 arrival on both versions. Original4.6–5.9 gap not reproduced; fixture isolates follow goal and overrides owner lookup. No production change. See line-abreast-result.md. |
| Curios ring slot/all effects | fixed; gates green | Add curios:ring; support Forge LazyOptional and Neo Optional, dispatch full legacy onUpdate-equivalent inventory tick for passive breathing even toggled off. Real optional integration GT covers active/off/remove/fire/breathing/water flight. |
| Attrs_Scale / four hostile arrays | fixed; gates green | ConfigHandler113–119/395–400 exact defaults; all production friendly/buff/potion/movement and hostile-band uses read config. Tables retain default reference constants. |
| Morph G/H | unlock-only gate legacy-identical; client counter-check pending | Old C2SInputPackets/GuiHandler require active morphEntity; modern unlock neither selects nor activates, missing selected profile closes screen. No invented activation; external bridge remains none. |
| OpenAL Stop error | vanilla/unattributed | Explicit stop/stopsound/custom SoundInstance audit finds no mod path. Vanilla Channel.stop/destroy issue this label. No single-session causal attribution and no workaround. See openal-result.md. |
| Floating fortress + all 8 Hime mounts | fixed; full gates green, wall visuals OPEN | Legacy keeps solid collision. Shared swept bounds/obstacle routing and all8 real mount profiles+fort pass. Neo failure was9³ GameTest barrier enclosure, resolved by dedicated fixture without assertion changes. See D3-resolution.md. |
| B2 kill/reload | fixed | Actual command kill and entity-region storage reload reproduce health refill after variant reconstruction. Restore original Health and death lock. Neo damage overflow also caused kill NaN: saturate amplified damage finite, same normal RNG/formula. See inventory-book-layout/round2-result.md. |

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

## F2 / D3 Forge gate

JDK17, external init /home/ubuntu/shincolle-curios-forge.init.gradle supplies
fg.deobf Curios5.14.1 runtime and required Mixin refmap mapping. Runtime-only QA
config run/config/curios-common.toml has slots=["id=ring;size=1"]. This run
exercises the real Curios branch, not the absent-mod/no-slots early success.
No production Gradle/dependency changes. Logs in home:
shincolle-queue-forge-round2-{build,gametest}.log.

```
BUILD SUCCESSFUL in 16s
8 actionable tasks: 5 executed, 3 up-to-date
========= 357 GAME TESTS COMPLETE ======================
All 357 required tests passed :)
BUILD SUCCESSFUL in 1m 5s
11 actionable tasks: 3 executed, 8 up-to-date
```

All8 mount styles and fort pass swept glass collision and finite-wall routes;
Curios active/passive/off/re-enable/remove pass. Ring scan keeps the legacy
isRingFlying cache flag as asserted by unchanged GameplayParityGameTests, but
also revokes ring-owned flying when toggle is off (mayfly never granted/revoked).
NeoForge port/final gate and D3 visual evidence still pending. Latest waypoint
stone/wrench VM screenshots: /home/ubuntu/screenshots/shincolle-121-queue-waypoint-
{stone,wrench}.png (same camera; stone genuinely equipped, not pointer).

Follow-up from the Neo gate: the helper's feet-only line-of-sight shortcut could
abandon a usable route while the mount's upper body was still beneath a glass
overhang. Replace that shortcut with a block-only swept bounding-box test;
entity-collision behavior is not changed. Strengthen D3 fixtures by adding real
hosts/targets to the level and asserting host resolution so all eight actual
host profiles, not the no-host fallback dimensions, are exercised. Shorten only
failure diagnostics to fit the 1.21 GameTest lectern's 1024-character book limit.

Final Forge follow-up gate06:50: build SUCCESSFUL in12s (8 tasks:5 executed,
3 up-to-date); all357 required GT passed, SUCCESSFUL in54s (11 tasks:3 executed,
8 up-to-date). Same round2 log paths, supersedes the16s/65s gate above.
