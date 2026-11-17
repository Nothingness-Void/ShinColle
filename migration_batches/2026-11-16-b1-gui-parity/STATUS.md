# Batch Status: 2026-11-16-b1-gui-parity

- **Milestone**: M2: B1-GUI-PARITY
- **State**: COMPLETED
- **Protocol**: FROZEN (`migration_batches/2026-11-16-b1-gui-parity/protocol.md`)
- **Decision Record**: `migration_batches/2026-11-16-b1-gui-parity/decision.md`
- **Results**: `migration_batches/2026-11-16-b1-gui-parity/results/b1-gui-parity.json`

## Summary of Accomplishments
1. Audited all 10 legacy GUI classes and 9 legacy Container classes against modern screens and menus.
2. Aligned `LargeShipyardMenu.java` slot layout (removed fuel slot at 9, 83; restored 9 tray input slots at y=116, x=25..169).
3. Aligned `LargeShipyardScreen.java` with complete click handling and rendering for inventory mode, material selection, adjustment panels (+/- 1000..1), and stock quantities at x=125 in yellow.
4. Aligned `LegacyCoreMenu.java` 3x3 slots and player/hotbar slots with `gui_vol_core.png` artwork.
5. Aligned `LegacyCoreScreen.java` charge bar to 31px height at `(38, 59-chargeBar)` and power toggle button at `(7, 6, 13, 13)`.
6. Aligned `CraneTerminalScreen.java` power button at `(7, 6, 13, 13)` and wait mode right-click decrement.
7. Verified `DeskReferenceScreen.java` multiplayer safety against `null` singleplayer server references.
8. Verified clean compilation with `./gradlew compileJava` (0 errors).
9. Verified full GameTest test suite with `./gradlew runGameTestServer` (276/276 passed, 0 failures, 0 regressions).
