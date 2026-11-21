# NeoForge 1.21.1 migration result

The version/API port now compiles, builds, and passes the complete server
GameTest suite. This supersedes the earlier 146-error WIP checkpoint. It does
not establish client visual parity or validate an actual upgraded user world.

## Target and boundaries

Preserve the fixed 1.20.1 / 1.12.2 gameplay, numbers and timing; add no mechanics.
Scope: modern code/resources, required build metadata and migration documents.
Acceptance: JDK 21 `compileJava`, `build`, and the full NeoForge
`runGameTestServer`, with existing assertions retained and legacy-data tests
added. Completing historical feature gaps, client visual approval, third-party
integration and an actual old-world upgrade are not claimed by this batch.

## Base and environment

- Base: `origin/devin/parity-fix-integration`, requested commit `5b95cfe`.
- Branch: `devin/mc-1.21.1-neoforge`.
- Minecraft 1.21.1, NeoForge 21.1.267, ModDevGradle 2.0.148, Gradle 8.8.
- JDK: `/usr/lib/jvm/java-21-openjdk-amd64` (21.0.13.1); JDK 17 retained.
- Code commit: `f9b814a682993d69e08692f69cc98965156f122a`, pushed to the target
  branch after all three checks passed. Documentation follows in a second push.
- No changes to `src/main/**`, including relative to the fixed parity base.

## Applied adaptations

- Build, metadata and preparation documentation reapplied from the old WIP branch.
- Datapack directories renamed, common tags moved to `c:`, biome modifiers moved
  to `neoforge`, and recipe/advancement/condition JSON formats adapted.
- Modern source imports, deferred holders, registration entry points and tick
  events adapted; screens use `RegisterMenuScreensEvent`.
- Six packet types use `CustomPacketPayload` and `StreamCodec`, registered with
  `RegisterPayloadHandlersEvent`; payload fields and command dispatch retained.
- `LegacyItemData` wraps `DataComponents.CUSTOM_DATA`; converted writes publish
  the updated tag instead of mutating a copy. Ship tanks use a fluid component.
- Teitoku attachment serializer and block/item capability registration introduced.
- Synced entity data uses builders; entity, block, block entity, SavedData,
  model, particle and screen signature adaptations applied.
- Chunk tickets use a registered `TicketController`.

- Recipe holders/inputs, enchantment holders, potion components, renderer and
  screen signatures, spawn/gravity signatures and falling-block codecs adapted.
- `processResources` lowercase resource copying, `syncGameTestStructures`,
  `verifyItemModelCoverage` and all four run types remain in place.

## Compatibility choices and runtime fixes

- **Damage timing:** the user-approved `LegacyHurtMixin` targets both
  `LivingEntity.actuallyHurt` and `Player.actuallyHurt`, immediately before the
  active damage-container read feeding armor reduction. It posts the mutable,
  cancelable `LegacyLivingHurtEvent`, updates the active amount and returns for
  cancellation/nonpositive damage. The configuration is registered in
  `META-INF/neoforge.mods.toml`; the source comment explains the missing public
  post-shield/pre-armor NeoForge event. `LivingIncomingDamageEvent` remains the
  separate old attack-stage adapter, not a replacement for this hurt stage.
  Real `hurt` tests cover both vanilla implementations, mutation before armor,
  cancellation, zero damage, and shield blocking before the restored stage.
- **Player data:** `LegacyEntityDataMixin` imports outer
  `ForgeCaps.shincolle:teitoku` after `Entity.load`, retaining the inner
  `TeitokuExtProps` wrapper through `TeitokuData.loadFromTag`. A serialized modern
  attachment wins over a stale old copy. An absent capability retains the old
  default UID of `-1`, not a newly invented zero default. Providers are propagated
  through Teitoku and MorphProfile equipment reads/writes.
- **Items:** `LegacyItemData` upgrades nested old `Count`/`tag` ItemStacks through
  the Minecraft item data fixer (3700 to the running version), preserving slot
  metadata and unrelated custom data. Entry points include player/ship load,
  block-entity inventories, MorphProfile equipment and recipe-paper reads.
  Empty stacks serialize without throwing; unknown items retain vanilla's empty
  fallback. Custom-data writes must publish the copied tag; OwnerPaper signing
  was corrected accordingly.
- **Fluids:** old ship-tank `FluidName`/`Amount`/`Tag` imports into the fluid
  component once, preserving fluid identity, amount and custom data. Consuming
  the old field prevents refilling after the tank is drained; an existing modern
  component wins. `LegacyFluidData` also bridges the old machine tank format for
  cranes and both shipyard implementations; modern fluid fields take precedence.
  Keep the legacy machine-interaction target as the tank item itself, rather
  than silently enabling machine-to-item fluid transfer as a new mechanic.
- **NBT/positions:** NBT matching includes migrated native enchantment/components
  while retaining the old independent damage toggle. A successful equipment
  enchantment roll replaces, rather than merges, previous enchantments.
  Waypoint/crane coordinates accept old XYZ compounds and new int arrays.
  Wrench packet validation accepts the new int-array encoding written by 1.21.
- **Tags:** recipe/task matching excludes newly introduced broad food/animal-food
  groups, `minecraft:meat`, `minecraft:wolf_food` and `minecraft:enchantable/*` as
  legacy OreDictionary equivalents. Otherwise raw and cooked beef shared new
  tags and cooking moved its input as though it were output. Existing ShinColle
  food tags remain eligible; recipes/resources use the migrated `c:` paths.
- **Riding:** `LegacyRiding`, ship attachment points and existing custom
  `positionRider` paths use the old numeric ship/player offsets. Mount, hit-zone,
  owner and skill-host parity tests pass without changing seat values.
- **Projectile construction:** the default visual item is available before
  synced entity data is initialized, as required by 1.21's projectile constructor.
- **GameTest API/fixtures:** retain the baseline mock-player game modes explicitly.
  NeoForge FakePlayer now refuses riding and menu opening, so those assertions
  use a connected server-player fixture with no-op packet delivery instead of
  bypassing production checks. Resource assertions inspect the new valid paths.
  The 1.21 GameTest enclosure placed barriers through Nagato's old out-of-template
  corridor (verified in the saved region); moving the corridor inside the 9x9
  template retains its four-block distance and all navigation assertions.
  Auto-pump fixtures use per-test X/Z origins instead of sharing persistent
  absolute coordinates; isolated-source removal, deep-water retention and XP
  assertions are unchanged. No production navigation/pumping rules were changed.
- **Pack metadata:** resource pack format 34 is retained, with supported formats
  34–48 so the combined mod pack also declares 1.21.1's data-pack format 48.

## Verification

The last source edit was followed, in order, by these commands using
`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` and its `bin` first on `PATH`:

`./gradlew --no-daemon compileJava`:

```text
> Task :compileJava
Note: /home/ubuntu/repos/ShinColle/src/modern/java/com/lulan/shincolle/gametest/NeoForgeCompatibilityGameTests.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.

BUILD SUCCESSFUL in 5s
2 actionable tasks: 1 executed, 1 up-to-date
```

`./gradlew --no-daemon build`:

```text
> Task :verifyItemModelCoverage
> Task :check
> Task :build

BUILD SUCCESSFUL in 4s
5 actionable tasks: 2 executed, 3 up-to-date
```

`./gradlew --no-daemon runGameTestServer`:

```text
[23:40:27] [Server thread/INFO] [minecraft/GameTestServer]: ========= 345 GAME TESTS COMPLETE IN 19.26 s ======================
[23:40:27] [Server thread/INFO] [minecraft/GameTestServer]: All 345 required tests passed :)

BUILD SUCCESSFUL in 31s
8 actionable tasks: 1 executed, 7 up-to-date
```

Result: **0 compile errors; build passed; 345/345 required GameTests passed,
0 failed** (340 existing plus five new compatibility tests). `build` has no
separate Java unit-test sources; the runtime assertions are the GameTests.
No assertion was weakened or required test disabled. Earlier failures were
fixed; this is not a claim that the first runtime attempt was green.

Local evidence (outside the repository, latest logs only):

| File | SHA-256 |
| --- | --- |
| `/home/ubuntu/shincolle-121-compile.log` | `e139f7373aac7f264b9ac6237d2dabcc996d04457ae29078c7cff42802724616` |
| `/home/ubuntu/shincolle-121-build.log` | `1e27898d872c9f46d75ce3eaf6cc069171be2db2e0987da7741612ae863afccd` |
| `/home/ubuntu/shincolle-121-gametest.log` | `9d9157383061588aed5dd9dec6de9ad54f678efe639e0c5bcec58b4dbd22c2cb` |
| `build/libs/shincolle-0.2.0-alpha.jar` | `a6e253030ba12d49c9e389e19987ad8c4a736f8ddec5949925e4f37c179757fa` |

The unknown-item error for `shincolle:missing_item` is intentional negative-test
coverage of vanilla's empty fallback, not a missing registered production item.
The development Mixin log reports a JAVA_21 compatibility-level diagnostic;
configuration loading and both damage targets were exercised successfully.

## Unavoidable differences and unverified work

No unavoidable player-visible gameplay difference has been established or
accepted. The port retains the fixed baseline's scope and existing limitations,
not a claim of complete 1.12.2 feature parity.

- Client startup/rendering, GUI hit zones, models/Glow/held items, HUD and particle
  visuals are **visually unverified**; no screenshots were captured.
- An actual 1.20.1 world/player upgrade, full restart and ticket lifecycle, and
  real client/server multiplayer transport have not been exercised. The new
  tests use targeted synthetic legacy NBT, not a user's complete world. Back up
  before attempting an actual upgrade.
- Real Curios/other third-party interoperability and external datapacks,
  including their tag/component edge cases, remain unverified.
- The fixed baseline's particle/config/morph/3D-navigation gaps remain as
  documented in `CURRENT_STATE.md`; this version upgrade does not complete them.
