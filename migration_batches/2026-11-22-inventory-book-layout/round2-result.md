# B2 kill/reload regression — reproduced and fixed

Real vanilla `/kill <uuid>` followed by vanilla EntityStorage entity-region disk
write/flush/close/reopen/load reproduced resurrection before death's first tick.
Baseline full Forge359 tests failed both new cases: friendly53 corpse at
DeathTime0 revived and a living damaged ship lost its saved HP. Log
/home/ubuntu/shincolle-kill-forge-gametest.log. This reproduces the symptom;
the user's particular B4 world/save was not supplied or examined.

Verified cause: LegacyShipEntity.readAdditionalSaveData first delegates vanilla
Health/DeathTime loading, then calls setVariantEggMeta. That setter calls
refreshFromVariant(false), explicitly filling HP. The final preserve-health
refresh preserves that newly full value when DeathTime0, and the existing
deathTime>0 tick guard cannot catch it. This also explains injured-ship healing.
Legacy BasicEntityShip.readFromNBT390 delegates vanilla then CapaShipSavedValues
loadNBTData; its calcShipAttributes(31,true) updates attributes and syncs, not
setHealth(max). Unified modern variant selection introduced the extra heal.

Minimal fix: retain numeric saved Health, finish all variant/level/equipment/
hostile-scale attribute reconstruction, then restore HP through vanilla setHealth
(normal max clamp). A pending DeathTime>0 still locks HP0; missing Health keeps
existing normal spawn defaults. No revival/reset mechanics or gameplay numbers.

New LegacyKilledShipPersistenceGameTests: actual command kill; friendly53 and
scale3 hostile2053; save at tick0/5 and hostile399; region files flushed,
closed/reopened, loaded through vanilla entity factory; exact UUID/HP/death timer;
corpse stays dead to removal, hostile removal400; empty region rewrite/reopen
has no phantom; living damaged level1/75 retains HP; missing Health defaults and
inconsistent positive Health with DeathTime5. Runtime region evidence directories
are beneath the QA world as shincolle-kill-* (not OS temp).

Disproof/other paths: RecoveredShipItemEntity only adds damage immunity to vanilla
ItemEntity, no auto-spawn tick. ShipChunkLoader only requests/releases chunk
tickets and clears restored tickets. TeitokuSavedData stores UID maps;
ShipCacheSavedData stores view records, not executable entity NBT. TeitokuHelper
refreshShipCache calls updateFromShip; remove marks dead/offline. No respawn
dispatch found in these paths; no changes to tickets, teams or recovery eggs.

Scope caveat: existing friendly death/removal timing differs from legacy's400
tick animation (modern vanilla20 and immediate recovery). This pre-existing
timing was not changed while fixing HP resurrection; hostile400 stays covered.
The new mortality tests deliberately do not canonize friendly20 as legacy.

Final Forge: build SUCCESSFUL15s (8 tasks6 executed2 up-to-date);
All360 required GT passed, SUCCESSFUL1m16s (11 tasks, see full log tail).
Logs /home/ubuntu/shincolle-kill-forge-final-{build,gametest}.log.
D3 enlarged self-contained fixture was also backported with unchanged geometry
and assertions; its Neo barrier explanation is in the Neo D3-resolution.md.
