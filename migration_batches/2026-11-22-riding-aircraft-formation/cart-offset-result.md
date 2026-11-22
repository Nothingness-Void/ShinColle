# Minecart passenger offset comparison

## Verified regression and minimal correction

`EntityDestroyerShimakaze -> BasicEntityShipSmall -> BasicEntityShip ->
EntityTameable -> EntityAnimal` inherits vanilla 1.12.2
`EntityAnimal.getYOffset() = 0.14D`. None of those mod classes overrides it.
Checking only `Entity.getYOffset() = 0` misses the animal override. Modern
`LegacyShipBehaviorCatalog.myRidingOffsetFor` incorrectly used zero for the
default. Restore `0.14` for friendly ships and retain `0` for hostile ships
(legacy `BasicEntityShipHostile -> EntityMob`, not `EntityAnimal`). Explicit
Ne `0.3`, Northern Hime and SSNH `0.25` overrides are unchanged.

### Passenger position (world blocks relative to cart entity Y)

- Vanilla 1.12.2 `Entity.updatePassenger`: cart Y + cart mounted offset +
  passenger `getYOffset`. `EntityMinecart.getMountedYOffset = 0`.
  Shimakaze is therefore **cart Y + 0.14**, both emotions.
- Forge 1.20.1: minecart mounted offset `0` + `getMyRidingOffset`.
  Before correction `0`; after correction **0.14**.
- Neo 1.21.1: `EntityType.MINECART` passenger attachment `0.1875`;
  `LegacyRiding.vehicleAttachment` returns `0.1875 - getMyRidingOffset`.
  `Entity.positionRider` subtracts this vehicle attachment from the cart's
  passenger attachment. Before: `0.1875 - 0.1875 = 0`. After:
  `0.1875 - 0.0475 =` **0.14**. Do not remove or reverse the existing cart
  attachment compensation.

Shimakaze's own `getMountedYOffset` is a different direction: it positions
someone riding *her*, not her position while riding a minecart. For height
1.6: standing `1.6 * 0.67 = 1.072`, sitting `1.6 * 0.16 = 0.256`, bored sitting
`1.6 * -0.04 = -0.064`. The catalog preserves these branches.

### Rendering chain, normal size (scale level 0), dry/no attack/no-fuel=false

There is no separate legacy Shimakaze renderer translation: `RenderShipEntity`
selects `ModelDestroyerShimakaze`; `RenderBasic.doRender` forwards the same
entity Y to `RenderLivingBase`. Vanilla living renderer flips Y and translates
model Y by `-1.501`. Both the outer living-renderer `setRotationAngles` and
the call inside `ModelDestroyerShimakaze.render` execute the riding branch.
Normal riding translates model Y by `+0.45` per call; bored by `+0.575`.
The inner call is inside the model's `scale(0.41)` / `translate(Y=2.17)`.
Neo's pose adapter preserves the two calls and these model constants/angles.

Model render-root Y relative to entity Y is:
`1.501 - outerRidingTranslation - 0.41 * (2.17 + innerRidingTranslation)`.

| Chain component | Legacy | Modern before | Modern after |
|---|---:|---:|---:|
| Entity passenger Y, either emotion | +0.140000 | 0.000000 | +0.140000 |
| Normal model-only root translation | -0.023200 | -0.023200 | -0.023200 |
| Normal combined render root / cart Y | **+0.116800** | -0.023200 | **+0.116800** |
| Bored model-only root translation | -0.199450 | -0.199450 | -0.199450 |
| Bored combined render root / cart Y | **-0.059450** | -0.199450 | **-0.059450** |

Numbers are rounded float transforms, not boot vertices: BodyMain/leg pivots,
angles and geometry still apply after this root. The normal/bored root
difference `-0.17625` is legacy model behavior, not something to normalize.
The correction raises the entire mesh by **0.14 world blocks in both poses**;
no pose, Z offset, geometry or state-bit changes are justified.

## Regression coverage and checks

`LegacyShipModelMappingGameTests.minecartPassengersRetainLegacyFeetAnchorInBothPoses`
now uses independent vanilla/legacy constants for every spec and both emotions,
not `getMyRidingOffset()` as its own expected oracle. It also covers null input.
Two existing passenger tests now include the inherited `0.14` in their exact
expected friendly/rider positions; tolerances and other assertions are unchanged.

Forge build: `/home/ubuntu/shincolle-cart-offset-final-forge-build.log`, successful
20s. Cart and updated passenger assertions pass, but full runs are not green:
one run failed the existing hostile Raiden pairing fixture; another failed
`hostileRangeTargetMixesPlayersAndShipsBeforeNearestThreeRoll` (empty candidate
list). The latter also fails in a fresh isolated game directory. A detached
unchanged HEAD `4d7e58b` comparison using the same isolated directory passes
362/362 (`/home/ubuntu/shincolle-cart-base-forge-gametest.log`, 1m22s).
Thus the failures are intermittent across runs; their cause is **not established**
and must not be described as a confirmed preexisting failure. No fixture weakened.

Vanilla 1.12.2 sources: KealJones/mc-1.12.2-source_files GitHub repository,
`Entity.java`, `passive/EntityAnimal.java`, `passive/EntityTameable.java`,
`item/EntityMinecart.java`, `client/renderer/entity/RenderLivingBase.java`.
Modern attachment source: cached NeoForm 1.21.1 Entity/EntityType/AbstractMinecart.

Factory visual evidence and final Neo gate are pending at this checkpoint.
