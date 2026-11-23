# Ship smoke — approved parity scope

## PR17/18 follow-up review

Verify local/remote e34f5de (Forge) / ae383c4 (Neo) before editing.
Audit historical 0aad501 friendly/hostile Akatsuki/Hibiki/Ikazuchi/Inazuma,
Nagato/Yamato/Kongou/Hiei/Haruna/Kirishima chimney callbacks and
ParticleHelper type20 versus43; compare catalog engine points and dispatch.
Fix only verified per-ship particle type/limiter/scale differences on both
existing smoke branches, with separate commits per finding and focused tests
in LegacyShipSmokeGameTests; correct the disproven smoke expectations in
GameplayParityGameTests against the historical callbacks. Full build/GameTests once after all code edits,
JDK17/21 respectively; push once per branch. No unrelated lifecycle, gameplay,
build/CI, version or read-only legacy edits. No new visual check requested.

Fresh origin-based branches devin/1795444967-ship-smoke-121/-120, bases
4380e97 / 1b82b2c. Do not merge Pointer commits or change existing worktrees.
Read-only legacy/historical 0aad501 ParticleHelper types 4/5/7/43,
ParticleSmoke, BasicEntityShip and BasicEntityShipHostile HP/death callbacks,
and all mapped engine callers. Modern scope: LegacyShipBehaviorCatalog,
LegacyShipEntity client/death callbacks, GameplayClientEffects, registered
particle options/provider/type and vanilla smoke resources, focused tests.

Restore type-43 scaled vanilla smoke, three y+0.1 segments and input velocity
(0, lookY, 0), not scale as X speed. Keep vanilla random jitter, legitimate
6/19/35/42 directional smoke and friendly Yamato speed blur unchanged. Client
HP effects every 16 ticks use types 4/5/7, y+0.7, width (hostile width*1.5),
legacy random offsets/counts and paired flame offsets/velocities. Existing
ship-death callbacks receive the historical smoke without timer/gameplay edits.
Aircraft death-lifecycle gap is explicitly approved as documented-only:
modern aircraft immediately discard; do not add corpse lifecycle/emitter.

Test pure generation/mapping/codecs, normal/empty/error cases; full JDK17 Forge
and JDK21 Neo build+GameTests after edits. Separate smoke commits and normal
push both new branches. Client runClient on DISPLAY=:0, isolated run directory,
minor/moderate/heavy and Kongou chimney screenshots smoke-*.png under
/home/ubuntu/screenshots, including visible rise. No src/main edits, build/CI
edits, version bump, Pointer fix, new mechanics or unrelated work.
