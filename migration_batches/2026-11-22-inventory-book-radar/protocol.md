# Batch B: GUI parity and world-height bounds

Source repro: attachments/3dfe574d-c555-4126-918b-8e6d6ee5db38/batch-b-report.md.
Compare legacy GuiShipInventory preview drawEntityOnScreen and control labels
against ShipInventoryScreen and compat FontRenderer/GlStateManager/common ship
renderer. Compare GuiDesk book chapter/recipe text/image placements and English
translations against DeskScreen. Fix only verified regressions; legacy-identical
overflow must remain unchanged. Prior radar-name overflow audit is authoritative.

Height adaptation: inspect GuiDesk radar cached position filter (py>0) and modern
counterpart; use current level min/max where a legacy validity/height bound was
intended. Inventory all modern hardcoded Y>0/Y>=0/Y<256 and equivalent bounds;
trace each candidate to legacy before changing. No src/main changes. Add a
focused radar-height test and bounds audit evidence; source guard for unchanged
GUI layouts. Forge first, merge NeoForge, final build+full GameTests both.
No wholesale GUI redesign, truncation, new previews or changed control semantics.
