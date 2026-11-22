# GUI and height review

Ship GUI: GuiShipInventory.drawEntityModel and ShipInventoryScreen use the same
anchor (218,100), mouse origin (215,60), scale/position modelPos array, and
unclipped control labels (e.g. Can Melee at187,133). Re-Class's modelPos is
(-6,25,0,40) in both EntityBattleshipRe and LegacyShipInventoryCatalog entry17.
Legacy also lets the model overlap the controls; no new scissor or label scaling
was invented. This is source-level parity, not an actual 1.12.2 visual run.

Book: every page's text, image UV/size/position and icon positions in Values.BookList
match LegacyDeskBookData. GuiBook and DeskScreen retain PageWidth135, left/right
text origins13/162, PageTY58, scale0.8, wrapping line height9 and <br> splitting.
However, 115 English book values had been replaced with different translations.
Restored all changed gui.shincolle:book.chap* values verbatim from en_US.lang,
including original spelling/formatting and line breaks; existing keys retained.
The directed source guard checks all book values and page placements. Remaining
fixed-layout text/image overlap is inherited; do not redesign the legacy book.

Radar: old py>0 is now a strict entity Y>level minimum gate; a missing entity stays
hidden (rather than treating the old -1 sentinel as a valid negative position).
New tests cover null, below/at/above minimum, negative Y, zero and positive Y.

Height audit: task paired/guard positions in LegacyShipBehaviorCatalog, route
positions in LegacyShipEntity, LegacyShipGuardGoal and the two ground-search
limits in ShipPathFinder now use level minimum instead of0. Each corresponded
to a legacy world-position validity/search bound. No hardcoded255/256 world
height validity checks remain in these paths. Numeric UI, cooldowns, fluid-depth,
waypoint mode checks and relative offsets are not build-height bounds. The
polymetal y>55 ore-generation rule is gameplay, not a validity bound, and remains.
Legacy strictness is retained: the floor itself is excluded, as old y=0 was.
