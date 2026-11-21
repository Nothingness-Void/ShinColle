# Morph inventory API adaptation

Truth: 0aad501 GuiMorphInventory, GuiHelper GUI 0 and 7, ContainerMorphInventory,
C2SGUIPackets MorphBtn/MorphBtn2, PacketHelper.setEntityByGUI.

The current src/main GuiDesk.mouseClicked was checked before correcting the
previous desk port. It only opens inventory on a repeated list-row click. Map
points are hover-only again; the added point-selection/opening and offset helper
were removed from code generation, generated code, assertions and desk notes.

## GUI

- 256x214 and the original lower-case GuiShipMorph texture copy; unchanged old
  icon sheets, text positions, inclusive rectangles and three main tabs.
- Foreground, background, attributes, hover text, button dispatch and initial
  translated labels are mechanically adapted from GuiMorphInventory.
- The model anchor is (218,100), model positioning comes from the old per-ship
  metadata, and mouse offsets are (215-xMouse,60-yMouse). The existing ship GUI
  renderer bridge preserves the original atan rotation formulas and restores
  entity/camera rotations; rendering goes through the migrated legacy models.
- Current/max HP are ceil(player.getHealth/getMaxHealth), not profile ship HP.
- The six equipment slots and all player inventory slots retain ContainerMorph's
  coordinates; there are no added profile arrows/counter or activation switch.

## Buttons and state

- MorphBtn adapts to MORPH_GUI_BUTTON with original ShipGuiButton IDs and value.
  NoFuel assigns value>0, Sit toggles regardless of value, Emotion assigns
  nextInt(9), Emotion4 toggles 0/4, model bits XOR, and aura/held
  assign the supplied flag. Unknown button IDs are ignored without mutation.
- Those missing GUI states persist/sync in MorphProfile.MorphGui. Old data without
  the compound reads the original false/zero defaults. Preview flags do not
  fabricate grudge refills, resource costs, or a new fuel-consumption rule.
- MorphBtn2 supply rectangles map to the existing three supply commands and
  existing server item-consumption handlers.
- Hostile profiles can be unlocked by existing gameplay. Their GUI icon/model-
  position metadata uses the existing explicit friendlyCounterpart table; combat
  stats and the actual preview entity retain the original hostile profile spec.
- Removed GUI-only cycle/activate routes, helpers, menu readouts, language keys
  and cycle assertions. Profile storage, explicit external selection and the
  active runtime flag remain because skill/mount/compatibility paths use them.
  This screen no longer supplies an activation entry point; no replacement or
  automatic activation is invented.

## Limits of this step

The modern profile has no historical kill counter or aircraft stock. Their
existing default values remain zero in the presentation; this task does not
invent a new kill/aircraft lifecycle. Actual world morph rendering/selection by
external mods is not replaced by the inventory's temporary preview entity.

## Reproduction

    python3 src/modern/scripts/port_morph_inventory.py ~/legacy0aad --check
    bash ./gradlew compileJava --no-daemon
    bash ./gradlew runGameTestServer --no-daemon

The generator reuses the existing ship GUI API-adaptation functions/render bridge
without executing that generator's writes or modifying ship inventory sources.
Regression assertions are included in the existing morph-support GameTest to
avoid adding world fixtures; they cover tabs, flag packets, supplies, model bits,
hostile metadata, empty/old data and invalid buttons.

## Verified in this step

compileJava succeeded; GameTest completed 301/301 required tests successfully.
The one client launch used an owned Atago unlocked through the existing wrench
interaction. The GUI showed player HP 20/24 (then natural regeneration to 21/24),
all three tabs, mouse-driven model rotation, and checkbox/pose updates for Sit
and NoFuel. Other ship models and external world morph rendering were not
visually checked.

Screenshots under /home/ubuntu/screenshots:

- ss_09dec2df.png / ss_84ee9487.png: opposite mouse positions; second shows HP.
- ss_9f64688b.png / ss_6d360779.png: sitting and NoFuel model poses/checkboxes.
- ss_1023c9eb.png / ss_b836dcea.png: second and third tabs.
