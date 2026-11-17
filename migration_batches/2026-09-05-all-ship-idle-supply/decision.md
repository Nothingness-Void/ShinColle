# Decision Record

# Decision Record

## Legacy source

- `BasicEntityShip.updateConsumeItem()` (`src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java:1562-1604`) first refills empty light/heavy ammo through the legacy item path, computes integer movement distance from `shipPrevX/Y/Z`, clamps morale loss to `5..50`, multiplies movement grudge by `ConfigHandler.consumeGrudgeAction[Move] = 3`, grants Transport Wa movement experience after tick 200, adds the class idle cost, consumes grudge, and updates the previous position.
- The method is called from the common `BasicEntityShip.onLivingUpdate()` path while the entity is not morphing. `BasicEntityShipSmall` and `BasicEntityShipCV` inherit that path, so the old Hime classes based on them retain the common resource behavior even when their modern spec is marked hostile for combat/loot identity.
- `BasicEntityShipHostile` extends `EntityMob` and has its own `onLivingUpdate()` implementation; the registered hostile Mob variants are therefore excluded from this common resource path.

## Cost mapping

The catalog now keeps the old `ID.ShipClass` mapping instead of using modern archetype or hostile flags:

- DD: class `0,1,2,3,27,36,51,52,53,54` -> `5`
- CA: class `9,10,49,58,59` -> `8`
- CV: class `12,20,33,47,48` -> `12`
- BB: class `13,14,26,37,46,60,61,62,63` -> `15`
- BBV: class `15,21,28,29,30,31` -> `14`
- AP: class `16` -> `3`
- SS: class `17,18,19,38,39,44,72` -> `4`
- CL: class `56,57` -> `7`

`LegacyShipBehaviorCatalog` returns `-1` for null/unknown IDs and for the modern `eggMeta >= 2000` hostile Mob registrations. Hime entries below that Mob range use the old base-class behavior.

## Modern changes

- `LegacyShipEntity.tickLegacyMovementAndIdleConsumption()` is now the single shared 128 tick implementation for all catalog-mapped `BasicEntityShip` classes; it no longer gates on the modern `hostile` flag.
- `LegacyShipBehaviorCatalog.legacyMovementExperienceFor()` mirrors the old Transport Wa `ticksExisted > 200` branch using the already multiplied movement grudge.
- The GameTest enumerates all 45 active base class IDs and validates class mapping, costs, Hime execution, hostile Mob exclusion, and the Transport tick boundary.

## Verification

- `git diff --check`: passed; Git emitted only existing LF/CRLF normalization warnings.
- `compileJava`: passed on Java 17.
- `processResources`: passed/up-to-date.
- `runGameTestServer`: 269 tests ran; the new idle-supply test passed. Four unrelated existing combat tests still fail and are tracked by the next batch; see [results/all-ship-idle-supply.json](results/all-ship-idle-supply.json).
