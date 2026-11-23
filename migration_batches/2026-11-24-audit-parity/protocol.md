# Audit parity fix protocol

Authorized scope: A01–A09 from /home/ubuntu/audit-2026-11-24, plus S01 only after verifying vanilla1.12 despawn/persistence. Forge first per finding, then Neo. src/main/** remains read-only; use pinned0aad501 reference snapshot. No aircraft lifecycle/Morph/config backlog changes.

Legacy entries: BasicEntityShip.addShipExp/heal/setShipLevel/training interaction; BuffHelper.applyBuffOnHeal; EntityAIShipFlee and EntityHelper.applyTeleport/spawnMobShip/spawnBossShip; TileEntityVolCore; BasicEntityShipHostile.canDespawn/ShipSpawnEgg and vanilla EntityLiving/Forge patch.

Modern entries: entity/ship/LegacyShipEntity, LegacyShipBehaviorCatalog; entity/ship/goal/LegacyShipFleeGoal and LegacyShipTeleportHelper; world/HostileEncounterSpawner; blockentity/LegacyCoreBlockEntity; focused gametest/Audit*GameTests plus necessary package-local fixtures. New helpers only for directly shared legacy semantics.

Assertions: single-level XP carry; potion/cloud additive heal precedence; direct loaded-chunk flee teleport; encounter positions and no collision veto; book full heal and unmarried cap consumption; VolCore combat/feet-liquid/morale threshold, strict fuel fit, player damage and early return; mutually exclusive25/75 level sounds/volume. S01 must verify persistence gates, default egg, natural encounter and far/near/disabled controls before changing removal.

One implementation commit per finding on each version. Full build runGameTestServer after all source/version edits, JDK17 Forge/JDK21 Neo. Preserve existing assertions; record any genuine baseline conflict rather than weaken them. Bump only requested version0.2.3-alpha; update control-plane links, push feature branches, copy/hash main release jars to requested paths. No claim of runtime/client parity from source checks alone.
