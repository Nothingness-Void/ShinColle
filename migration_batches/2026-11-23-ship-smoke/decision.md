# Decision — restore legacy smoke, retain explicit lifecycle boundary

## Scope and source audit

Fresh branches/worktrees:
- Forge: devin/1795444967-ship-smoke-120, /home/ubuntu/repos/ShinColle-ship-smoke-120, base 1b82b2c.
- Neo: devin/1795444967-ship-smoke-121, /home/ubuntu/repos/ShinColle-ship-smoke-121, base 4380e97.

No Pointer commits were merged into these branches. The separately requested Pointer flee review was committed/pushed first: Forge 5f3663193733c5e2f650201d2664c8ad65c02282, Neo edfa1db1662fc8372629468825099fdde2d5ad8e.

Historical 0aad501 ParticleHelper.java:483–490 type 43 directly creates three ParticleSmoke objects at y+i*0.1, input velocity (0, lookY, 0), scale lookX. ParticleSmoke merely inherits the scaled vanilla ParticleSmokeNormal constructor. Modern pre-fix LegacyShipBehaviorCatalog.tickLegacyClientBehavior's SMOKE_CHIMNEY_L branch instead used LARGE_SMOKE, spacing 0.3, and particleScale as X speed. Modern vanilla SmokeParticle/BaseAshSmokeParticle adds supplied X speed to random motion; X is not a scale argument.

Reachability: LegacyShipEntity.tick calls tickLegacyClientBehavior on clients. Its client/alive/every-four-tick guard reaches the engine-point table when configured model bits/rider/active-state conditions hold. Audited chimney entries map to type-43 friendly/hostile Nagato, hostile Yamato, Akatsuki/Hibiki/Ikazuchi/Inazuma and Kongou/Hiei/Haruna/Kirishima callers. The VM exercised the model-bit-enabled, fueled Kongou path and observed actual LegacySmokeParticle instances. Thus the parameter error is a verified reachable migration regression; the original reporter's precise ship/model state remains unrecorded. Legitimate directional types 6/19/35/42 and friendly Yamato speed blur were not changed.

HP: historical BasicEntityShip.java:1946–1966 and BasicEntityShipHostile.java:952–969 emit every sixteen client ticks. MINOR/MODERATE/HEAVY map to types 4/5/7 at y+0.7, width (hostile width*1.5), vertical input 0.05. ParticleHelper.java:186–224 specifies three normal-smoke groups for 4/5, four large-smoke groups for 7, paired flame with swapped X/Z random offsets, and zero input Y for heavy smoke. Modern synchronized HP state previously had no particle consumer.

Death: historical BasicEntityShip.onDeathUpdate:5425 onward and BasicEntityShipHostile.onDeathUpdate use every-four-client-tick type-43 smoke, quality-dependent group count, width*1.2 spread, friendly y+0.1/scale1.5 versus hostile y+0.3/scale1+scaleLevel. Existing modern tickDeath dispatch is retained unchanged except this client emission.

## Implementation

- LegacySmokeOptions + ModParticleTypes: registered scale-carrying type; overrideLimiter=true matches legacy direct renderer insertion. Neo MapCodec/StreamCodec versus Forge Codec/Deserializer/FriendlyByteBuf only.
- LegacySmokeParticle: scaled vanilla SmokeParticle and vanilla generic smoke sprites, supplied vertical motion kept separate from scale. Retains unbiased vanilla jitter, not artificially zeroed lateral random motion.
- LegacyShipSmoke: pure type-43/HP/death emission geometry and type/count helpers.
- LegacyShipBehaviorCatalog: HP callback before model/fuel/chimney alive gating; type-43 chimney replacement only.
- LegacyShipEntity.tickDeath + GameplayClientEffects: existing client death callback emission; no death timer/gameplay edit.
- Five LegacyShipSmokeGameTests: triplets/scale/spacing/velocity; HP types/counts/tick gate/empty states; hostile spread and paired flame; death quality/offset/scale; registry codec/network roundtrip and malformed/missing-scale rejection. Forge's new tests have a separate batch to avoid shifting existing oversized default fixtures; existing tests/assertions were not modified. Neo retains its original default-batch form.

## Verification and earlier gate limitation

Final commands, after the last production/test edit:
- Forge JDK17: bash ./gradlew build runGameTestServer
- Neo JDK21: ./gradlew build runGameTestServer

Exact output is in results/gates.txt. Forge final full gate: /home/ubuntu/shincolle-smoke-forge-corpse-final.log, all379 passed, BUILD SUCCESSFUL in1m6s. Neo final full gate: /home/ubuntu/shincolle-smoke-neo-corpse-final.log, all388 passed, BUILD SUCCESSFUL in50s. Subsequent changes are docs only.

Final source review found legacy HP emission has no alive-state guard: BasicEntityShip.onUpdate calls super then its client HP block even during death; BasicEntityShipHostile's client onLivingUpdate does likewise. The initial implementation had inherited the chimney alive guard. This was corrected on both versions: HP emission runs before the unchanged chimney alive guard, and damageTick depends only on client/tick. Updated the new cadence test; then executed each full gate once on the changed inputs, with both passing.

Before that independent smoke-owned correction, three Neo repeats failed only the preexisting forgedatadropmarkersurvivesloadradarandclearcommand assertion: Desk radar must discover the loaded ForgeData drop and skip ordinary items. Logs: /home/ubuntu/shincolle-smoke-neo-isolated-final2.log, /home/ubuntu/shincolle-smoke-neo-green.log, /home/ubuntu/shincolle-smoke-neo-final-restored.log. All five new smoke tests passed in those completed runs. No further attempt was made to fix that radar failure; the final run was required revalidation of the newly corrected smoke inputs, not an unchanged-input retry.

Independent unchanged4380e97 baseline: /home/ubuntu/shincolle-pointer-water-baseline-neo.log, same assertion at14:32:35. Radar/marker production and test files are unchanged by smoke. This establishes a preexisting failing assertion, not its exact timing/chunk-visibility cause. No radar fix is claimed; investigate separately if it recurs.

Earlier Forge default-layout run hit the existing elevated-wall assertion; unchanged Forge baseline hit a different Pointer long-ray assertion, which does not independently establish that exact wall failure as preexisting. Isolating only the newly added pure smoke tests yielded a full green Forge gate; no mount/Pointer code or old assertions were changed.

## Client evidence

Neo JDK21 runClient, DISPLAY=:0, isolated game directory /home/ubuntu/shincolle-smoke-visual, init /home/ubuntu/shincolle-smoke-visual.init.gradle, client log /home/ubuntu/shincolle-smoke-client.log.

Fixture-only attach agent creates three stationary friendly ships with minor/moderate/heavy HP states, plus class62 Kongou with the chimney model bit, fueled. Health is refreshed per view to avoid ordinary automatic repair changing the state before capture. First-person camera/FOV50, normal vanilla assets. Screenshots are unedited full VM captures:
- /home/ubuntu/screenshots/smoke-minor.png — small normal smoke above the ship.
- /home/ubuntu/screenshots/smoke-moderate.png — normal smoke plus paired flames.
- /home/ubuntu/screenshots/smoke-heavy.png — larger smoke plus flames, plume visibly above the head.
- /home/ubuntu/screenshots/smoke-kongou-chimney.png
- /home/ubuntu/screenshots/smoke-kongou-chimney-rise.png — subsequent live view with smoke above the chimney.

Alive-ship screenshots precede the final corpse-HP gating correction; that correction does not alter any alive emission or chimney input. Client particle inspection independently sees LegacySmokeParticle from the Kongou emitters, starting at y101.17/101.32 (plus0.1 segments), with aged particles reaching y103.22 and positive vertical speed about0.074. Horizontal speeds have both signs at ordinary jitter magnitude, not scale-sized constant positive X motion. Raw inspection log: /home/ubuntu/shincolle-smoke-visual-evidence.log. This supports actual provider/texture/render-path execution, not merely successful client startup.

Existing ship-death smoke was source/pure-emission tested, not separately killed-and-screenshotted in the client. Hostile width/death differences are GameTest-covered, not separately visually verified.

## Approved gap / non-goals

Historical BasicEntityAirplane.onDeathUpdate:281+ retains a falling corpse for about90 ticks and emits recurring smoke. Modern LegacyShipAircraftEntity.applyAircraftDamage:721–730 immediately discards on lethal damage and has no client death state. User explicitly approved documenting that lifecycle gap instead of adding it or approximating it with a one-shot effect. No aircraft production edit.

No navigation, AI mutex, ring, mounts/LivingEntity, LEVITATION, stats, attack, version, build or CI change; src/main/** remains read-only and unchanged. Existing old VM world/worktrees untouched.
