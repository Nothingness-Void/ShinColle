# D round 4 actual mount path following and Akagi height

Lead report on Neo470ff021: player mounting, line-abreast and aircraft wall PASS;
all eight mounts stop ~5.75 before glass instead of going around/over a goal ~15
away. Akagi heavy aircraft does not hit an elevated Y73 target behind wall by210
ticks, light does. Cart clipping remains part of cart-offset-result comparison.

Compare src/main BasicEntityMount, mount-specific entities and floating potion
delegation, ShipPathNavigate81–90 (48/70), path following and related attack AI
with modern LegacyShipMountEntity.tick/moveToward/resolveMovementTarget,
LegacyShipObstaclePath, ShipPathFinder/ShipPathNavigate, LegacyMountControls,
host floating/attack accessors. For aircraft compare BasicEntityAirplane,
EntityAirplaneTakoyaki/carrier Akagi attack dispatch and attack AI with modern
LegacyShipAircraftEntity movement, strike gates, profile/cooldown and render
counterparts. Follow only their direct dependencies as necessary.

Existing D3 tests drive helper waypoints directly; add a focused actual mount.tick
regression with real host/mount/passenger and a stationary goal, normal empty path,
finite glass obstruction and invalid host. Verify floating-effect requirement and
range before changing values. Fix only proven legacy divergences Forge-first,
keep Forge commits local, merge and gate Neo then normal push. Do not weaken
assertions, relocate failing tests, edit src/main or invent new flight mechanics.
