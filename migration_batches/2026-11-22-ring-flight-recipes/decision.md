# Batch F decisions and complete recipe diff

## Ring flight

Historical 0aad501 MarriageRing.onUpdate (lines122-147) DOES grant temporary
water flight: it sets isFlying, not allowFlying. It runs on both sides, only
claims flight if not already flying and hasRing/isRingActive, and clears owned
flight on liquid exit for noncreative players. onItemRightClick (74-77) and
CapaTeitoku.setCapaDataMisc (1566-1571) clear flying and RingFly on disable.
EventHandler's 16-tick inventory scan (696-700) clears flying on losing the ring,
without touching allowFlying. None of these paths grants creative-style permission.

Reachable regression: modern inventoryTick calls tickActiveRingEffects for active
rings (the optional Curios tick calls it too); it unconditionally wrote mayfly=true
in liquid. Right-click disabled flying and RingFlying but not mayfly; inactive
rings never reached dry-exit cleanup, leaving survival double-jump permission.
The leading explanation was counter-checked against legacy capability/tick code:
water flight exists, but the permission grant does not. Restore the old distinction:
never write mayfly in ring effects, restore the not-already-flying/has-ring gates,
and execute the inventory effect on both sides, with server ability sync on changes.
Do not seize independently granted flying (including creative/spectator); all their
permission bits remain unchanged. Existing ring-loss parity assertions remain intact.

New test enters real WATER, asserts the wet fixture, grants temporary flying but
not mayfly, toggles off, re-enables and exits water, asserts the dry fixture, and
checks empty/inactive rings and preexisting independent flight. This prevents new
leaks. It deliberately does not guess the owner of an already-saved mayfly=true
bit left by older buggy builds; existing affected players can refresh their game
mode to reset vanilla permissions. Automatically stripping unrelated flight would
not be legacy parity.

## Recipes (all 84 reference JSONs versus all modern crafting JSONs)

`check_recipe_parity.py` compares canonical full shaped grids (including horizontal
mirror equivalence), shapeless multisets, alternatives, result IDs and counts.
Aliases: registry case normalization; metadata-split abyssmetal/nugget, ammo,
grudge, combat ration and ship tank; ShipSpawnEgg0/1 -> smallegg/largeegg;
clownfish -> tropical_fish; 22 OreDictionary names -> corresponding common tags.
It expands the shipped cooked_meat tag before comparing alternatives. Group,
category and ore recipe serializer names are API/UI metadata, not ingredients.
NeoForge's singular recipe/item directory and c: namespace are handled separately.

Complete semantic difference list before fixes (79 matches):

| Reference | Modern before | Action |
| --- | --- | --- |
| itemWrench -> TargetWrench, 4 AbyssMetal0 (`a a /  a  /  a `) | targetwrench, 6 abyssmetal | Restore 4 and exact grid. Same registered TargetWrenchItem, not a different item. |
| blockDesk requires black_wool | blockdesk accepts any wool | Restore black wool. |
| ingotPolymetal5: ToyAirplane -> 5 AbyssMetal1 | missing | Add abyssmetal1_from_toyairplane. |
| itemGrudgeXP: eight experience bottles around Grudge0 -> Grudge1 | missing | Add grudge1_from_experience_bottles. |
| no inverse BlockGrudgeXP crafting recipe | grudge1_from_blockgrudgexp -> 9 grudge1 | Remove invented recipe. Legacy ModRecipes.register registers only RecipeEnchantShell; commented old recipes do not add an inverse. |
| blockLargeShipyard -> blocklargeshipyard | missing | Invalid reference-only JSON, NOT a registered item in legacy ModBlocks or modern ModBlocks. Existing strict GameTest explicitly forbids this standalone recipe. Leave absent; do not invent a block/item. |

After corrections: 83/84 semantic matches, the sole unmatched legacy JSON is the
invalid standalone large-shipyard recipe; zero modern-only recipes. This is an
explicit unmapped report, not a claim that all 84 are runnable legacy recipes.
Earlier positive fixtures had white wool and six ingots; corrected these fixtures
to the reference inputs and ADDED rejection assertions for both previous inputs,
empty grids and invented XP unpacking. No assertion was removed or weakened.
Actual RecipeManager/assembly checks verify both restored recipes and quantities.

Forge final code gates: BUILD SUCCESSFUL in16s; 349 required tests passed.
Logs: /home/ubuntu/shincolle-120-batch-def-{build,gametest}.log.
NeoForge adaptation, final gates and waypoint visual evidence remain pending.

NeoForge-specific follow-up: its universal JAR's c:obsidians contains BOTH
obsidians/normal (minecraft:obsidian) and obsidians/crying (crying_obsidian), while
Forge47's forge:obsidian contains only minecraft:obsidian. Eight recipes had been
broadened during the initial API port: blockdesk, blockframe, blocksmallshipyard,
shiptank1, blockvolblock/_alt and blockvolcore/_alt. Narrow to c:obsidians/normal
on NeoForge only; Forge is already correct. Add a crying-obsidian desk rejection.
API-only common-tag aliases are gunpowder/gunpowders, flour/flours and
cooked_rice/foods/cooked_rice; the audit normalizes them, but deliberately does
NOT equate the broad obsidians tag with legacy obsidian.
