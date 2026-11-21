# NeoForge 1.21.1 migration checkpoint

This is an incomplete, non-runnable WIP, not a parity-validated port. Do not open
an existing world with this checkpoint.

## Base and environment

- Base: `origin/devin/parity-fix-integration`, requested commit `5b95cfe`.
- Branch: `devin/mc-1.21.1-neoforge`.
- Minecraft 1.21.1, NeoForge 21.1.267, ModDevGradle 2.0.148, Gradle 8.8.
- JDK: `/usr/lib/jvm/java-21-openjdk-amd64` (21.0.13.1); JDK 17 retained.
- No changes to `src/main/**`; no push.

## Applied adaptations

- Build, metadata and preparation documentation reapplied from the old WIP branch.
- Datapack directories renamed, common tags moved to `c:`, biome modifiers moved
  to `neoforge`, and initial recipe/advancement/condition JSON conversion applied.
- Modern source imports, deferred holders, registration entry points and tick
  events adapted; screens use `RegisterMenuScreensEvent`.
- Six packet types use `CustomPacketPayload` and `StreamCodec`, registered with
  `RegisterPayloadHandlersEvent`; payload fields and command dispatch retained.
- `LegacyItemData` wraps `DataComponents.CUSTOM_DATA`; converted writes publish
  the updated tag instead of mutating a copy. Ship tanks use a fluid component.
- Teitoku attachment serializer and block/item capability registration introduced.
- Synced entity data uses builders; initial entity, block, block entity, SavedData,
  model, particle and screen signature adaptations applied.
- Chunk tickets use a registered `TicketController`.

These describe implemented source changes, not runtime verification.

## Latest checks

`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon compileJava`:

```text
146 errors
28 warnings

> Task :compileJava FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileJava'.
> Compilation failed; see the compiler error output for details.

BUILD FAILED in 8s
2 actionable tasks: 1 executed, 1 up-to-date
```

The first compile had 1,763 errors. Intermediate checkpoints had 1,106, 548 and
188 errors. The latest complete compiler log is stored outside the repository at
`/home/ubuntu/shincolle-121-compile.log`.

`./gradlew --no-daemon cleanProcessResources processResources verifyItemModelCoverage`:

```text
> Task :cleanProcessResources UP-TO-DATE
> Task :processResources
> Task :verifyItemModelCoverage

BUILD SUCCESSFUL in 4s
3 actionable tasks: 2 executed, 1 up-to-date
```

JSON syntax validation: `JSON syntax OK: 136 datapack JSON files`.
This does not validate Mojang/NeoForge codecs or registry references.
GameTests cannot be run while their source and the mod fail `compileJava`.
No client/server launch, gameplay, old-save upgrade, packet round-trip or visual
verification has been performed.

## Remaining compiler work

Compiler diagnostics by source package (146 total):

| Package | Errors |
| --- | ---: |
| gametest | 76 |
| entity/ship | 19 |
| menu | 10 |
| morph | 8 |
| teitoku | 7 |
| client/screen | 5 |
| client/renderer/entity | 5 |
| entity/mount | 3 |
| client/model/legacy | 2 |
| entity/projectile | 2 |
| registry | 2 |
| crafting | 2 |
| item | 2 |
| client/model/legacy/compat | 1 |
| world | 1 |
| command | 1 |

- `TeitokuEvents`, `MorphDamageHandler`, related GameTests: removed
  `LivingAttackEvent`, `LivingHurtEvent`, `EntityItemPickupEvent` require careful
  event-phase/cancellation adaptation. Do not blindly map both damage hooks to
  `LivingIncomingDamageEvent`: timing relative to shields/armor differs.
- `LegacyShipEntity`, `MorphDamageHandler`, potion tests: replace removed
  `PotionUtils` with `PotionContents`, including area-effect cloud contents.
- `LegacyShipBehaviorCatalog`, `RecipePaperMenu`, recipe tests: `CraftingInput`,
  `SingleRecipeInput`, recipe holders and new assemble/remaining-item signatures.
- `LegacyEquipmentBuildHelper`, ship task code and tests: enchantment registry
  keys/holders and mutable enchantment components; mining tier API replacement.
- `MorphProfile`, Teitoku serialization, recipe paper and entity inventories:
  propagate `HolderLookup.Provider` through ItemStack/NBT serialization.
- Presentations/cache: component JSON serialization now requires registry lookup.
- Riding offsets/attachments, projectile gravity and final spawn signatures.
- Remaining model normals/tint signatures, text-field ticking and mouse scrolling.
- `ModBlocks`: new ore constructor order and abstract falling-block codec.
- GameTests: block interaction/load/save calls, mock players, command execution
  return handling, conditions codec, generic holder arrays, arrow constructor.

## Semantic and persistence work still required

No gameplay or numeric change is intended or approved. No unavoidable
player-visible change has been established. Behavior parity is not yet proven.

- Import old Forge capability data into the Teitoku attachment. The serializer
  currently writes the same inner keys but does not bridge the old outer
  `ForgeCaps` storage to NeoForge attachments.
- Convert old item-handler/fluid NBT and nested ItemStacks, not just new-world
  serialization. The new ship-tank fluid component does not yet import the old
  fluid-handler tag. Do not assume existing tank contents survive.
- Audit legacy ItemStack `hasTag` semantics against split vanilla/custom
  components, especially named items, damage, enchantments and ring state.
- Complete riding attachment integration so the retained numeric seat-offset
  helpers affect the new engine's actual passenger placement.
- Verify registry missing-value behavior (`getValue` versus defaulted registry
  `get`) and common tag mappings against the 1.20.1 baseline.
- Validate all datapack JSON through a real server reload; syntax checks alone
  cannot establish recipe, loot, advancement or worldgen validity.
- Run existing parity GameTests, packet normal/empty/error round trips, old-save
  upgrade cases, and actual client rendering checks once compilation is green.
