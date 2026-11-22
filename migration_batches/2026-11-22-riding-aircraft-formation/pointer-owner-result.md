# PR11 Pointer client ownership review result

## Scope and status

Question: did 6c5214c gate the actual client click on an unsynchronized numeric
owner UID? [Protocol](pointer-owner-protocol.md). Source-verified regression,
fixed with already-synced UUID ownership; no new sync field or packet/schema.
Forge source commit 96336ca (local only); Neo API adaptation cd06bdc, followed
by ordinary merge. Final documentation/push SHAs are reported in the handoff.
Previous [section/profile result](section-readiness-profile-result.md) remains
historical and its unrelated movement work is unchanged.

## Evidence chain and attempt to disprove

- PointerClientEvents.handlePointerLeftClick calls pickCommandTarget, then
  consumesTargetClick before sending POINTER_LEFT_CLICK. False means no target
  packet; a sneaking player instead reaches mode-switch/clear-team logic.
  The short-range client attack handler also uses this predicate.
- The 6c5214c predicate calls PlayerSkillHostResolver.sameOwner, comparing the
  capability's numeric player UID to ship.getOwnerUid().
- Modern getOwnerUid returns the ordinary private ownerUid field, initially 0.
  Checked every assignment in LegacyShipEntity: save-NBT loading, setOwner and
  clearOwner. It is not a SynchedEntityData accessor. Network/client code has
  no corresponding ship ownerUid setter or ship-NBT hydration route.
- To disprove the missing-data explanation, checked whether the getter resolves
  from synchronized UUID and whether custom client networking assigns it: neither
  occurs. In contrast DATA_OWNER_UUID/NAME are defined synchronized entity data,
  setOwner publishes them, and isOwnedBy compares the synchronized UUID to the
  actual player's UUID. getShipUid already has an explicit synchronized client
  accessor, so current-team membership does not need another sync adaptation.
- Legacy PointerItem.onEntitySwing runs this ownership branch client-side.
  TeamHelper.checkSameOwner compares numeric UID; legacy S2CEntitySync PID51
  SyncEntity_PlayerUID explicitly serializes the owner's UID and the client
  handler calls IShipOwner.setPlayerUID. That is a legacy packet path, not
  evidence of a modern equivalent.

Thus an owned hit with client ownerUid=0 is rejected before packet dispatch,
including teammate sneak clicks. This matches the reported no-selection and
mode-switch symptoms. This is source-path verification plus a client-data-shaped
GameTest, not a captured LocalPlayer packet trace or new visual client session.

## Minimal fix and regression coverage

Only production change: consumesTargetClick uses ship.isOwnedBy(player), including
the resolved mount host. It reads existing synchronized ownership data. No changes
to server handleTarget authorization (still sameOwner), team/focus behavior,
sneak/sprint fall-through, repeated LeftClickBlock hold, network version or saves.

MorphAndTeamParityGameTests.pointerClientOwnershipUsesSyncedUuidWithoutNumericOwnerCache
creates a real ship/mount/current team, then zeros only the server numeric owner
cache to reproduce client data availability. Synced owner UUID and ship UID
remain valid. It asserts normal ship/mount consumption, teammate sneak
consumption, and non-team sneak fall-through. Original foreign/null/non-ship,
mount, sneak+sprint and long-ray/crane-dispatch cases remain unchanged. The numeric
field is restored in finally before normal server fixture teardown, so the
synthetic snapshot is never used for persistent cache cleanup.

## Final gates

Coverage/commands: [test matrix](../../MIGRATION-TEST-MATRIX.md).
Forge JDK17, combined build/full GameTest:
```
========= 370 GAME TESTS COMPLETE ======================
All 370 required tests passed :)
BUILD SUCCESSFUL in 1m 37s
```
Neo JDK21, combined build/full GameTest:
```
========= 378 GAME TESTS COMPLETE IN 42.60 s ======================
All 378 required tests passed :)
BUILD SUCCESSFUL in 1m 21s
```
Persistent logs:
- /home/ubuntu/shincolle-pointer-owner-forge-restored-cleanup.log
- /home/ubuntu/shincolle-pointer-owner-neo-restored-cleanup.log

An intermediate Forge run failed the existing crane transfer case:
```
crane load row should move only matching chest items into ship cargo; expected=4068/d6fc4082-b2e1-4af1-b0bf-39d741fbaa13 actual=null
```
The identical symptom predates this patch: archived
/home/ubuntu/shincolle-queue-final-forge-gametest.log at 07:40:02, 361-test suite:
```
crane load row should move only matching chest items into ship cargo; expected=3677/f8714ad2-2375-4238-b6d8-3f241fea2638 actual=null
```
No crane source/fixture change was made and its intermittent cause is not
established. The new test's temporary numeric field restoration was added before
the final gates; both gates were rerun because that fixture input changed.
Do not treat the final pass as a fix for crane intermittence.

Neo merge resolution preserves exactly the tested cd06bdc src/modern snapshot;
later documentation does not change those inputs. Forge hold remains active,
src/main unchanged, no force push. No fresh client screenshots or visual claim.
