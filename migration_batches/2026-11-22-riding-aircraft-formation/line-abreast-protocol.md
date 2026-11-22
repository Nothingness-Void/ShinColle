# Dynamic line-abreast trace protocol

After B2 gates: test only formation5 (单横), pickup off. Read legacy
utility/FormationHelper.getFormationGuardingPos/nextLineAbreastPos and
ai/EntityAIShipFollowOwner, plus ShipPathNavigate.pathFollow as necessary;
modern formation/LegacyFormationHelper, entity/ship/goal/LegacyShipFollowOwnerGoal,
entity/ship/path/ShipPathNavigate and LegacyShipEntity's tick/goal wiring.

Drive real Goal/legacy navigator and movement on a flat bounded fixture. Record
each tick's target, actual distance, cached follow distance, min/max hysteresis,
path node/index/final, goal continue condition. Compare target to independent
legacy line-abreast formula; compare stop/arrival with legacy4/7 squared ranges,
32tick distance refresh, 10 then32tick path refresh and navigator width threshold.
Use fixture owner only to avoid dependence on network-connected players; state
that limitation. Assert pickup off, formation5 and actual movement reached.
Do not treat formula agreement as proof of original user's stall cause; if no
reproduction, retain open/legacy-identical code distinction. No speculative fixes.
Test boundary/empty path and retain error diagnostics. Non-goals: other formation
redesign, follow range/radius tuning, world terrain or legacy engine behavior.

Fixture stayed formation0 after three setups: verify native tick-count ownership
in ServerLevel.tickNonPassenger versus Entity.tick before any further attempts.
Manual tick driver must reproduce native clock increments, not tune assertions.
