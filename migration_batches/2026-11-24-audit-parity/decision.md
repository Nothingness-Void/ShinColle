# A01–A09 and S01 — legacy parity decisions

Scope: approved 2026-11-24 audit; Forge first for every finding, then NeoForge. Legacy src/main/** and all other worktrees remain untouched. Both branches use mod_version=0.2.3-alpha. No mechanics are added.

| ID | Legacy evidence at historical0aad501 under src/main/java/com/lulan/shincolle/ (audit snapshot) | Decision / focused GameTest |
|---|---|---|
| A01 | entity/BasicEntityShip.java:1162–1193; one if, not a loop | One level per positive XP award; keep carry and full heal. AuditExperienceGameTests |
| A02 | entity/BasicEntityShip.java:5407–5422; entity/BasicEntityShipHostile.java:1796–1810; utility/BuffHelper.java:599–670 | Server cloud scan before thrown potion; first heal/regen effect amplifier+1; amount+(max(1,maxHP*.01)+3 or6)*(1+level*.5), then HPRES. Preserve directional AABB.expand via expandTowards, not inflate. AuditPotionHealingGameTests |
| A03 | ai/EntityAIShipFlee.java:113–137; utility/EntityHelper.java:2079–2136 | Failed navigation directly uses escort XYZ with Y+.5 and loaded chunk/config checks; no perimeter/support/collision veto. AuditFleeTeleportGameTests |
| A04 | utility/EntityHelper.java:2234–2357,2358–2478 | Remove collision rejection. Common groups use fractional offsets within the anchor block; boss/escort use integer0..2 without modern extra+.5; Y+.5. Keep water-anchor validation/group rolls. AuditEncounterGameTests |
| A05 | entity/BasicEntityShip.java:1765–1787,1203–1215 | Training allowed below150, unmarried level clamps100; consume and fully heal even at unmarried100; level150 rejects. General setter health semantics unchanged. AuditTrainingGameTests |
| A06 | tileentity/TileEntityVolCore.java:317–347; utility/EntityHelper.java:97–100,192–200; reference/ID.java morale L_Excited=5100 | Require out-of-combat >128 ticks, actual feet-block liquid, morale <5100*1.8F; heal remains HPRES-adjusted. Preserve legacy feet Y truncation and centered radius6. AuditVolCoreAuraGameTests |
| A07 | tileentity/TileEntityVolCore.java:145–187 | Consume only if fuel+stored is strictly less than capacity; do not clamp/drop overflow. AuditVolCoreFuelGameTests |
| A08 | tileentity/TileEntityVolCore.java:350–402 | Include survival players; return whole loop at ship/hostile/mount/airplane. Modern non-Living mount/aircraft/FloatingFort adapters must participate in early-return query. BasicEntitySummon/Rensouhou is not an exemption. AuditVolCoreDamageGameTests |
| A09 | entity/BasicEntityShip.java:1180–1188 | Exactly one cue: roll0 vanilla (25%), rolls1..3 mod (75%), both volume.75/pitch1. AuditLevelSoundGameTests |

## S01 — confirmed default egg-hostile migration deviation

Verified chain, rather than inferring from canDespawn alone:
1. Legacy item/ShipSpawnEgg.java:299–334 getSpawnEntity calls EntityHelper.createShipEntity(...,false); :556–563 initializes hostile scale and spawns. utility/EntityHelper.java:1933–1987 constructs/adds the entity and does not enable persistence for hostiles. The hostile constructor and initialization also do not enable it.
2. Vanilla 1.12.2 EntityLiving constructor leaves private persistenceRequired=false. EntityLivingBase.onLivingUpdate dispatches updateEntityActionState on server unless movement blocked; EntityLiving.updateEntityActionState increments idleTime and invokes despawnEntity. NoAI suppresses this dispatch.
3. Vanilla EntityLiving.despawnEntity resets idle for explicit persistence. Otherwise it requires a closest player, uses canDespawn and distance>128^2 for immediate removal, or idle>600 / 1-in-800 / distance>32^2; within32 resets idle. BasicEntityShipHostile.canDespawn (:156–176) supplies the configured minion/boss age gate, with negative settings disabling removal, regardless of spawn origin.
4. Attempted disproof: checked getSpawnEntity, EntityHelper.createShipEntity, hostile constructor/init, and Forge's EntityLiving patch. No implicit spawn-egg enablePersistence or ShinColle despawn-event veto was found. Forge adds canEntityDespawn event handling, DEFAULT falls through to this vanilla branch.
5. Modern constructor forces native persistence for unified friendly/hostile Mob; custom tick previously additionally required NaturalSpawn=true, whereas LegacyShipSpawnEggItem sets false. Thus default egg hostiles never reached removal. This is a real deviation.

Fix: apply the vanilla default despawn algorithm to both origins. Remove the invented96/900 distance/timer branch, retain negative config/strict age and NoAI handling, and do not remove when there is no closest player. NaturalSpawn remains provenance, not gameplay eligibility. Idle restarts on load as vanilla (not serialized idleTime).

The native Mob flag is forced for unified navigation/lifecycle, so explicit persistence must be tracked independently in HostileRuntime.PersistenceRequired. Genuine public setPersistenceRequired calls (including name tags) are captured; internal native-retention marks are separated by requireNativePersistence ([PR19/20 review correction](review-persistence.md)); spawning/reset establishes the default false, loading preserves explicit new-format values. Legacy saves lacking HostileRuntime honor their native PersistenceRequired; pre-fix modern saves with HostileRuntime but no explicit key cannot distinguish default forced true, so default to false except named entities. This preserves preexisting named-hostile protection without preserving the erroneous egg-wide default.

AuditHostileDespawnGameTests exercises the real tickHostileRuntime for natural/egg, minion/boss, negative/exact/over-limit ages, explicit persistence and reload; helper coverage checks nearby idle reset. Optional third-party despawn-event integration is not runtime-certified by this default-mod gate.

Vanilla source consulted:
- https://raw.githubusercontent.com/KealJones/mc-1.12.2-source_files/master/src/minecraft/net/minecraft/entity/EntityLiving.java (constructor; PersistenceRequired read/write; despawnEntity/updateEntityActionState; enablePersistence)
- https://raw.githubusercontent.com/KealJones/mc-1.12.2-source_files/master/src/minecraft/net/minecraft/entity/EntityLivingBase.java (onLivingUpdate server dispatch)
- https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/1.12.x/patches/minecraft/net/minecraft/entity/EntityLiving.java.patch (DEFAULT event fallthrough)

## Limits / decisions not taken
The ten focused tests share one audit_parity GameTest batch rather than forcing/unforcing the same world chunks once per individual finding. Assertions remain independent and unchanged; this avoids gratuitous per-finding chunk-load turnover. Intermediate Forge runs exposed existing Pointer ray / elevated flyer fixture failures; base6d2c54d passed its full386-test gate. Their exact cause is not established by that base pass. Final status records the actual outcomes rather than calling them proven preexisting failures.

No aircraft death-lifecycle port, Morph bridge, config-backlog changes, visual/audio replay, optional-mod runtime certification, or full real-save upgrade replay. This batch restores the nine audited behaviors and the verified S01 default dispatch; it is not an exhaustive equivalence proof.
