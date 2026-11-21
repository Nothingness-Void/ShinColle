# Desk, radar and book parity

Reference: complete 0aad501 GuiDesk, GuiBook, GuiHelper, GuiHandler, Values,
TileEntityDesk and the actual item GUI calls. Edit only src/modern. Reuse the
existing team/target server commands. Keep desk functions 0-4, portable radar 1
and book 2, the original 1.25 GUI scale and fixed button rectangles. Preserve
all seven chapter bounds and original data, translations, pictures and icons.

Validation: compileJava, runGameTestServer (exact totals), data/control parity
checks, at most one client smoke attempt. Commit and push parity-fix-client.

## Mapping

- Desk block: type 0; functions 0 (closed page), 1 radar, 2 book, 3 team,
  4 target. Sneaking does not open or cycle the desk. No added bell sound.
- The actual 0aad501 portable desk items are DeskItemRadar (type 1) and
  DeskItemBook (type 2). Both use this same menu without the block's tab bar.
- LegacyDeskState retains GuiHelper's inclusive button rectangles. The block
  entity keeps the original function, zoom, chapter and page fields. Cooldown
  menu data uses ContainerDesk's seconds-to-ticks conversion.
- Book content, icon IDs, ship lists and seven chapter bounds come from Values
  and GuiBook. Metadata 0/1 ship icons map to smallegg/largeegg; other legacy
  metadata maps to the existing split registry items. Collected class 0 is a
  valid DDI entry, not an invalid/uninitialized class. Model pages remain
  collection-gated and reuse existing ship and mount renderers.
- The six GuiDesk textures and book picture are byte-identical lower-case
  resource copies. Original language text (including blanks and locale fallback)
  is retained. port_desk.py --check compares the original data, translations
  and texture bytes; generic modern-language quality checks exclude book data.
- Radar ships resolve live client entities as in GuiDesk. Item coordinates use
  PacketHelper's float coordinates, BasicEntityItem marker filter, and 1.12
  AxisAlignedBB.expand semantics (modern expandTowards(128,256,128)). The modern
  facility/route and ordinary-item contacts are removed. Row selection has no
  time window. Repeated map-point opening is explicitly requested in this task;
  its hit bounds use the legacy map-hover bounds and its list offset stays in
  the existing five-row scroll range, whereas the original
  GuiDesk.mouseClicked only implemented repeated-row opening.
- Team and target rendering/controls use GuiDesk method bodies with existing
  team/target server commands. Target preview accepts modern registry IDs as
  well as legacy/simple Java class names. No extra world-rules category remains.

## Visual coverage

One client launch: portable book introduction, unified desk/book, closed-page
toggle and empty target page were captured. Radar/team tab transitions were
exercised without separate captured-state review. Populated lists, actual
radar-to-inventory opening, collected ship models and detailed preview animations
are not visually verified. Final source-only changes after this launch concern
team-list ordering, radar offset clamping, cooldown regression assertions and
documentation, not the
captured empty-page/book rendering.
