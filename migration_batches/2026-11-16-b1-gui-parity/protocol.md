# Protocol: 2026-11-16-b1-gui-parity

## 1. Goal
Achieve complete GUI and Container/Menu parity between legacy 1.12.2 (`src/main/java`) and modern 1.20.1 Forge (`src/modern/java`) across all 10 legacy GUI classes and 9 legacy Container classes. Specifically align slot layouts, interactive click handlers, visual overlays, stock/build quantity labels, and multiplayer safety.

## 2. Scope & Mapping Matrix

### 2.1 Legacy Gui to Modern Screen Mapping (10 Classes)
| # | Legacy GUI Class | Modern Screen Class | Status | Actions |
|---|------------------|---------------------|--------|---------|
| 1 | `GuiSmallShipyard.java` | `SmallShipyardScreen.java` | Full Parity | None. Verified 100% matched. |
| 2 | `GuiLargeShipyard.java` | `LargeShipyardScreen.java` | Alignment Required | Wire `BUTTON_INVENTORY_MODE` (2), `BUTTON_SELECT_MATERIAL_BASE` (3..6), `BUTTON_ADJUST_MATERIAL_BASE` (7..14); render overlay at (23,92), frame at (27, 14+m*19), adjustment panel at (50, 8+m*19); render stock labels at x=125 in yellow. |
| 3 | `GuiVolCore.java` | `LegacyCoreScreen.java` | Alignment Required | Realign charge bar to `(38, 59 - chargeBar, 12, chargeBar)`; add power toggle button at `(7, 6, 13, 13)` with active overlay UV `(12, 166, 13, 13)`. |
| 4 | `GuiRecipePaper.java` | `RecipePaperScreen.java` | Full Parity | None. Verified 100% matched. |
| 5 | `GuiCrane.java` | `CraneTerminalScreen.java` | Alignment Required | Add power toggle button at `(7, 6, 13, 13)` with active overlay UV `(176, 0, 13, 13)`; support right-click decrement on wait mode button. |
| 6 | `GuiFormation.java` | `FormationScreen.java` | Full Functional Parity | Modern UI adaptation with full command binding. Verified. |
| 7 | `GuiMorphInventory.java` | `MorphInventoryScreen.java` | Full Functional Parity | Modern UI adaptation with full command binding. Verified. |
| 8 | `GuiShipInventory.java` | `ShipInventoryScreen.java` | Full Functional Parity | Slot coordinates 100% identical; AI keybinds aligned. Verified. |
| 9 | `GuiBook.java` | `DeskReferenceScreen.java` | Alignment Required | Guide book chapter reading verified; guard `getSingleplayerServer()` for multiplayer safety. |
| 10 | `GuiDesk.java` | `DeskTerminalScreen.java` / `DeskReferenceScreen.java` / `WaypointTerminalScreen.java` | Full Functional Parity | Monolith split into dedicated terminal and reference screens. Verified. |

### 2.2 Legacy Container to Modern Menu Mapping (9 Classes)
| # | Legacy Container Class | Modern Menu Class | Slot Count (Tile/Ext + Player) | Status | Actions |
|---|------------------------|-------------------|--------------------------------|--------|---------|
| 1 | `ContainerSmallShipyard.java` | `SmallShipyardMenu.java` | 5 in + 1 out + 36 player = 42 | Full Parity | Verified. |
| 2 | `ContainerLargeShipyard.java` | `LargeShipyardMenu.java` | 1 out + 9 tray + 36 player = 46 | Alignment Required | Remove invalid slot at (9, 83); place slots 1..9 along y=116 at x=25..169 (`7 + i * 18`); ensure total 46 slots. |
| 3 | `ContainerVolCore.java` | `LegacyCoreMenu.java` | 9 fuel + 36 player = 45 | Alignment Required | Realign 3x3 fuel grid from `(18, 22)` to `(62 + col*18, 19 + row*18)`; player inv to `(8 + col*18, 84 + row*18)`; hotbar to `(8 + hotbarSlot*18, 142)`. |
| 4 | `ContainerRecipePaper.java` | `RecipePaperMenu.java` | 9 matrix + 1 out + 36 player = 46 | Full Parity | Ghost matrix and NBT recipe sync verified. |
| 5 | `ContainerCrane.java` | `CraneTerminalMenu.java` | 18 template + 36 player = 54 | Alignment Required | Support `BUTTON_POWER` (9) and `BUTTON_WAIT_MODE_DECR` (10); wire to `CraneBlockEntity`. |
| 6 | `ContainerFormation.java` | `FormationMenu.java` | 0 + 0 = 0 | Full Parity | Verified. |
| 7 | `ContainerMorphInventory.java` | `MorphInventoryMenu.java` | 6 equip + 36 player = 42 | Full Parity | Verified. |
| 8 | `ContainerShipInventory.java` | `ShipInventoryMenu.java` | 6 equip + 18 cargo + 36 player = 60 | Full Parity | Verified. |
| 9 | `ContainerDesk.java` | `DeskTerminalMenu.java` / `DeskReferenceMenu.java` | 0 + 0 = 0 | Full Parity | Verified. |

## 3. Legacy References (Read-Only Truth)
- `src/main/java/com/lulan/shincolle/client/gui/GuiSmallShipyard.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiLargeShipyard.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiVolCore.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiRecipePaper.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiCrane.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiFormation.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiMorphInventory.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiShipInventory.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiBook.java`
- `src/main/java/com/lulan/shincolle/client/gui/GuiDesk.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerSmallShipyard.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerLargeShipyard.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerVolCore.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerRecipePaper.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerCrane.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerFormation.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerMorphInventory.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerShipInventory.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerDesk.java`
- `src/main/java/com/lulan/shincolle/utility/GuiHelper.java`

## 4. Modern References (Modification Targets)
- `src/modern/java/com/lulan/shincolle/menu/LargeShipyardMenu.java`
- `src/modern/java/com/lulan/shincolle/client/screen/LargeShipyardScreen.java`
- `src/modern/java/com/lulan/shincolle/menu/LegacyCoreMenu.java`
- `src/modern/java/com/lulan/shincolle/client/screen/LegacyCoreScreen.java`
- `src/modern/java/com/lulan/shincolle/blockentity/CraneBlockEntity.java`
- `src/modern/java/com/lulan/shincolle/menu/CraneTerminalMenu.java`
- `src/modern/java/com/lulan/shincolle/client/screen/CraneTerminalScreen.java`
- `src/modern/java/com/lulan/shincolle/client/screen/DeskReferenceScreen.java`

## 5. Acceptance Assertions
- [ ] `LargeShipyardMenu`: slot 0 at (168, 51), slots 1..9 at `(7 + i * 18, 116)` for `i = 1..9`. No slot at (9, 83). Total 46 slots.
- [ ] `LargeShipyardScreen`:
  - `invMode` button at `(23, 93, 25, 20)` triggers `BUTTON_INVENTORY_MODE`; overlay rendered at `(23, 92, 25, 20)` with UV `(208, 82)` when in output mode.
  - Material selection rows 0..3 triggered by clicking `(27, 14 + mat*19, 18, 18)` or `(51, 19 + mat*19, 46, 8)` for `BUTTON_SELECT_MATERIAL_BASE + mat`; selection frame rendered at `(27, 14 + selectMat*19, 18, 18)` with UV `(208, 64)`.
  - Adjust buttons panel rendered at `(50, 8 + selectMat*19, 48, 30)` with UV `(0, 223)` when material is selected; clicking +1000..-1 triggers `BUTTON_ADJUST_MATERIAL_BASE + action`.
  - Display stock amounts `matStock0..3` centered at `x=125, y=20, 39, 58, 77` in yellow `0xE0C54E`.
- [ ] `LegacyCoreMenu`: 3x3 fuel grid at `(62 + col*18, 19 + row*18)`, player inv at `(8 + col*18, 84 + row*18)`, hotbar at `(8 + hotbarSlot*18, 142)`.
- [ ] `LegacyCoreScreen`: charge bar at `(38, 59 - chargeBar, 12, chargeBar)`; power toggle button at `(7, 6, 13, 13)` with active overlay UV `(12, 166, 13, 13)`.
- [ ] `CraneTerminalMenu` & `CraneTerminalScreen`: power toggle button at `(7, 6, 13, 13)` with active overlay UV `(176, 0, 13, 13)`; right-click on wait mode decrements mode.
- [ ] `DeskReferenceScreen`: `getSingleplayerServer()` safely guarded against `null` for multiplayer environments.
- [ ] `./gradlew compileJava` builds with 0 errors.
- [ ] `./gradlew runGameTestServer` passes with 0 failures (all 276 tests pass).

## 6. Frozen Non-Goals
- Modifying `src/main/java` or `src/main/resources` (read-only truth).
- Replacing modern `Button` and `GuiGraphics` widgets in `FormationScreen` or `MorphInventoryScreen` with legacy raw OpenGL draw calls where modern presentation is already functionally equivalent.
- Network packet protocol overhauls (deferred to `B2-PACKET-PARITY`).
