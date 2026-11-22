# Rendering backport protocol

Backport rendering-only work from cae1ae9, 3fa3514 and a1050d1 to the fixed
Forge 47 / Minecraft 1.20.1 parity branch. Preserve Destroyer Hime's intentional
leg-hidden hover (user decision). No src/main edits or gameplay changes.

Entries: LegacyShipPoseModel; LegacyBlockItemRenderer and Forge block-item
registration; DeskBlockEntityRenderer; SmallShipyardBlockEntityRenderer;
ModBlocks; corresponding item/block JSON, canonical texture paths; resource
and CPU pose audit scripts; rendering GameTests and matrix/state documents.
Use reference render/model sources already audited in the NeoForge batch.
Check f6db0ec (recovery entity component migration) and 3a06c51 (enchantability
tags) against Forge 1.20.1 implementation/dependencies; do not invent fixes.

Acceptance: JDK17 build and complete GameTests, resource and CPU model audit,
one targeted client rendering smoke test. Push parity branch. Merge the updated
base into NeoForge only if not already an ancestor; preserve 1.21 APIs with
mechanical resolutions, stop on substantive conflicts. JDK21 build and complete
GameTests, then push NeoForge. No force-push; no PR edits or unrelated work.

Approved addition: Destroyer Hime only shows legs on dry land even with bit 4
clear; water retains the legacy leg toggle. Use the live engine isInWater flag
through the compatibility entity. Share a pure render policy with regression
tests; never mutate stored model state. Document this visual deviation and
capture NeoForge land/water plus ship/block/item overview screenshots.
