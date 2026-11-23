# Final gate / delivery evidence

Version: **0.2.3-alpha** on both branches. All A01–A09 production fixes are separate commits, Forge first for each finding; S01 is a separate confirmed-deviation fix. [Legacy decisions](decision.md).

## Branch boundaries
- Forge: devin/1795453712-audit-parity-120, fresh base origin/codex/mc-1.20.1 at6d2c54d; /home/ubuntu/repos/ShinColle-audit-parity-120.
- Neo: devin/1795453712-audit-parity-121, fresh base origin/mc-1.21.1 atde91016; /home/ubuntu/repos/ShinColle-audit-parity-121.
- Read-only src/main/** unchanged, verified with git diff against each base. No shared build/CI changes or changes to other existing worktrees.
- Release code/evidence commits were ordinary-pushed and remote-verified: Forge8038a1725209995107ff042c55ccdce8a5725cc2; Neof9654a297db4d00730daf3a2af88effde50fe0ce. The subsequent delivery-state update is documentation only; its final published HEAD is reported in the handoff.

## Finding commits
| Finding | Forge | NeoForge |
|---|---|---|
| A01 | 61fad544a6bb23a04bd4409b34187613769e28df | 05f71513cc7c7d91a53540db134be79aa9c69b3b |
| A02 | 9fb7045325a95fb63981012b7870911116729701 | 8a24c081b1fa3ea3ad6a1f8afa36efafbfd7818a |
| A03 | 33e26cbb156d64813d776f377bb810ecedfbb268 | 27e82f695795a632a61754cd52d3e9e71f718aac |
| A04 | bc5cf0ace06aaf2199b256d15bcd45f5195b7e62 | f0be180eff1c9cd2c7e73852d615bb67405d6e3a |
| A05 | dbc50ae548f081eb0de20d49ffbdf20086047109 | 679b8c1ddfb1e72b13177ba8c368e2ac44c9d0b4 |
| A06 | e76904ef377c630abc5ab9bdffbed50e19495a3d | d7363fab13c2a8996918e66d2933900c32a6734d |
| A07 | 2522d48af4e1ecef852f3e666c11f2fc56a7b2a8 | a4ad80d69e577ed1c5751956a7f63e55d3eb5fa5 |
| A08 | 0b24825a7f01393b930296de086eed57897864a6 | 53fd4024ae404ea76a5372ea6a7c0aa2afd0631b |
| A09 | a9a81e3038521406f211cb4e9338ca565a464562 | 1feb95769682677b1637a35659d1ef1c3c928139 |
| S01 | 3dd25c424003523cb6606bfab5ef11d6e7a91fef | 458af33f6e29a62e7059a653feb654d8b10c3157 |

Fixture-only follow-up: Forge3888425 / Neo07a3cce correct XP/HPRES multiplier assumptions, put liquid feet exactly on integral negative-Y legacy sampling, and use a genuinely damageable survival Player instead of the invulnerable Forge/Neo FakePlayer. No production behavior or existing assertion was weakened. The release commit groups the ten audit tests into one batch and expands S01 save-format/non-roll coverage.

## Final full gates, after final source/test/version inputs
Forge JDK17: JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 bash ./gradlew build runGameTestServer
```text
========= 396 GAME TESTS COMPLETE ======================
All 396 required tests passed :)
BUILD SUCCESSFUL in 2m 23s
```
Log: /home/ubuntu/shincolle-audit-parity-forge-grouped-final.log

Neo JDK21: JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build runGameTestServer
```text
========= 405 GAME TESTS COMPLETE IN 30.62 s ======================
All 405 required tests passed :)
BUILD SUCCESSFUL in 1m 1s
```
Log: /home/ubuntu/shincolle-audit-parity-neo-grouped-final.log

Ten new focused tests cover all nine findings plus S01. They run in one audit_parity batch; old Pointer, flyer and other tests remain in the full gate without modified bodies. No source/resource/version inputs changed after these final successful builds.

### Intermediate failures / limits
Initial new fixtures failed on XP/HPRES multiplier assumptions, negative fractional feet Y, and FakePlayer invulnerability; corrected as above. Logs: /home/ubuntu/shincolle-audit-parity-{forge,neo}-gate1.log.

With one batch per new test, later Forge full runs hit:
```text
water ray must retain the surface fluid block, while land is offset above the floor
elevated route must retain its flyer: 32
```
No Pointer/mount production or existing test-body changes were made. Unchanged6d2c54d was checked in a fresh detached /home/ubuntu/repos/ShinColle-audit-parity-basecheck-120:
```text
All 386 required tests passed :)
BUILD SUCCESSFUL in 2m 38s
```
Base log: /home/ubuntu/shincolle-audit-parity-forge-base.log. That passing base is not proof of a preexisting failure. The exact scheduling/chunk/world-state cause remains unverified. The focused audit tests now share one batch to avoid gratuitous per-test chunk-force turnover, and the final Forge gate used a fresh generated world. Failed run worlds were preserved by renaming within this worktree's run directory, not destroyed; final gates above passed in full.

## Release jars
Copies are the main jars from the final passing builds, not sources/dev jars:
- /home/ubuntu/release/shincolle-0.2.3-alpha-mc1.20.1-forge.jar
  - SHA256 e1371564e35fcf7072d245fb5027f60a0bc677caa80d6eff55c3bc899fc1ca5a
- /home/ubuntu/release/shincolle-0.2.3-alpha-mc1.21.1-neoforge.jar
  - SHA256 ccc390ce1466651d2f56397d4a0d05c8c9d42b74223a59e3a194ac922b3b5c17

Both checks printed:
```text
PASS source/copy SHA256 identical; metadata version=0.2.3-alpha
```

No fresh visual/audio client replay, optional-mod despawn-event certification, or complete real-world save upgrade replay was performed. Aircraft death lifecycle / Morph / config backlog remain excluded.
