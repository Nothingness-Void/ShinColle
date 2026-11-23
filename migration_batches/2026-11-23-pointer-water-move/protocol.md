# Pointer water movement — frozen investigation

Question: why do some selected ships remain stationary after a Pointer move on
water, while land movement works? Strict legacy parity; no speculative tuning.

Read modern item/PointerClientEvents, PointerCommandEvents, PointerItem,
network/GameplayCommandHandler and command types, entity/ship/LegacyShipEntity,
LegacyShipObstaclePath and directly dispatched navigation/move/follow goals,
water motion and formation helpers. Compare read-only legacy item/PointerItem,
ShipPathNavigate, ShipPathFinder, ship move AI/water/formation logic and direct
dependencies, including historical compilable 0aad501. Inspect existing pointer,
navigation, formation and negative-Y water GameTests and their templates.

Trace dispatch reachability for single/group movement; ray fluid/block choice,
destination Y and formation offsets, water node eligibility and 3D searches,
range/repath/stuck handling, negative-Y floor rounding. First add and run a real
group water-movement GameTest and land control against unchanged production.
Record actual per-ship target/path and arrival; reject unreachable hypotheses.
Fix only a demonstrated modern divergence; retain legacy-identical limits.

Use fresh specified-base branches/worktrees only. Modern code/resources and docs
only; no src/main edits, version bump, unrelated movement redesign, LivingEntity
mount port, RingFly changes or build/CI changes. Full JDK17 Forge / JDK21 Neo
build runGameTestServer gates after final edits; commit and normal-push both new
feature branches if regression verified. No PR creation/client visual ask.

PR15/16 follow-up: audit historical EntityAIShipFlee mutex versus modern
LegacyShipFleeGoal and registered Float/Flee/Guard priority. Restore mutex 7
(MOVE/LOOK/JUMP), with a floating, fueled low-HP ship, real level-resolvable
owner in flee range and opposite Pointer destination. Assert actual registered
flee wins over guard and movement starts toward owner. Full gates and separate
review-fix commit/push both Pointer branches; do not include smoke work.
