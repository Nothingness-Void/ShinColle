# Merged review follow-ups: frozen scope

## A — legacy ore equivalence
Question: can modern shared bucket/container tags match lava to water where
legacy crane ore dictionary matching would not? Read legacy TileEntityCrane,
InventoryHelper and directly called ore-match helpers/registrations; modern
crafting/LegacyItemMatchHelper, block/entity/CraneBlockEntity (locate exact path),
and existing item-match/crane GameTests. Inspect relevant platform bucket/ingot
tags only as required. Preserve legitimate material equivalence and wildcard,
damage, empty-input, blacklist semantics. Add water/lava rejection with ore on
and legitimate ingot-equivalence coverage; change only the overly broad tag rule.

## B — legacy persistent drop markers
Question: does the actual entity load mixin recover ShinColle drop markers from
ForgeData when upgrading Forge saves to NeoForge? Read modern
mixin/LegacyEntityDataMixin, TeitokuHelper drop radar helpers, shipcleardrop command
and existing marker/persistence GameTests; follow direct persistent-data API and
legacy drop-marker writer/reader dependencies. Copy only relevant legacy keys,
respect modern values on conflict, preserve unrelated mod data and save/load
idempotence. Test real entity load from ForgeData, marker discovery and cleanup;
check Forge's native load semantics and avoid unnecessary Forge production edits.

## Execution boundaries
Fresh worktrees only; original worktrees untouched. No src/main writes. Separate
fix commits, then explicit gradle.properties 0.2.1-alpha bump (user-authorized
exception to modern-only edits). Both full build/GameTest gates, correct JDK17/21,
then normal pushes of the two newly named feature branches. No PR creation,
unrelated gameplay, client UI work, shared build/CI edits or mainline pushes.
