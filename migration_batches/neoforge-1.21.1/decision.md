# Residual floating report: Destroyer Hime

## Updated decision: user-approved land legs

The original investigation below found an intentional legacy hovering pose,
not another skipped model call. The user subsequently approved a narrow visual
deviation on both versions: Destroyer Hime only shows legs on land even with
bit 4 clear. In water it retains the legacy toggle; explicitly enabled legs
always show. Stored/default state, dimensions, geometry and gameplay stay intact.
Existing sitting/no-fuel branches are retained, not forced into standing.

The exact signal is the existing ent.getShipDepth(0) > 0, the model's own
legacy liquid classification. A live water check disproved vanilla isInWater
as sufficient: at the natural surface-floating Y=99.9 on a source at Y=99,
it still showed legs; the source's fluid-height test excludes that thin strip.
Positive legacy depth includes it. Both leg visibility and normal pose use
the same pure DestroyerHimePosePolicy; zero/negative depth means land, positive
depth retains the original liquid pose, and explicit bit 4 always wins.
Reachability: LegacyShipEntity.tick calls updateLegacyWaterState before its
client/server gate; BasicEntityShip.getShipDepth(0) forwards the resulting
legacy depth into the model. The saved natural water-surface position remains
Y=99.9 after the check, with stored state 0, yet the corrected renderer hides
the legs. Thus the visual test covers the real surface case, not an artificially
submerged/NoGravity stand-in.
No saved-state write, gameplay depth calculation or other model changes.
MIGRATION-DECISIONS.md and the
[Forge backport decision](../rendering-backport-1.20.1/decision.md) record this
as an explicit exception to strict legacy visual parity. The 768-case CSV below
is historical evidence; the new land/water audit measures 1536 cases.

## Identification and reachable path

- The screenshot's item label `驱逐舰姬` maps to `shipegg29` in `zh_cn.json`.
  `ShipEntitySpecs` maps egg **29**, legacy class **27**, to
  `EntityDestroyerHime` / `ModelDestroyerHime`. FR is a shared egg texture label,
  not a unique model identity. The earlier visual check covered eggs 31 and 33,
  not 29; its broad placement conclusion did not establish this model's behavior.
- `LegacyShipRenderer` uses the common `LegacyShipPoseModel` for this spec.
  `renderToBuffer` unconditionally calls its `setRotationAngles` before
  `render`, outside the latter's local scale, after `cae1ae9`.
  Destroyer Hime inherits the same `ShipModelBaseAdv.setRotationAngles` as the
  other advanced models. Its catalog entry has no extra vertical offset.
- `ModelDestroyerHime.showEquip` hides **both entire leg hierarchies** when
  model-state bit 4 is clear. The bottom visible in the screenshot is the skirt,
  not the feet. State 0 is the legacy default (`ShipStateHandler` initializes
  ModelState to 0; `EntityDestroyerHime` does not override it; a new specific
  `ShipSpawnEgg` leaves it unchanged unless restoring saved Attrs2 state).
  Modern `applySpawnSpec` and the emotion bridge retain this default.
- Its normal pose explicitly checks `!sitting && !showLeg` under the legacy
  comment `水上漂浮`, applying only the small animated bob. The fixed 0.59
  translation belongs to **applyDeadPose**, not the normal standing pose.
  Thus restoring the outer pose pass cannot ground a deliberately hovering,
  fueled, leg-hidden model. `applyNormalPose`, `showEquip`, geometry, scale 0.38
  and offsetY 2.47 match the legacy source exactly.
- Counter-check: enabling only bit 4 draws the missing legs and brings their
  soles to the ground, without changing the renderer/entity position. Sitting
  also selects a different legacy pose instead of retaining the standing gap.
  The client reproduces the reported silhouette with state 0 and the grounded
  version with state 16, at identical entity Y. The user's actual entity NBT
  was not supplied, so its exact fuel/state values cannot be read from the image.

Reference entry points are the read-only `src/main/java/com/lulan/shincolle/`
`entity/hime/EntityDestroyerHime.java`, `handler/ShipStateHandler.java`,
`item/ShipSpawnEgg.java`, `client/render/RenderShipEntity.java` and
`client/model/ModelDestroyerHime.java`.

## All-model audit and measurement

`src/modern/scripts/check_ship_pose_parity.py` verifies **all 45 registered model
class bodies** against their legacy sources, ignoring package/import/whitespace
and exactly three API aliases: IFloatingEntity -> IShipFloating,
getEntityDepth -> getShipDepth, GL11.glTranslatef -> GlStateManager.translate.
It also compares the shared advanced pose method and visibility-flag helper,
and checks that the wrapper retains the outer pose-before-render order.
No additional unported per-model transform was found.

The CPU-only Java auditor instantiates the model for **all 64 registered egg
variants**, each at model states 0, 16 and 255, standing/sitting and fueled/no-fuel:
**768 cases**, using a controlled BasicEntityShip surrogate rather than real
world entities and their complete type-specific compatibility wrappers.
It measures transformed vertices after the vanilla flip/-1.501 translation
and both legacy pose passes, with the entity/shadow plane at Y=0. All meshes
are nonempty with finite vertices. Field-tagged leg boxes provide a separate
leg minimum when named and visible; `hidden_or_unnamed` is not universally a
claim that the model has no feet. These are geometry bounds, not alpha-aware
pixel measurements or a rule to snap every protruding weapon/garment to Y=0.

For Destroyer Hime on dry land at age 100, no walking/attack/riding, scale band 0:

| State | Sitting | Fuel | Lowest visible Y | Lowest leg Y |
| --- | --- | --- | --- | --- |
| 0 | no | yes | **+0.424872** | hidden (0 leg vertices) |
| 16 | no | yes | -0.015195 | **-0.015195** (96 leg vertices) |
| 0 | yes | yes | -0.052077 | hidden |
| 16 | yes | yes | -0.077353 | -0.077353 |
| 0 | no | no | -0.212886 | hidden |
| 16 | no | no | -0.212886 | -0.005833 |

The roughly half-block apparent gap is reproduced numerically. It is absent
when the existing leg option is enabled. Among the default-state, fueled,
standing samples, **ModelDestroyerHime alone** has lowest visible geometry
above +0.15. That is the affected model for this reported silhouette, not a
newly discovered missed API adaptation. All 64 variants/45 classes are present
in [the complete CSV](ship-pose-audit.csv).

The dynamic sample is not exhaustive over continuous animation time, walking,
water, riding/morph, hostile scale bands above 0, or every bit combination;
the independent source-body comparison covers the model branches themselves.
An actual 1.12.2 client was not launched; the reference comparison is source,
and the CPU measurement replays the current compatibility model pipeline.

## Checks and evidence

Commands and acceptance are linked from [the test matrix](../../MIGRATION-TEST-MATRIX.md#destroyer-hime-hover-audit).
After the last GameTest edit, JDK 21 compileJava:

```text
> Task :compileJava

BUILD SUCCESSFUL in 6s
2 actionable tasks: 1 executed, 1 up-to-date
```

Full GameTest:

```text
[03:52:11] [Server thread/INFO] [minecraft/GameTestServer]: ========= 352 GAME TESTS COMPLETE IN 21.24 s ======================
[03:52:11] [Server thread/INFO] [minecraft/GameTestServer]: All 352 required tests passed :)

BUILD SUCCESSFUL in 38s
8 actionable tasks: 1 executed, 7 up-to-date
```

Final CPU/source audit:

```text
All 45 registered model bodies match legacy after three API aliases
Shared pose, visibility flags and outer pre-render call order verified
Measured 768 model/state/pose cases at shadow plane Y=0
Vertex audit passed; CSV: migration_batches/neoforge-1.21.1/ship-pose-audit.csv
```

`destroyerHimeRetainsLegacyLegVisibilitySwitch` adds coverage of default state,
model identity, fueled normal-pose gating and bit preservation while standing
and sitting. Existing assertions are unchanged. No complete build rerun claimed.

Targeted client comparison passed: same egg 29, same Y, state 0 versus 16,
standing then sitting, with grounded hitboxes and shadows:

- `/home/ubuntu/screenshots/shincolle-ddhime-standing-leg-switch.png`
- `/home/ubuntu/screenshots/shincolle-ddhime-sitting-leg-switch.png`
- `/home/ubuntu/shincolle-ddhime-client.log`

| Evidence | SHA-256 |
| --- | --- |
| `/home/ubuntu/shincolle-ddhime-compile.log` | `7171d140f2bacacf78225742092b2f38ecbdb4f7515565cf1089c7549f3041ff` |
| `/home/ubuntu/shincolle-ddhime-gametest.log` | `fd1c87274d9196e1b9e1cafa3ce5b3262372d10c23d9613adc04bc47d60ab432` |
| `ship-pose-audit.csv` | `f1776696e7121e3a3df2581bf7bf6d90451269e9ed6ff1b8371e2ac6a9752e15` |
