# B round2: killed ships reappear after reload

Report: ships removed by /kill reappeared after save/reload in B4 world.
Compare legacy BasicEntityShip/BasicEntityShipHostile death+NBT lifecycle and
vanilla kill semantics with LegacyShipEntity.tickDeath/readAdditionalSaveData,
stat recalculation and Health/DeathTime restoration. Trace recovery-drop entity,
TeitokuSavedData/team registry and chunk-ticket paths only far enough to check
whether they spawn or merely index/load surviving entities. Sources: src/main
read-only; modern entity/ship/LegacyShipEntity, LegacyShipStats, recovery entity,
teitoku/TeitokuSavedData, ticket helper and their existing GameTests.

Reproduce /kill then save at death-animation tick0/mid-animation and load through
the entity factory; assert still dead, correct deadline/drop, and no revival.
Also assert living injured ships retain health; do not change legacy animation
delays, inventories/recovery, team semantics or ticket behavior without a proven
causal regression. Forge first, then Neo merge, full build/GT at final edits;
record missing real-world data rather than claim the user's exact cause.

Disk reproduction uses vanilla EntityStorage/ChunkEntities in a fresh persistent
directory beneath the QA world: actual /kill command, entity-region write/flush,
close/reopen, region load with vanilla entity factory, death continuation and
empty chunk rewrite. Covers friendly/scale3 hostile, tick0/mid-death, injured
living control. Read direct cache calls and TeitokuData team serialization as
needed; indexes/tickets must not be called respawn causes without a spawn site.

Legacy read entry is BasicEntityShip.readFromNBT (not readEntityFromNBT): it
delegates vanilla first then CapaShipSavedValues.loadNBTData. Include that
capability reader and its attribute refresh only as needed to compare health.

Neo initial actual-kill precondition failed, then passed after only diagnostics.
Investigate deterministic random defense: LegacyShipCombatHelper.applyDefenseReduction
and native/NeoForge LivingEntity.hurt/actuallyHurt plus DamageContainer. Test
seed0 before real command; inspect patched bytecode if bundled sources are vanilla.
Do not mask an incomplete kill by directly setting HP0 or retrying the command.
