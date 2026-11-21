# Ship inventory parity scope

Reference: 0aad501 GuiShipInventory (cargo tabs, twelve control pages,
formation tooltips), GuiHelper ship hitboxes, PacketHelper and EntityHelper GUI
button handlers. The legacy checkout and src/main are read-only.

Targets: ShipInventoryScreen, ShipInventoryMenu, existing ship GUI button
dispatch/state synchronization, their GameTest coverage, language and lowercase
copies of original GUI textures. Preserve the existing sixty inventory slots and
three-page storage. Do not invent ship-specific capacity rules or hotkeys.

Restore the original texture layout, click regions, page controls, independent
follow-distance sliders, retreat threshold, model visibility, crane input/output/
fuel faces, and actual formation modifiers. Reuse existing legacy button IDs.

Checks: compileJava, runGameTestServer with counts, diff/boundary checks and one
client smoke check. Commit and push only devin/parity-fix-client.

Implementation notes: the original pages 9-12 have selectors and indicators but
no controls; they remain reserved rather than receiving invented actions. The
sixty slots are six equipment slots and three eighteen-slot cargo pages. Capacity
comes exclusively from the entity's existing getInventoryPageSize(), not the GUI
metadata catalog. The existing Wa capacity is backed by EntityTransportWa:267.

The menu sends the server's actual buffed, morale and formation attributes with
the opening data and changed snapshots. Cargo-page commands send the snapshot
before a full slot refresh, including equal-valued stacks on different pages.
Control hitboxes, dispatch and slider formulas are shared with server-safe
regression checks; the presentation methods and original resources are checked
by scripts/port_ship_inventory.py --check against the complete legacy tree.

Validation: compileJava succeeded; all 301 required GameTests passed. A client
was launched into the local test world, but the target inventory was not opened
successfully during the smoke attempt. The inventory rendering and interaction
hitboxes remain visually unverified; no in-world screenshot is parity evidence.
