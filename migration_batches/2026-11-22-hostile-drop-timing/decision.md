# Decision

Confirmed timing regression, not scale/chance mismatch. Modern vanilla lethal
dispatch calls dropCustomDeathLoot during die/dropAllDeathLoot, while no ship
tickDeath override existed: egg appeared synchronously and corpse disappeared
at vanilla tick20. NeoForge21.1.267 mapped LivingEntity source confirms both
dispatch and default20. Historical0aad501 hostile onDeathUpdate increments
deathTime, drops at equality with deathMaxTick (ConfigHandler default400,
Death_Time range0..3600), and then removes the corpse. Probability and existing
modern egg ID/lifetime mapping are unchanged.

Move only hostile egg dispatch to a hostile tickDeath override; friendly
recovery loot and vanilla friendly death path remain unchanged. Restore the
missing Death_Time config (necessary minimal common-config addition); consult
CanDrop/doMobLoot at the deadline, as legacy does. Preserve equality: zero
removes on first death tick without an egg. Server-authoritative removal keeps
the current vanilla end particle event. XP dispatch is not changed in this
egg-timing fix; its timing is not covered by these tests.

Counter-checks: scale3 has guaranteed getDropEgg in both versions, so random
probability cannot explain early appearance. New deterministic GameTest covers
no tick advancement, each tick before400, deadline, duplicate prevention,
configured4/0 and CanDrop/doMobLoot denial. Existing hostile egg lifetime test
now asserts no immediate egg and advances the real death ticks before retaining
ALL lifetime/CanDrop assertions for12000/0/-1. No assertion weakened.
