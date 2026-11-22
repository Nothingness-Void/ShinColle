# Review follow-ups — final verification

## Worktree and commits
- Branch: devin/1795364114-review-followups-121
- Worktree: /home/ubuntu/repos/ShinColle-review-followups-121
- Base: origin/mc-1.21.1, 6ccd51ac04f9fa1b6601421371432b67d81bafe2
- A: 3dedae0c49fc55d4f38ad19aac49f2f81a261a7d
- B fix: 33afed0cc79ac9cad8fe27c41d01dc21ba1693da
- Separate final release/docs commit sets mod_version=0.2.1-alpha (already
  included in the verified source snapshot).
- Existing worktrees were not edited. The Forge base is the current remote head,
  including merged 0.1.2 version PR, not the earlier 80365a2 release snapshot.

## Final full gate
JDK 21.0.13.1; command:
```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build runGameTestServer
```

```text
========= 383 GAME TESTS COMPLETE IN 26.25 s ======================
All 383 required tests passed :)
BUILD SUCCESSFUL in 50s
```

Log: /home/ubuntu/shincolle-review-followups-121-green.log

The default fresh-worktree run directories were used; no shared run directories,
init-script overrides or build/CI changes. Optional Curios runtime was not
injected for these requested default gates. Neo's tracked structure-sync task
also prepares its templates. Subsequent commits only separate the already tested
changes and add documentation; no source/resource/version inputs changed after
the passing gate.

Main artifact:
`/home/ubuntu/repos/ShinColle-review-followups-121/build/libs/shincolle-0.2.1-alpha.jar`

## Problems found during verification
- Initial Neo compile found package-private Holder.Reference.bindTags. The
  temporary tag fixture uses reflection only on Neo; production API unchanged.
- The new ingot negative test initially exposed additional shared vanilla beacon
  and trim classifications. Inspected resource-tree resolution confirmed the
  exact common-tag set, excluded those non-ore classifications, and reran both
  complete gates after the final code edit. No assertions were weakened.
- Final full gates above passed. Earlier failed attempts are retained in
  /home/ubuntu/shincolle-review-followups-121-{gate,final}.log.

## Limits
A uses real matching/crane filter predicates with scoped, restored tag fixtures;
B uses real Entity.load/save, radar payload and command dispatch, not merely a
helper call. This is not an actual client visual test or a complete real-world
Forge-to-Neo world-upgrade replay. No client UI/rendering code changed. No PR
created; normal pushes are authorized only for the two new feature branches.
