# ShinColle 1.20.1 Porting Notes

This branch is the first staged port from Minecraft 1.12.2 to Minecraft 1.20.1 on Forge 47.4.18.

Before continuing any migration work, re-read `PORTING-MISTAKES.md`.
That file is the persistent guardrail for mistakes already made during this port.
For the newest implementation handoff, read `AI-HANDOFF.md`.

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
  - all legacy ship spawn egg item IDs
- Restored the full legacy equipment item line as modern standalone data items:
  - equipment variant lines such as `equipairplane`, `equipammo`, `equiparmor`, `equipcannon`, `equipcatapult`, `equipdrum`, `equipmachinegun`, `equipradar`, `equiptorpedo`, `equipturbine`
  - special equipment items such as `equipcompass`, `equipflare`, and `equipsearchlight`
  - extracted legacy stat tables, tooltip metadata, creative-tab ordering, and modern item-model coverage for the whole equipment catalog
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
  - `blockvolcore`
  - `blockwaypoint`
  - internal helper blocks: `blocklightair`, `blocklightliquid`
- Added modern lowercase block resources for the expanded block set:
  - blockstates, block models, item models, and loot tables
  - migrated legacy block textures into `assets/shincolle/textures/block/*` lowercase naming
- Ported the modern sound-event registry for legacy ship and item feedback:
  - fixed ship voice/event sounds such as idle, hurt, death, marry, feed, bell, and kaitai
  - known ship-specific voice variants already present in `sounds.json` are exposed through the new registry layer
- Added temporary interactive block feedback for feature-heavy blocks:
  - volcano core / polymetal / heavy grudge blocks now show explicit migration-status messages when used.
- Started the first persistent block-entity migration:
  - `blockdesk` now uses a modern `BlockEntityType`
  - right clicking the block opens a block-backed placeholder desk screen
  - `blocksmallshipyard` now uses a modern `BlockEntityType`, real inventory slots, a restored build GUI, active-state smoke, and old-style ship/equip loop modes
  - small shipyard output now creates material-tagged `smallegg` items again, and those eggs once more roll their ship result from the old weighted construction table instead of the flat modern random pool
  - sneak-right-click cycles between radar and logbook desk modes and saves that mode in block NBT
- Started the second persistent block-entity migration:
  - `blockwaypoint` now uses a modern `BlockEntityType`
  - placed waypoints save owner, stay-time mode, last waypoint, next waypoint, and paired container in block NBT
  - right clicking a waypoint with `targetwrench` cycles stay time and prints the saved route summary
  - right clicking the block normally now opens a lightweight waypoint terminal that shows owner, route links, paired container, and stay time controls
  - sneak-right-clicking two valid targets with `targetwrench` now links waypoint -> waypoint or waypoint -> container fully server-side without the legacy packet layer
- Started the third persistent block-entity migration:
  - `blockcrane` now uses a modern `BlockEntityType`
  - cranes now persist owner, paired container, last/next route nodes, wait mode, load/unload toggles, and redstone/liquid/energy modes
  - right clicking the block opens a modern crane terminal instead of only showing a placeholder message
  - the crane terminal now includes the legacy 9x2 filter grid as persistent ghost slots, plus player inventory access for configuring those filters
  - left click on a crane filter slot stores a normal filter template, right click stores a one-item inverted filter template, and empty-hand click clears the slot
  - `targetwrench` pairing now treats waypoint and crane as the same modern route-node contract, so crane <-> container and crane <-> waypoint links can be stored again
  - crane and waypoint block models now use custom JSON shapes instead of fallback full cubes
- Started the first reusable ownership/persistence slice:
  - route-node block entities now share a modern owner data model (`UUID + display name + edit checks + NBT serialization`)
  - this replaces duplicated owner handling across migrated block entities and forms the first base for later admiral data and ship ownership migration
  - that owner model now also persists old-style `playerUID`, so migrated blocks and ships can keep converging toward the original `CapaTeitoku` / team-data design instead of staying UUID-only
- Ported the first interactive item GUI:
  - `recipepaper` now opens a modern `Menu + Screen` pair and saves its ghost recipe grid back into item NBT.
- Ported additional lightweight item-side GUIs:
  - `deskitembook` and `deskitemradar` now open modern placeholder screens that keep their GUI path alive while the admiral desk systems are rebuilt.
- Added modern sound feedback to the currently ported item interactions so placeholder flows still have audible confirmation.
- Ported the first fluid utility item:
  - `shiptank` to `shiptank3` now use modern item fluid capabilities with capacity tooltips and basic fill/place interactions.
- Restored self-contained item behaviors where possible:
  - `pointeritem` now stores a modern mode state in NBT and swaps item models by mode.
  - `ownerpaper` now alternates signatures between two stored owner slots.
  - `marriagering` now toggles an active state and only glows while active.
  - `kaitaihammer` now behaves like a reusable crafting tool with durability loss.
  - `targetwrench` now stores its selected pairing target in item NBT and can clear that selection in-air.
  - `bucketrepair`, `modernkit`, and `trainingbook` now have proper use animations plus explicit port-status feedback.
- Restored the legacy equipment item-side presentation layer:
  - equipment tooltips now show extracted stat lines, construction info, enchant category, and carrier restrictions again
  - the creative tab order now follows the old grouped equipment ordering instead of raw registry order
- Started the first modern ship entity baseline:
  - added a modern generic ship entity type plus synced legacy egg-meta / class-id variant data
  - all currently registered `smallegg`, `largeegg`, and `shipegg*` items now deploy real entities instead of placeholder items
  - friendly variants can be claimed by the spawning player and toggled between escort / standby with empty-hand interaction
  - claimed friendly variants now follow their owner again, can teleport back into escort range when they fall too far behind, and assist when the owner attacks or is attacked
  - hostile and friendly variants now recognize opposing ship factions and can engage each other through the same shared modern entity baseline
  - hostile `(Mob)` egg variants now deploy aggressive mobs through the same modern entity baseline
  - added the first client entity renderer using legacy ship textures through a lowercase-safe modern texture bridge
  - claimed ships now carry a persistent modern 6-slot equipment column plus 18-slot cargo hold saved in entity NBT
  - sneak-right-clicking an owned ship now opens a modern ship inventory screen that reuses the old `GuiShipInventory` texture and restores the first real ship-side GUI flow
  - the new ship menu includes entity-backed equipment slots, cargo slots, health/status readouts, and an in-GUI escort / standby toggle button
  - ships now persist modern runtime state for level, morale, marriage, and modernization bonus progress
  - training books, modernization kits, repair buckets, combat rations, ownership papers, wedding rings, repair goddesses, and pointer caress mode now all work again on friendly ships
  - legacy equipment mounted in ship equipment slots now feeds back into ship stats through a restored 1.12.2-style stat pipeline instead of an ad-hoc modern formula layer
  - friendly ships now rebuild `raw -> equip -> morale -> potion -> formation -> buffed` attributes from restored legacy base tables and morale tables before syncing max HP / movement / melee damage back into modern entity attributes
  - melee combat now uses a first restored legacy combat slice for miss, critical, double-hit, triple-hit, dodge, defense reduction, player damage limiting, and HPRES-based healing
  - ship combat routing now follows restored legacy attack families instead of forcing every variant through one melee shell:
    - destroyers / submarines / cruisers / battleships use light-heavy cannon compatibility attacks with legacy cooldowns and range
    - carriers use air attack compatibility loops again
    - mixed hime / installation variants such as airfield, harbour, northern, and midway now run both cannon and air attack baselines together
  - heavy cannon and air attacks now spawn a modern compatibility projectile entity instead of resolving only as silent server-side direct damage:
    - heavy launch miss handling now behaves like a launch deviation instead of incorrectly collapsing to zero damage
    - carrier / air attacks now travel as item-rendered projectile entities that can steer toward their intended target
  - the ship inventory equipment column now enforces carrier-only / not-for-carrier style restrictions based on restored legacy equipment metadata
  - escort-mode friendly ships now treat vanilla hostile mobs as valid combat targets again, can auto-acquire nearby `Enemy` mobs around the owner/ship, and use archetype-scaled melee reach instead of a single flat vanilla contact distance
  - repair goddesses can be stored in ship cargo and now prevent a fatal hit once
  - kaitai hammer now works on owned ships again as a direct dismantle action
- Started the first old-version-driven admiral / Teitoku framework slice:
  - added a modern `TeitokuData` capability based on the old `CapaTeitoku` field set instead of a new ad-hoc player-data model
  - the migrated field slice now includes player name, stable `playerUID`, ring state, marriage count, boss/team cooldowns, collected ship/equipment lists, and custom target-class storage
  - added server `SavedData` that assigns and persists stable `playerUID` values by player UUID, so ships and owner-tagged blocks can once again converge on the old integer owner key the legacy code expects
  - added the first modern `SimpleChannel` packet path and client sync for Teitoku data on login, respawn, and dimension change
  - marriage-ring state now syncs back into Teitoku data, successful marriages now increment admiral marriage count, and friendly ship deployment/marriage paths now record collected-ship state again
  - owner-tagged route nodes, the small shipyard, and friendly ships now all persist `OwnerUID` alongside `OwnerUuid` / `OwnerName`, keeping the migration aligned with the old ownership / team / formation dependency chain
- Added basic tags, loot tables, and model resources for that subset.
- Added a dedicated `textures/ported/` path to avoid Windows case-collision issues while preserving legacy assets in place.
- Added a lowercase-safe `textures/entity/modern/` bridge so modern resource locations can safely reuse the legacy entity texture set.

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
4. Capabilities, saved data, and attachments
5. Entity AI, renderers, and gameplay behavior fixes against 1.20 mappings
6. Worldgen, recipes, and data-driven content

## Audit snapshot

- Java source files: 483
- Resource files: 566
- Largest code areas: `client` (130 files), `entity` (109 files), `item` (42 files), `handler` (42 files)

This baseline is intended to compile and boot a clean Forge 1.20.1 workspace first, then restore gameplay features in controlled batches.
