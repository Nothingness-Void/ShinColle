# Frozen Protocol

## Legacy references

- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipCV.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostileCV.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerI.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerRo.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerHa.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerNi.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerShimakaze.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerAkatsuki.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerHibiki.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerIkazuchi.java`
- `src/main/java/com/lulan/shincolle/entity/destroyer/EntityDestroyerInazuma.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCARi.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCANe.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTenryuu.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCLTatsuta.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCAAtago.java`
- `src/main/java/com/lulan/shincolle/entity/cruiser/EntityCATakao.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipRu.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipTa.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipRe.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipNGT.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBattleshipYMT.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBKongou.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBHiei.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBHaruna.java`
- `src/main/java/com/lulan/shincolle/entity/battleship/EntityBBKirishima.java`
- `src/main/java/com/lulan/shincolle/entity/carrier/EntityCarrierWo.java`
- `src/main/java/com/lulan/shincolle/entity/carrier/EntityCarrierKaga.java`
- `src/main/java/com/lulan/shincolle/entity/carrier/EntityCarrierAkagi.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCarrierHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityAirfieldHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityBattleshipHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityDestroyerHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityHarbourHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityIsolatedHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityMidwayHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityNorthernHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCarrierWD.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntityCAHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySubmHime.java`
- `src/main/java/com/lulan/shincolle/entity/hime/EntitySSNH.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmKa.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmYo.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmSo.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmU511.java`
- `src/main/java/com/lulan/shincolle/entity/submarine/EntitySubmRo500.java`
- `src/main/java/com/lulan/shincolle/entity/transport/EntityTransportWa.java`

## Modern references

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAttackProfile.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [ ] Every registered base class has an explicit legacy attack-source conclusion: inherited default, class override, or hostile override.
- [ ] Modern light/heavy/air flags match the old constructor paths for friendly and hostile variants.
- [ ] Special hooks do not depend on an accidentally enabled generic attack channel.
- [ ] Any code change has a focused regression assertion and one batch-level build/GameTest run.
- [ ] A no-difference result is recorded explicitly rather than inferred from existing test count.

## Frozen non-goals

- Do not introduce new per-ship classes or a second dispatch layer.
- Do not infer behavior from ship names, textures, or modern archetypes when the old constructor is silent.
- Do not modify legacy files.
