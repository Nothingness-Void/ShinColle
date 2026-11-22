# Review follow-ups — final verification

## Worktree and commits
- Branch: devin/1795364114-review-followups-120
- Worktree: /home/ubuntu/repos/ShinColle-review-followups-120
- Base: origin/codex/mc-1.20.1, 353c67ce3e268afff318f59e833ab1613e7e6b4e
- A: 9d63d7f82de3fce6e3962f5d34151b14ef8f22bc
- B native-load verification (no production migration required): db94c74eaf49de0556bb74fdc813821f8e559941
- Separate final release/docs commit sets mod_version=0.2.1-alpha (already
  included in the verified source snapshot).
- Existing worktrees were not edited. The Forge base is the current remote head,
  including merged 0.1.2 version PR, not the earlier 80365a2 release snapshot.

## Final full gate
JDK 17.0.20.1; command:
```sh
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 bash ./gradlew build runGameTestServer
```

```text
========= 374 GAME TESTS COMPLETE ======================
All 374 required tests passed :)
BUILD SUCCESSFUL in 1m 11s
```

Log: /home/ubuntu/shincolle-review-followups-120-green.log

The default fresh-worktree run directories were used; no shared run directories,
init-script overrides or build/CI changes. Optional Curios runtime was not
injected for these requested default gates. Neo's tracked structure-sync task
also prepares its templates. Subsequent commits only separate the already tested
changes and add documentation; no source/resource/version inputs changed after
the passing gate.

Main artifact:
`/home/ubuntu/repos/ShinColle-review-followups-120/build/libs/shincolle-0.2.1-alpha.jar`

## Problems found during verification
- Initial Neo compile found package-private Holder.Reference.bindTags. The
  temporary tag fixture uses reflection only on Neo; production API unchanged.
- The new ingot negative test initially exposed additional shared vanilla beacon
  and trim classifications. Inspected resource-tree resolution confirmed the
  exact common-tag set, excluded those non-ore classifications, and reran both
  complete gates after the final code edit. No assertions were weakened.
- Final full gates above passed. Earlier failed attempts are retained in
  /home/ubuntu/shincolle-review-followups-120-{gate,final}.log.

## Limits
A uses real matching/crane filter predicates with scoped, restored tag fixtures;
B uses real Entity.load/save, radar payload and command dispatch, not merely a
helper call. This is not an actual client visual test or a complete real-world
Forge-to-Neo world-upgrade replay. No client UI/rendering code changed. No PR
created; normal pushes are authorized only for the two new feature branches.
