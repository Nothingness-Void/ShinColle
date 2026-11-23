# PR19/20 review — routine HP updates are not explicit persistence

## Scope / starting evidence
Local and remote heads matched before editing:
- Forge devin/1795453712-audit-parity-120:1e42146635a810f5bf6b317a2d9fefc634677d0c.
- Neo devin/1795453712-audit-parity-121:443f40fdaff2c18127cdb73789fc7862cffdf746.

Verdict: **real S01 regression on both branches**. No vanilla indirection is necessary for the reported HP path.

## Verified reachable chain and attempted disproof
At the reviewed heads, src/modern/java/com/lulan/shincolle/entity/ship/:
1. LegacyShipEntity.tick:1170–1173 dispatches tickLegacyEmotionState every64 ticks on the server, before tickHostileRuntime. No spawn-origin gate excludes hostile eggs or natural hostiles.
2. LegacyShipBehaviorCatalog.tickLegacyEmotionState:3587–3600 accepts live server ships and unconditionally calls ship.setHpState after calculating the HP ratio. This happens even if HP state remains unchanged.
3. LegacyShipEntity.setHpState:2172–2175 directly calls this.setPersistenceRequired. Emotion, inventory and other normal state setters also used the same public call.
4. The S01 override at:4512–4518 captures that call into hostileRuntimeState.persistenceRequired when hostile, not loading, with an initialized state — exactly the ordinary live server tick.
5. LegacyShipHostileDespawn.tick returns early when that field is true. HostileRuntimeState.saveToTag/loadFromTag serializes/restores it, so reload does not repair the mistaken request.
6. Disproof checks: spawning/reset does clear the field, but does not run after every HP update; loadingSaveData does not suppress normal server ticks. Merely removing capture altogether would break genuine vanilla/name-tag persistence, so that is not the fix.

Legacy0aad501 at src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java:918–924 invokes updateEmotionState on its64-tick cadence; :975–1003 calculates the same HP-state tiers. Its setStateEmotion at:418–426 only writes the state array and optionally synchronizes; it never enables persistence. Friendly BasicEntityShip.java:1411+ follows the same separation. Original S01 default despawn evidence remains in decision.md.

## Minimal root fix
LegacyShipEntity now has a private requireNativePersistence wrapper that calls super.setPersistenceRequired without changing explicit hostile intent. Its38 internal native-retention calls (constructor, inventory callback and state setters) use that wrapper. The public setPersistenceRequired override is still used by genuine caller requests, including vanilla NameTagItem, and continues capturing explicit intent.

This preserves the unified Mob's native flag exactly as before and changes neither the legacy despawn algorithm nor age/distance limits, save schema, naming behavior or friendly retention. No catalog special case, stack inspection, HP-only exception, or clearing genuine persistence on later routine updates.

## Focused regression coverage
AuditHostileDespawnGameTests.routineTicksDoNotPersistUnnamedHostiles executes actual LegacyShipEntity.tick, not just the private despawn helper:
- Natural and egg origins, minion and boss scales.
- Tick64 at half health must actually produce legacy MODERATE HP-state2, without setting HostileRuntime.PersistenceRequired.
- Native unified-Mob retention remains true.
- A player exists but is more than128 blocks away; this is “no nearby player”, not “no player anywhere” (vanilla does not despawn with no closest player).
- Original and save/reloaded unnamed ships survive exact age128 and despawn at129.
- An actual named Items.NAME_TAG interaction sets the explicit flag, and both original/reloaded tagged ships survive age192 including another periodic HP update.
- Existing direct explicit-request, disabled/exact-age, old-save format and idle-distance controls are unchanged.

Initial compile exposed the catalog HP constant's private visibility. The test now independently asserts the serialized legacy value2, without widening production API visibility.

## Compatibility boundary
Already-saved true HostileRuntime.PersistenceRequired values cannot be reliably distinguished between genuine requests and this regression. Existing true values are deliberately not bulk-cleared; that could destroy real persistence intent. This fix prevents routine updates from creating the erroneous value going forward and validates correct reload behavior for newly saved state. No speculative save-format migration was added.

## Verification / artifacts
Final Forge JDK17: JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 bash ./gradlew build runGameTestServer
```text
========= 397 GAME TESTS COMPLETE ======================
All 397 required tests passed :)
BUILD SUCCESSFUL in 1m 17s
```
Log: /home/ubuntu/shincolle-audit-review-persistence-forge-final.log

Final Neo JDK21: JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build runGameTestServer
```text
========= 406 GAME TESTS COMPLETE IN 24.08 s ======================
All 406 required tests passed :)
BUILD SUCCESSFUL in 43s
```
Log: /home/ubuntu/shincolle-audit-review-persistence-neo-clean.log

The first completed Neo gate passed the new tick64/name-tag regression but failed the previously recorded assertion:
```text
Desk radar must discover the loaded ForgeData drop and skip ordinary items
```
Log: /home/ubuntu/shincolle-audit-review-persistence-neo-final.log. No Desk/item-marker production or test-body edits were made. A single retry with a fresh generated test world passed all406 tests; the old world was renamed within run/ and retained. This does not establish the exact cause of the intermittent failure. The already-passing Forge gate was not rerun.

Both main release jars were refreshed from these passing build outputs:
- /home/ubuntu/release/shincolle-0.2.3-alpha-mc1.20.1-forge.jar — SHA256 bfcc65641262ad760e244983fef8457b3c35aa4b5d11e37d16541211df9d4684
- /home/ubuntu/release/shincolle-0.2.3-alpha-mc1.21.1-neoforge.jar — SHA256 dcbab0bc787208c3acffdefc529b08f2df424cd9ced4a90fd992ada78b37c70e
Each check printed:
```text
PASS source/copy SHA256 identical; metadata version=0.2.3-alpha
```

No source/test/resource/version inputs changed after the successful gates. src/main/**, version, build/CI and unrelated existing test bodies remain unchanged. One separate review-fix commit per branch includes this evidence; the handoff records its exact remote-verified SHA after one consolidated ordinary push per branch.

No client/visual replay was performed for this server persistence fix.
