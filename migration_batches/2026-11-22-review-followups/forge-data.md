# B — ForgeData drop markers

## Evidence and decision
LegacyEntityItemMarker stores one relevant key: shincolle:BasicEntityItem
(boolean). TeitokuHelper.buildDeskRadarPayloadTag and
ShinColleCommands.executeShipClearDrop filter ItemEntity via isMarked; item
identity alone is intentionally insufficient, matching the legacy
BasicEntityItem target class.

Inspected the actual platform Entity patches/sources:
- Forge 47.4.18 Entity.load reads ForgeData into persistentData, and saves it
  back as ForgeData. This branch therefore needs no production migration.
- NeoForge 21.1.267 Entity.load/save reads/writes NeoForgeData exclusively.
  Its compiled source contains no ForgeData read. Existing
  LegacyEntityDataMixin only upgrades nested stacks on selected entities and
  imports a player ForgeCaps attachment; neither restores an item drop marker.

No Forge production code changed. The native ForgeData load path already\npreserves the marker. A regression GameTest verifies this instead of adding\nunnecessary migration logic.

## Coverage
LegacyDropMarkerPersistenceGameTests loads real entity NBT with ForgeData,
checks the marker, native save/reload, Desk radar inclusion and actual
/shipcleardrop removal while preserving an unmarked ordinary item.
Missing/empty/malformed/false inputs remain unmarked.

Full build/GameTest evidence is recorded in result.md. No client visual check
was requested/performed; this is server-side NBT and command verification, not a
complete real-world upgrade replay.
