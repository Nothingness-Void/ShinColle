# ShinColle 1.20.1 Porting Notes

This branch is the first staged port from Minecraft 1.12.2 to Minecraft 1.20.1 on Forge 47.4.18.

## What changed in this baseline

- Switched the project to the official ForgeGradle 6 / Gradle 8.8 toolchain.
- Raised the Java target from 8 to 17.
- Replaced `mcmod.info` metadata with `mods.toml` and `pack.mcmeta`.
- Added a modern `@Mod` entrypoint under `src/modern/java`.
- Kept the legacy 1.12.2 source tree untouched in `src/main/java` so it can be ported subsystem by subsystem.

## Content migrated in the first batch

- Added a modern custom creative tab.
- Ported the first resource items as standalone 1.20 items:
  - `abyssmetal`, `abyssmetal1`
  - `abyssnugget`, `abyssnugget1`
  - `ammo`, `ammo1`, `ammo2`, `ammo3`
  - `grudge`, `grudge1`
- Ported additional simple misc items:
  - `combatration` to `combatration5`
  - `instantconmat`
  - `recipepaper`
  - `repairgoddess`
  - `shiptank` to `shiptank3`
  - `toyairplane`
- Registered the remaining legacy item catalog in the modern item registry:
  - utility items such as `bucketrepair`, `modernkit`, `ownerpaper`, `optool`, `pointeritem`, `targetwrench`, `trainingbook`
  - equipment variant lines such as `equipairplane`, `equipammo`, `equiparmor`, `equipcannon`, `equipcatapult`, `equipdrum`, `equipmachinegun`, `equipradar`, `equiptorpedo`, `equipturbine`
  - all legacy ship spawn egg item IDs as temporary placeholders until entity migration
- Ported initial placeholder equipment items:
  - `equipcompass`
  - `equipflare`
  - `equipsearchlight`
- Ported the first simple blocks:
  - `blockabyssium`
  - `blockgrudge`
  - `blockgrudgexp`
  - `blockpolymetalore`
  - `blockpolymetalgravel`
  - `blockvolblock`
- Ported the remaining legacy block IDs into modern registries as placeable blocks:
  - `blockcrane`, `blockdesk`, `blockframe`
  - `blockgrudgeheavy`, `blockgrudgeheavydeco`
  - `blockpolymetal`
  - `blocksmallshipyard`
  - `blockvolcore`
  - `blockwaypoint`
  - internal helper blocks: `blocklightair`, `blocklightliquid`
- Added modern lowercase block resources for the expanded block set:
  - blockstates, block models, item models, and loot tables
  - migrated legacy block textures into `assets/shincolle/textures/block/*` lowercase naming
- Added temporary interactive block feedback for feature-heavy blocks:
  - crane / desk / shipyard / volcano core / waypoint / polymetal / heavy grudge blocks now show explicit migration-status messages when used.
- Ported the first interactive item GUI:
  - `recipepaper` now opens a modern `Menu + Screen` pair and saves its ghost recipe grid back into item NBT.
- Ported additional lightweight item-side GUIs:
  - `deskitembook` and `deskitemradar` now open modern placeholder screens that keep their GUI path alive while the admiral desk systems are rebuilt.
- Ported the first fluid utility item:
  - `shiptank` to `shiptank3` now use modern item fluid capabilities with capacity tooltips and basic fill/place interactions.
- Restored self-contained item behaviors where possible:
  - `pointeritem` now stores a modern mode state in NBT and swaps item models by mode.
  - `ownerpaper` now alternates signatures between two stored owner slots.
  - `marriagering` now toggles an active state and only glows while active.
  - `kaitaihammer` now behaves like a reusable crafting tool with durability loss.
  - `bucketrepair`, `modernkit`, and `trainingbook` now have proper use animations plus explicit port-status feedback.
- Added basic tags, loot tables, and model resources for that subset.
- Added a dedicated `textures/ported/` path to avoid Windows case-collision issues while preserving legacy assets in place.

## Why the legacy code is not compiled yet

The current codebase depends heavily on APIs removed or redesigned after 1.12.2:

- `@SidedProxy`, `@Mod.EventHandler`, and the old pre/init/post-init lifecycle
- `SimpleNetworkWrapper` channel registration
- `NetworkRegistry.INSTANCE.registerGuiHandler` and container GUI flow
- `GameRegistry.registerTileEntity`, `EntityRegistry.registerModEntity`, `RenderingRegistry`
- `ModelLoader` item/block model hooks
- `TileEntitySpecialRenderer`
- widespread direct `IInventory` and `ITickable` usage

## Recommended migration order

1. Blocks, items, sounds, and block entities to `DeferredRegister`
2. Menus and screens to the modern menu/screen system
3. Packets to `SimpleChannel`
4. Block entity renderers and entity renderers
5. Capabilities, saved data, and attachments
6. Worldgen, recipes, and data-driven content
7. Entity AI and behavior fixes against 1.20 mappings

## Audit snapshot

- Java source files: 483
- Resource files: 566
- Largest code areas: `client` (130 files), `entity` (109 files), `item` (42 files), `handler` (42 files)

This baseline is intended to compile and boot a clean Forge 1.20.1 workspace first, then restore gameplay features in controlled batches.
