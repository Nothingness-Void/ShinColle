# NeoForge 1.21.1 migration result

## Rendering backport and approved land legs

Forge rendering backport 1547691 is merged as the new parity base, without
force-push/rebase. There were no substantive behavior conflicts: overlapping
fixes were identical, renderer/test/import conflicts only required retaining
NeoForge APIs, and docs retained each branch's history. The existing NeoForge
dimensions test stays in NeoForgeCompatibilityGameTests, avoiding duplicate
registration. The new pure Hime policy and its state/land/water regression are
shared with Forge; no assertion was weakened. See updated decision.md and
MIGRATION-DECISIONS.md for the explicit user-approved visual deviation.

After the last source edit, JDK21 `./gradlew --no-daemon build`:

```text
> Task :verifyItemModelCoverage
> Task :check
> Task :build

BUILD SUCCESSFUL in 15s
5 actionable tasks: 3 executed, 2 up-to-date
```

`./gradlew --no-daemon runGameTestServer`:

```text
[04:17:48] [Server thread/INFO] [minecraft/GameTestServer]: ========= 352 GAME TESTS COMPLETE IN 21.87 s ======================
[04:17:48] [Server thread/INFO] [minecraft/GameTestServer]: All 352 required tests passed :)
BUILD SUCCESSFUL in 34s
8 actionable tasks: 1 executed, 7 up-to-date
```

CPU audit:

```text
All 45 model bodies match legacy after three API aliases and the approved Hime land-leg override
Shared pose, visibility flags and outer pre-render call order verified
Measured 1536 model/state/pose cases at shadow plane Y=0
Vertex audit passed; CSV: migration_batches/neoforge-1.21.1/ship-pose-audit-land-water.csv
```

Logs: /home/ubuntu/shincolle-121-land-water-{build,gametest,audit}.log.
New [land/water CSV](ship-pose-audit-land-water.csv) does not replace the old
768-case historical investigation or the limits documented in decision.md.
Forge evidence and review applicability are preserved in the merged
../rendering-backport-1.20.1 batch. Client evidence follows below.

The version/API port now compiles, builds, and passes the complete server
GameTest suite. This supersedes the earlier 146-error WIP checkpoint. It does
not establish client visual parity or validate an actual upgraded user world.
The PR #11 follow-up below records two fixes, an upstream cancellation defect,
and the latest 348-test result; original build evidence remains separately dated
by its code commit.

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
  the Minecraft item data fixer (3465 to the running version), preserving slot
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
  use a server-player fixture with a stub connection/no-op packet delivery instead of
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

For original port commit `f9b814a`, the last source edit was followed, in order,
by these commands using
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

## PR #11 review follow-up

Reviewed against the actual NeoForge 21.1.267 source artifact, not a newer
release. No changes to legacy sources, gameplay values, or existing assertions.

1. **Fixed — `f6db0ec`: custom recovery-drop item migration.**
   `ModEntityTypes.RECOVERED_SHIP_ITEM` creates `RecoveredShipItemEntity`, an
   `ItemEntity` subclass rather than a player/ship. The prior `Entity.load` HEAD
   gate excluded it. Vanilla schema `V1460.java:90` describes the `Item` field for
   `minecraft:item`, not this mod entity; inherited
   `ItemEntity.readAdditionalSaveData` (`ItemEntity.java:332–353`) parses current
   components directly. The hook now also accepts this specific custom drop.
   The regression runs old entity-chunk NBT through vanilla DFU (3465 to current)
   and `EntityType.loadEntityRecursive`, then checks egg ownership, ship level,
   nested equipment/slot/damage, already-modern round trips, and empty/unknown
   item fallback. This covers the upgrade path synthetically, not a real world.

2. **Not a ShinColle-specific bug — upstream NeoForge record retention remains.**
   `Player.hurt` delegates to `LivingEntity.hurt` (`Player.java:919`).
   `LivingEntity.java:1124` pushes a damage container; line 1125 returns false
   immediately when `CommonHooks.onEntityIncomingDamage` reports cancellation.
   The ordinary final pop is at line 1265, with no pop on that cancellation
   branch. `CommonHooks.java:295–296` simply posts the cancelable event and
   returns `isCanceled()`. The event's own documentation explicitly places
   cancellation before mitigation, so moving these attack-stage immunities to
   the later hurt/damage event would not preserve the old behavior.
   Diagnostic test commit **`c3c7611`** first proves ordinary zombie damage pops
   the record, then adds only a bare canceling listener: three real canceled
   generic hits leave three records. Three actual ring-fire, mounted-fall and
   mounted-suffocation cancellations each retain exactly the same three records.
   The fixtures clear their own retained records afterward. No production
   engine-stack workaround was added; a listener-side pop could also conflict
   with later listeners changing cancellation. Fixing global NeoForge lifecycle
   ownership is outside these mod review fixes. The leak is real, not dismissed
   as nonexistent, and is not claimed fixed.

3. **Fixed — `3a06c51`: enchanting classifications are not ore names.**
   NeoForge `Tags.Items.ENCHANTABLES` explicitly aggregates enchantable groups;
   `data/c/tags/item/enchantables.json` includes vanilla armor and sword groups.
   `CraneBlockEntity.matches` forwards the ore toggle to the affected helper.
   Exclude `c:enchantables` and its subgroups, alongside the existing vanilla
   enchanting and food exclusions. Other baseline tag matching is unchanged;
   this is not a replacement OreDictionary implementation. Tests assert that
   the helmet and sword really share the loaded NeoForge tag but do not match
   in either direction, while identical items, plankWood-style matching, the
   disabled ore toggle and empty templates retain their prior semantics.

After the final Java edit, using the same JDK 21 environment:

`./gradlew --no-daemon compileJava`:

```text
1 warning

BUILD SUCCESSFUL in 6s
2 actionable tasks: 1 executed, 1 up-to-date
```

The warning is the unchanged crane call to deprecated `FluidStack.isFluidEqual`;
no compiler errors. `./gradlew --no-daemon runGameTestServer`:

```text
[23:53:48] [Server thread/INFO] [minecraft/GameTestServer]: ========= 348 GAME TESTS COMPLETE IN 18.62 s ======================
[23:53:48] [Server thread/INFO] [minecraft/GameTestServer]: All 348 required tests passed :)

BUILD SUCCESSFUL in 33s
8 actionable tasks: 1 executed, 7 up-to-date
```

**348 passed, 0 failed** (previous 345 plus three review regressions/diagnostics).
No new `build` or client/visual/real-world validation is claimed for this review.

| Latest review log | SHA-256 |
| --- | --- |
| `/home/ubuntu/shincolle-pr11-review-compile.log` | `79a61874ca296c80370fcf33395eb1dbc430656004618e2eb15399c19f036cf1` |
| `/home/ubuntu/shincolle-pr11-review-gametest.log` | `7c4bab6fdd7e0c780b51db1d46da325ccc8e54adf31b26863c7568918f748c44` |

## Client rendering follow-up: ship placement

The missing legacy pre-render pose pass is reachable through the registered
`LegacyShipRenderer` -> `LivingEntityRenderer.render` ->
`LegacyShipPoseModel.renderToBuffer` -> `ModelBase.render` bridge. The wrapper
called only `render`; legacy `RenderLivingBase.doRender` called
`setRotationAngles` before `render`, and individual ship `render` methods call
it again inside their model-local scale. Those pose methods also translate the
matrix: for example, `ModelBattleshipRe.applyNormalPose` translates Y by 0.63,
and its dead pose by 1.13. Omitting the outer, unscaled pass therefore raises
the model by exactly that missing translation, independently of the entity
position and shadow. Restoring the old call order also restores the shared
sitting/riding pose transforms; no model-local offsets or gameplay values were
changed. Preserve Midway's separately rendered mount at its original matrix.

This is a pre-existing bridge omission, not a newly introduced 1.21 scale
change: the wrapper and renderer/catalog match the fixed 1.20.1 branch apart
from required API signatures. NeoForge 21.1.267 `LivingEntityRenderer` still
flips the matrix, calls `scale`, and translates by -1.501; its new entity scale
is 1.0 for the reproduced ships (also checked with the runtime attribute
command). Our renderer's `scale` only adjusts shadow radius, and it does not
override `setupRotations`. These checks disprove the proposed extra vanilla
scale/translation explanation rather than compensating for it with a guessed
global offset.

`shipRuntimeDimensionsRetainLegacyCatalogValues` exercises every ship spec,
all four hostile scale bands, standing and sitting: scale remains 1.0, actual
width/height/AABB and eye height match the legacy catalog. Existing class-size
and standing/sitting passenger/rider offset parity tests also pass unchanged.
Debug hitboxes remain grounded while the model placement is corrected.

The final source batch (including the separate block-render fix below) was
checked on JDK 21 with `./gradlew --no-daemon compileJava`:

```text
4 warnings

BUILD SUCCESSFUL in 6s
2 actionable tasks: 1 executed, 1 up-to-date
```

Warnings are deprecated mod-bus annotations (existing and new registrations),
not compilation errors. Full `./gradlew --no-daemon runGameTestServer`:

```text
[03:29:06] [Server thread/INFO] [minecraft/GameTestServer]: ========= 351 GAME TESTS COMPLETE IN 20.20 s ======================
[03:29:06] [Server thread/INFO] [minecraft/GameTestServer]: All 351 required tests passed :)

BUILD SUCCESSFUL in 32s
8 actionable tasks: 2 executed, 6 up-to-date
```

351 passed / 0 failed: previous 348 plus runtime dimensions and two block
render-resource/occlusion regressions. No existing assertions were weakened.
Logs: `/home/ubuntu/shincolle-render-{compile,gametest}.log`; SHA-256 respectively
`b4a5cce10a94b15851520f3968c5d13cefa15fa86ecac7ceeddacc2dc696551c`
and `96593ffffd4c7dd3325215d7018056d3a1ccea4e70a7fe8fb9d58df8c64b57c2`.

Targeted client check: Battleship Re, Isolated Island and Northern Princess,
with hitboxes, standing and sitting. Screenshots are recorded with the block
follow-up below. This is not a full per-variant/animation/glow/held-item visual
acceptance, nor a visual test of every riding/misc-model combination. Riding
offset coverage here is source comparison plus the existing full GameTests.

## Client rendering follow-up: special blocks and items

- Crown is `blockcrane`: a legacy baked JSON model, not an OBJ or TESR.
  Its model (and waypoint's) had no block-model parent, so the inventory
  renderer inherited identity display transforms and front lighting. Looking
  straight down Z flattens its thin frame to the gray side texture. Inherit
  `minecraft:block/block` for the normal 3D block-item view. Legacy JSON
  geometry/UVs/textures are otherwise byte-semantically unchanged.
- Desk and small shipyard used Java models `ModelBlockDesk` and
  `ModelSmallShipyard`, world TESRs and `RenderTileEntityItem`, installed by
  `ClientProxy.init`. The modern BERs already parse those same Java models,
  but inventory items used baked approximations instead of that renderer.
  Register a client-only `IClientItemExtensions` BEWLR for these two items;
  use `builtin/entity` item models, legacy block-item transforms, and dispatch
  to their existing BERs. The shipyard inventory selects the legacy active
  texture, as the old no-world tile did. Unknown/empty stacks simply draw
  nothing in this renderer.
- Both BERs flipped around X (`scale(1,-1,-1)`), whereas the TESRs rotated
  180 degrees around Z. Restore that rotation without changing facing tables.
  Desk also retained opaque/full-block lighting defaults: dispatcher light
  sampled inside it was black. Match the legacy non-opaque desk with
  `noOcclusion`; the parsed five-part model and its original 64x64 texture
  were not missing/corrupted (old and canonical modern PNG SHA-256 both
  `9470adea7e2e1780c8fb41cf7744985eb265b44342637d80385dbbda1493cd6e`).
- Audit: waypoint/crane/volcano core are baked blocks in the reference, not
  missing TESRs. Volcano core already inherits a cube model. Small shipyard
  shares the corrected item/orientation path. The large shipyard/vortex
  renderer and its lowercase textures already exist; it did not share the
  missing desk/small-shipyard inventory dispatch. No unrelated BER added.
- Removed all 16 case-colliding uppercase modern texture copies, including
  particles. Lowercase runtime references and assets are retained unchanged.
  The unused uppercase `GuiNameIcon2.png` differs from its lowercase sibling;
  reference audit found the runtime uses lowercase, so retain that current
  atlas rather than silently replacing it with the obsolete uppercase image.
  `processResources` lowercase-copy behavior and `src/main/**` are untouched.

Final-source checks are the compile/full GameTest results above, plus:

```text
Render resource checks passed: 0 case-fold collisions; legacy geometry, textures and item render paths intact
```

Command: `python3 src/modern/scripts/check_render_resources.py`.
The two new block GameTests cover non-opaque TESR shape, legacy model parts,
renderer marker/display parents, and canonical textures; client screenshots
are necessary to verify the actual draw path, not replaced by those assertions.

Targeted NeoForge client check passed: corrected standing/sitting ships and
grounded hitboxes; 3D crown/desk/shipyard hotbar items, desk/shipyard held items,
and placed desk, crane, waypoint, small shipyard and volcano core. Evidence:

- `/home/ubuntu/screenshots/shincolle-ships-before.png`
- `/home/ubuntu/screenshots/shincolle-ships-standing-after.png`
- `/home/ubuntu/screenshots/shincolle-ships-sitting-after.png`
- `/home/ubuntu/screenshots/shincolle-blocks-before.png`
- `/home/ubuntu/screenshots/shincolle-blocks-after.png`
- Client log: `/home/ubuntu/shincolle-render-client-fixed.log`

**Observed upgrade caveat:** the desk already saved in the test world retained
its old dark block-light cache until it was removed/replaced. A block/light
update restored the correct lit texture; newly placed desks render correctly.
This is not a claim that every previously saved chunk auto-relights. Full
old-world upgrade, Windows-native checkout/build, complete large-shipyard
multiblock visuals, all model/animation/rider combinations, and broad client
parity remain unverified. No new mechanics or model-local gameplay values.

## Destroyer Hime residual hover report

The reported girl is egg 29 / legacy class 27, not the FR models 31/33 used
in the earlier check. The apparent feet are her skirt; legacy state 0 hides
the legs and deliberately hovers in the fueled standing pose. No production
grounding override was made because it would change 1.12.2 visual behavior.
See [decision, full model audit and evidence](decision.md): all 45 model bodies
match reference, 64 variants/768 CPU pose cases measured, compileJava and full
**352/352** GameTests passed, and the client shows state 16's legs grounded
beside state 0's original hovering silhouette. Forcing the no-leg variant to
ground remains a parity decision requiring explicit approval, not a claimed fix.
