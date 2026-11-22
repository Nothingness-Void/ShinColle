# Batch A: negative-height water and coordinate narrowing

User finding: water surface at Y=-60 sinks ships to about -61.05 while the same
pool at65 floats. Trace LegacyShipEntity.updateLegacyWaterState and its real tick
dispatch, compare historical BasicEntityShip depth sampling. Audit all (int)
entity/world-position casts across src/modern (X/Y/Z, old pos fields, eyes/AABB),
including helper/screen/particle/worldgen uses; record intentional legacy X/Z
truncation rather than changing it. Floor Y block-coordinate conversions as the
negative-world-height adaptation. Change X/Z only with legacy floor evidence.

Entries: src/main BasicEntityShip, EntityHelper, BlockHelper and any exact legacy
counterpart found by the cast inventory; src/modern LegacyShipEntity and
LegacyShipBehaviorCatalog, TeitokuEvents, waypoint/client effects, block entities,
world/config helpers, screens/models and existing water/depth GameTests.

Inventory every coordinate-cast candidate in a scripted report; compare each
reachable production path with reference before changing it. Exclude unrelated
GUI/pixel interpolation, motion/count arithmetic and unrelated gameplay. No
src/main changes; no broad architecture/config changes. Add positive-vs-negative
Y water-depth/buoyancy GameTest if feasible (assert the fixture and tick path).
Forge first, then merge NeoForge without force-push, then final build and full
GameTests both branches. Prior D/E/F builds are historical once A changes code.
Waypoint visual checks remain independently required; no broader client testing.
