# Pointer-base merge and 0.2.2-alpha release

Fetch updated bases: Forge0f6d350b6cdf8b0157c0f5ee92077c243ed97aeb,
Neo563779b9edf265b16e133b55a02cda4f86956808. Starting smoke HEADs:
Forgef49c14acea7010f60d078d62d01813cc296ad1a5,
Neo4a1892f58fbf279b0bf1fe60f9c759631d8879cf.

Both are normal merges, no rebase or force push. Identical five conflict
paths on each version:
- CURRENT_STATE.md
- ROADMAP.md
- MIGRATION-CHANGELOG.md
- MIGRATION-SHIP-MATRIX.md
- MIGRATION-TEST-MATRIX.md

For CHANGELOG and both matrices, retained smoke/review and incoming Pointer
entries together, dropping only conflict markers/common-ancestor alternatives.
Rebuilt CURRENT_STATE/ROADMAP as combined integration/release control planes,
linking both authoritative batch histories and retaining lifecycle gaps.
No Java/resource conflict or source replacement. Incoming floating TARGET,
flee MOVE|LOOK|JUMP and registered-goal Pointer tests are preserved exactly.
Smoke custom/ordinary particle distinction, HP/death emission and fixed Hiei
chimney parameters remain unchanged.

User authorizes mod_version=0.2.2-alpha in gradle.properties on both branches.
Full JDK17/21 build+GameTests after final version inputs. Results, pushed
SHAs (handoff), artifact metadata and copy hashes follow after gates.
No src/main, unrelated worktree, build/CI or new visual changes.

## Merge commits and gates

- Forge merge: d03c2130d343b4bad1568d7183ccbeb18bf17093.
- Neo merge: 50e775294c0e05ef1c6ed6a79c6f449182e369f5.
- Both have their original smoke HEAD and fetched origin base as two parents.
- Diff checks: incoming Pointer goal/test files equal origin base; retained
  smoke catalog/helper/tests equal pre-merge smoke HEAD. src/main unchanged.
- Both properties and packaged mod metadata report 0.2.2-alpha.

Forge JDK17, bash ./gradlew build runGameTestServer:
```text
[15:28:38] [Server thread/INFO] [minecraft/GameTestServer]: ========= 386 GAME TESTS COMPLETE ======================
[15:28:38] [Server thread/INFO] [minecraft/GameTestServer]: All 386 required tests passed :)
BUILD SUCCESSFUL in 1m 25s
```
Log: /home/ubuntu/shincolle-022-merged-forge-gate.log.

Neo JDK21, ./gradlew build runGameTestServer:
```text
[15:29:54] [Server thread/INFO] [minecraft/GameTestServer]: ========= 395 GAME TESTS COMPLETE IN 25.13 s ======================
[15:29:54] [Server thread/INFO] [minecraft/GameTestServer]: All 395 required tests passed :)
BUILD SUCCESSFUL in 45s
```
Log: /home/ubuntu/shincolle-022-merged-neo-gate2.log.

First Neo gate failed only:
```text
[15:28:09] [Server thread/ERROR] [minecraft/LogTestReporter]: forgedatadropmarkersurvivesloadradarandclearcommand failed at 11243571, -60, 7631818! Desk radar must discover the loaded ForgeData drop and skip ordinary items
BUILD FAILED in 55s
```
Log: /home/ubuntu/shincolle-022-merged-neo-gate.log.
This same assertion is recorded in the earlier unchanged-base
/home/ubuntu/shincolle-pointer-water-baseline-neo.log. No radar/drop fixture or
implementation was changed. One full rerun passed all395; no claim to
diagnose/fix that intermittent cause. Explicit JDK17/21 runtimes verified.

## Main release artifacts

The requested full build already produced the main release jars. No code,
resource or version input changed after the gates; only evidence docs.
Reuse these gated artifacts after push rather than a redundant identical build.

Forge source:
 /home/ubuntu/repos/ShinColle-ship-smoke-120/build/libs/shincolle-0.2.2-alpha.jar
Target:
 /home/ubuntu/release/shincolle-0.2.2-alpha-mc1.20.1-forge.jar
SHA256: 7464c744f988ba6c24500456f92150696748c5e5b9570f5bd2338742f468820e
Metadata META-INF/mods.toml: modId=shincolle, version=0.2.2-alpha, forge.

Neo source:
 /home/ubuntu/repos/ShinColle-ship-smoke-121/build/libs/shincolle-0.2.2-alpha.jar
Target:
 /home/ubuntu/release/shincolle-0.2.2-alpha-mc1.21.1-neoforge.jar
SHA256: 44143423bac2bf3c561908687a1d7d7284ad005d131e3a9a8b7c2964a7b9e19f
Metadata META-INF/neoforge.mods.toml: modId=shincolle, version=0.2.2-alpha, neoforge.

Final feature-branch push SHAs and post-push target-copy equality are recorded
in the handoff. No new client/visual replay.
