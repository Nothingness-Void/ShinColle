# Decision Record: 2026-11-16-b1-gui-parity

## 1. Context
Milestone M2: B1-GUI-PARITY requires auditing and aligning all 10 legacy 1.12.2 `Gui*` classes and 9 `Container*` classes against modern 1.20.1 Forge screens and container menus, ensuring strict behavioral fidelity while adhering to the minimal change principle.

## 2. Audit Findings & Alignment Decisions

### 2.1 LargeShipyard Menu & Screen
- **Legacy Truth**:
  - In `ContainerLargeShipyard.java:44-47`, slot 0 is output at `(168, 51)`. Slots 1..9 are tray slots along y=116 at `(7 + i * 18, 116)` for `i = 1..9` (x = 25, 43, 61, 79, 97, 115, 133, 151, 169).
  - In `GuiLargeShipyard.png`, the gauge at `(9, 83)` is a vertical progress bar for fuel, NOT an item slot.
  - In `GuiLargeShipyard.java`:
    - `BUTTON_INVENTORY_MODE` is at `(23, 93, 25, 20)`; when `invMode == 1` (output mode), overlay UV `(208, 82, 25, 20)` is rendered at `(23, 92)`.
    - Selected material frame is rendered at `(27, 14 + selectMat * 19)` with UV `(208, 64, 18, 18)`.
    - Material adjustment panel is rendered at `(50, 8 + selectMat * 19)` with UV `(0, 223, 48, 30)` containing 8 buttons (+/- 1000, 100, 10, 1).
    - Stock quantities `matStock0..3` are rendered centered at `x=125, y=20, 39, 58, 77` in yellow `0xE0C54E`.
    - Build quantities `matBuild0..3` are rendered centered at `x=73, y=20, 39, 58, 77` with threshold colors (<100: red, ==1000: yellow, else: white).
- **Decision & Fix**:
  - Removed the slot at `(9, 83)` in `LargeShipyardMenu.java`. Added all 9 tray slots along y=116 from x=25 to 169.
  - In `LargeShipyardScreen.java`, wired mouse click hitboxes for inventory mode toggle, material selection rows, and the 8 adjust buttons (+/-).
  - Implemented rendering in `LargeShipyardScreen.java` for the output mode overlay, material frame, adjustment panel, and yellow stock labels at x=125.

### 2.2 VolCore / LegacyCore Menu & Screen
- **Legacy Truth**:
  - In `ContainerVolCore.java:33-41`, the 3x3 fuel grid is located at `(62 + col * 18, 19 + row * 18)`. Player inventory is at `(8 + col * 18, 84 + row * 18)`, and hotbar is at `(8 + hotbarSlot * 18, 142)`.
  - In `GuiVolCore.java`:
    - The power toggle button is at `(7, 6, 13, 13)`; when active, overlay UV `(12, 166, 13, 13)` is rendered.
    - Fuel progress bar is scaled to 31px and rendered at `(38, 59 - chargeBar)` with UV `(0, 197 - chargeBar, 12, chargeBar)`.
    - GUI height is 166.
- **Decision & Fix**:
  - In `LegacyCoreMenu.java`, realigned 3x3 slots from `(18, 22)` to `(62 + col * 18, 19 + row * 18)`, player inv to `(8 + col * 18, 84 + row * 18)`, and hotbar to `(8 + hotbarSlot * 18, 142)`.
  - In `LegacyCoreScreen.java`, updated `imageHeight = 166`, charge bar scale to 31px at `(38, 59 - chargeBar, 12, chargeBar)`, added power button click and active overlay UV `(12, 166, 13, 13)`.

### 2.3 Crane Terminal Menu & Screen
- **Legacy Truth**:
  - In `GuiCrane.java:289-293`, power button is at `(7, 6, 13, 13)` with active overlay UV `(176, 0, 13, 13)`.
  - In `GuiCrane.java:395-407`, left-clicking wait mode button increments mode; right-clicking decrements mode.
- **Decision & Fix**:
  - In `CraneBlockEntity.java`, added `active` state tracking, `isActive()`, `toggleActive()`, and directional `cycleWaitMode(boolean forward)`.
  - In `CraneTerminalMenu.java`, added `BUTTON_POWER = 9` and `BUTTON_WAIT_MODE_DECR = 10`.
  - In `CraneTerminalScreen.java`, added power overlay rendering, power toggle click handler at `(7, 6, 13, 13)`, right-click decrement in wait mode click handler, and power status tooltip.

### 2.4 Desk Reference Screen Multiplayer Safety
- **Legacy Truth**:
  - Guide book reading and radar contact scan.
- **Decision & Fix**:
  - Verified and confirmed that client queries accessing server state (such as `getWorldBlockedClasses` and `collectFacilityContacts`) in `DeskReferenceScreen.java` safely guard `this.minecraft.getSingleplayerServer()` against `null`, preventing NPEs on dedicated multiplayer servers.

### 2.5 Confirmed No-Change Items (Full Parity Already Verified)
1. `GuiSmallShipyard` & `ContainerSmallShipyard` -> `SmallShipyardScreen` & `SmallShipyardMenu`: 100% parity verified.
2. `GuiRecipePaper` & `ContainerRecipePaper` -> `RecipePaperScreen` & `RecipePaperMenu`: 100% parity verified (ghost slots and NBT recipe sync).
3. `GuiShipInventory` & `ContainerShipInventory` -> `ShipInventoryScreen` & `ShipInventoryMenu`: Slot coordinates 100% identical; AI keybinds aligned.
4. `GuiFormation` & `ContainerFormation` -> `FormationScreen` & `FormationMenu`: 100% functional parity with modernized Forge widgets.
5. `GuiMorphInventory` & `ContainerMorphInventory` -> `MorphInventoryScreen` & `MorphInventoryMenu`: ~~100% functional parity~~（该Morph完整等价概括**已撤销**；获取/选择与空值语义以 [D-012](../../MIGRATION-DECISIONS.md#d-012) 为准，不否定本记录其它GUI工作）。
6. `GuiDesk` -> `DeskTerminalScreen` & `DeskReferenceScreen` & `WaypointTerminalScreen`: 100% functional parity.

## 3. Verification Evidence
- `./gradlew compileJava`: PASSED (0 errors, 0 warnings that block build).
- `./gradlew runGameTestServer`: PASSED (276/276 required tests passed, 0 failures, 0 regressions).
