# Complete cart offset and factory visual check

Compare legacy EntityDestroyerShimakaze/BasicEntityShip/BasicEntityShipSmall,
RenderShipEntity/RenderDestroyerShimakaze and ModelDestroyerShimakaze with modern
LegacyShipEntity, LegacyRiding, LegacyShipBehaviorCatalog, ship renderer/pose
adapter and model. Include vanilla1.12 Entity/AbstractMinecart positionRider and
1.20/1.21 Entity/AbstractMinecart attachment math from available vanilla sources.
Report cart-origin to passenger and render-origin normal/bored numeric sums;
disprove leading sign/anchor hypothesis with real cart passenger GameTest.
Fix only verified differences Forge-first; keep Forge push hold, ordinary Neo
merge/push only. Models and src/main remain unchanged unless migrated differently.
Then factory-created carrier attack aircraft, Northern Hime fortress and actual
owned mount, stationary owner/camera while advancing into glass. Cart normal and
bored states must be established before screenshots; diagnostic frame is not pass.
Use a QA-only GameTest fixture or existing factory dispatch, not repeated NBT
identity reconstruction. Freeze via server tick manager and save actual PNGs.
