# Dynamic trace completed; no verified movement regression

Initial fixture stayed formation0 after three setups. Further reading of native
ServerLevel.tickNonPassenger proved tickCount increments there, NOT Entity.tick.
The manual driver had kept tickCount41, never reaching ship's20tick formation
refresh. Corrected driver increments the clock exactly as ServerLevel does;
not an AI/formation fix. Registered moving ship and real fleet placeholders,
capability UID and explicit friendly53 are retained. Final Forge build19s,
All362 required GT passed, SUCCESSFUL1m33s. Logs
/home/ubuntu/shincolle-line-clock-forge-{build,gametest}.log.

Six slots x384 real goal/native ship ticks =2304 CSV rows. Every tick compares
target to independent formula, start/continue hysteresis, pickup-off4/7 ranges,
and records actual/cached distance plus path index/length/final point. All6
progress into squared7 arrival envelope, then stop clears navigation. CSV
shincolle-line-abreast-trace.csv under GameTest world. Legacy FormationHelper749
slot offsets0,+3,-3,+6,-6,+9 perpendicular to travel; follow EntityAIShipFollowOwner
shouldExecute/updateDistance uses4/7 squared,32tick refresh,10 then32 repath;
modern LegacyFormationHelper/LegacyShipFollowOwnerGoal reproduce these expressions.
Source agreement plus controlled counter-check, not proof of original stall's
cause. The reported4.6–5.9 gap was NOT reproduced; no movement changes justified.

LegacyLineAbreastTraceGameTests is now a registered regression test. Owner lookup
uses a fixture override rather than a network-connected player; other AI goals
disabled on this test subclass, real follow goal/navigator/travel driven manually.
Only one fleet member moves at a time; other fixtures remain at staging. Thus
crowded fleet, actual player movement/terrain and original world's state remain
unverified. To investigate original stall further, capture its actual live fleet
and follow goal target/paths rather than infer legacy-identical from source alone.
