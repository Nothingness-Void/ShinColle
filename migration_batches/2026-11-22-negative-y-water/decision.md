# Decision

Required negative-height adaptation, not a new buoyancy mechanic. Legacy
0aad501 BasicEntityShip:1910 and BasicEntityMount:507 subtract posY-(int)posY;
modern updateLegacyWaterState is called first in every LegacyShipEntity.tick.
At feet Y=-60.1 in water below surface -60 the old fractional calculation is
1.1 instead of 0.1. Counter-check: that alone overestimates depth (not evidence
of the reported sinking direction). The independently reachable
ShipNavigationHost.travel/isInLiquid and TeitokuEvents liquid gate also cast
bounding-box minY, sampling AIR at -60 rather than WATER at -61; travel then
takes its land branch. Both faults must be corrected, not just the fraction.

Floor Y in ship depth, mount-depth render compatibility, both navigation-host
liquid/free-block gates, all five ShipPathNavigate coordinate casts (legacy
98/107/446/472/501), formation target Y (FormationHelper:876), Teitoku liquid
gate (EntityHelper:99/106) and desk height readout (GuiDesk.drawRadarText).
The existing path-test target assertion now floors Y too. No lift, threshold,
water movement, formation layout, or name rendering values changed.

ALL explicit int casts across src/modern Java were inventoried, including
scripts, tests, HUD, particles, block entities, model compatibility and world
code. CSV preserves each operand and classification. Size/width, health,
animation frame, lightmap, mouse/HUD pixels, count/distance, pointer hit-height
percentage rounding and packet effect selectors are not absolute coordinates.
ShipWorldCacheEntry and Teitoku X/Z already use Math.floor; other modern block
positions use BlockPos.containing/Mth.floor. No X/Z changes: ShipPath's
isDestinationSame vector truncation is IDENTICAL to legacy ShipPath:141 and is
intentionally retained. Both target coordinates in ShipPathNavigate already
floor X/Z just as legacy did. Model bodies remain unchanged.

New GameTest exercises the actual ship tick at surfaces65/-60, submersions
0.1/0.8 and X/Z +3200.5/-3200.5 (8 cases), verifies depth, canFloatUp, both
liquid gates and exact legacy lift, then verifies dry exit. Fixture blocks and
entities are restored. This proves the depth/buoyancy/gate adaptation, not a
long-running live-client settling coordinate; that part of the reported sink
has not been measured. Other negative-height navigation routes and visual desk
height/mount-depth branches are source-audited, not individually live-tested.
