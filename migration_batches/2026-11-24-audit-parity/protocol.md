# Audit parity fix protocol

## PR19/20 persistence review follow-up
Authorized scope: trace tick64 tickLegacyEmotionState -> setHpState -> setPersistenceRequired and other internal state setters in LegacyShipEntity, comparing BasicEntityShip/BasicEntityShipHostile display setters and vanilla NameTag persistence semantics. Distinguish native unified-Mob retention from genuine explicit hostile persistence, without weakening named/explicit persistence. Add integrated tick64/age/reload and genuine-request controls to AuditHostileDespawnGameTests. Forge first then Neo; full build runGameTestServer once after final edits, JDK17/21. One review-fix commit per branch; push once each, refresh existing0.2.3-alpha release jars. No config/version bump, src/main edit, new despawn mechanism, client/visual check, or unrelated fixture-body changes.

Authorized scope: A01–A09 from /home/ubuntu/audit-2026-11-24, plus S01 only after verifying vanilla1.12 despawn/persistence. Forge first per finding, then Neo. src/main/** remains read-only; use pinned0aad501 reference snapshot. No aircraft lifecycle/Morph/config backlog changes.

Legacy entries: BasicEntityShip.addShipExp/heal/setShipLevel/training interaction; BuffHelper.applyBuffOnHeal; EntityAIShipFlee and EntityHelper.applyTeleport/spawnMobShip/spawnBossShip; TileEntityVolCore; BasicEntityShipHostile.canDespawn/ShipSpawnEgg and vanilla EntityLiving/Forge patch.

Modern entries: entity/ship/LegacyShipEntity, LegacyShipBehaviorCatalog; entity/ship/goal/LegacyShipFleeGoal and LegacyShipTeleportHelper; world/HostileEncounterSpawner; blockentity/LegacyCoreBlockEntity; focused gametest/Audit*GameTests plus necessary package-local fixtures. New helpers only for directly shared legacy semantics.

Assertions: single-level XP carry; potion/cloud additive heal precedence; direct loaded-chunk flee teleport; encounter positions and no collision veto; book full heal and unmarried cap consumption; VolCore combat/feet-liquid/morale threshold, strict fuel fit, player damage and early return; mutually exclusive25/75 level sounds/volume. S01 must verify persistence gates, default egg, natural encounter and far/near/disabled controls before changing removal.

One implementation commit per finding on each version. Full build runGameTestServer after all source/version edits, JDK17 Forge/JDK21 Neo. Preserve existing assertions; record any genuine baseline conflict rather than weaken them. Bump only requested version0.2.3-alpha; update control-plane links, push feature branches, copy/hash main release jars to requested paths. No claim of runtime/client parity from source checks alone.
