# Batch C4: hostile egg drop timing

Repro: attachments/f8156fb0-c5f6-4774-9ec9-2b2fa6a8adad/ShinColle-Batch-C.md.
Freeze ticks, kill hostile Akatsuki egg2053 at scale3: friendly egg53 appears
immediately rather than at legacy death-animation tick400. Inspect historical
BasicEntityShipHostile.onDeathUpdate/dropEgg and ConfigHandler deathMaxTick;
modern LegacyShipEntity.dropCustomDeathLoot/tickDeath/dropHostileEgg and hostile
runtime state/catalog, plus relevant existing death/drop GameTests. Verify
reachable dispatch and counter-check non-hostile loot, empty/no-drop cases and
death horizon/config gates. Restore only verified timing regression, with tests
before death advancement, immediately before configured deadline, at deadline
and no duplication. Forge first then merge to NeoForge; no src/main changes,
new loot/probability/mechanics or weakened assertions. Final build and full
GameTests both branches after this queued change.
