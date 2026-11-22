# Batch D round2

D round3 extends block-physics/pathing to FloatingFort and all eight shared Hime
mount styles. Compare EntityFloatingFort, BasicEntityAirplane, BasicEntityMount,
EntityHelper movement and ShipPathFinder to modern fort/mount movement. Keep
manual steering manual; restore solids and path detours, no attack/stat/model
changes. Add all-style swept collision and finite-wall route tests plus VM evidence.

Read repro attachments/23ce2043-1d1b-4aec-bb2a-7fe43cadf620/补测报告.md.
Aircraft: compare historical BasicEntityAirplane constructor/collision/travel and
ShipPathFinder solid-block classification to modern LegacyShipAircraftEntity
and shared ShipNavigationHost/ShipPathFinder. Mount: compare legacy summon,
ownership, mount interaction and rider-type flags with LegacyShipMountEntity,
MountSpec/catalog and Airfield Hime summon dispatch. Fix confirmed regressions.

Shimakaze: both normal and bored boots reported, superseding the first D item.
Use prior exact model-body/pose evidence and cart anchor regression rather than
invent per-pose offsets; check new report for contrary state/position evidence.
Line-abreast: compare FormationHelper/EntityAIShipFollow/guard navigation and
modern LegacyFormationHelper/LegacyShipGuardGoal/navigation; identify whether
4.6–5.9 block stopping distance is an inherited threshold or a migration error.
Trace actual gates before ranking causes, include a counter-check. Preserve
numbers and behavior except necessary APIs. No src/main changes. Forge first,
merge NeoForge, focused GameTests plus final build/full GameTests both branches.
