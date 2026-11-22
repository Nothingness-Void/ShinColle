# Pointer client ownership review

Question: does the click-consumption predicate read unsynchronized numeric owner
UID on the real client path? Compare legacy PointerItem ownership/sync with modern
PointerClientEvents, PointerCommandEvents and LegacyShipEntity owner accessors and
synced data. Inspect TeitokuHelper player UID and existing MorphAndTeamParityGameTests
only as directly required. Fix the predicate using already-synced ownership data
if verified; preserve team/sneak/fall-through and repeated block-hold behavior.
Add client-shaped owner-data tests, retain foreign/missing/mount/team cases.
Forge first, local only; minimal Neo adaptation/ordinary merge; both full gates,
then Neo push. No shared entity sync schema change unless unavoidable. No other
gameplay, mounts, ring or audio work. Existing test matrix commands apply.
