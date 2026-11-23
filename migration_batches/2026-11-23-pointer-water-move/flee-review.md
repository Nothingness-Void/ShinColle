# PR15/16 review: flee mutex follow-up

Historical 0aad501 EntityAIShipFlee.java:35 sets mutex 7, like guarding;
modern LegacyShipFleeGoal.java:30 incorrectly had MOVE/LOOK/TARGET.
With floating restored to independent TARGET, that fourth flag blocked flee
while guard (MOVE/LOOK/JUMP) could start. Restore flee to MOVE/LOOK/JUMP only.

PointerWaterMoveGameTests.floatingLowHpShipFleesToOwnerInsteadOfPointerDestination
registers a real level-resolvable owner, a submerged fueled 25%-HP ship with
FleeHP=50, and an actual Pointer group command in the opposite direction. It
asserts floating/flee eligibility, registered flee running (never guard), and
physical movement toward owner over sixteen native entity ticks. Forge uses
the existing synchronous server-clock fixture adaptation and restores it.
The new connected-player fixture has its own pointer_water_flee batch.

Final checks: full build runGameTestServer, JDK17 Forge / JDK21 Neo:
- Forge /home/ubuntu/shincolle-pointer-flee-forge-final2.log:
  All 379 required tests passed :)
  BUILD SUCCESSFUL in 59s
- Neo /home/ubuntu/shincolle-pointer-flee-neo-final2.log:
  All 388 required tests passed :)
  BUILD SUCCESSFUL in 43s

First Neo compile needed the minimal 1.21 getAvailableGoals/isRunning API
instead of removed getRunningGoals. First Forge run failed existing
shipRangeTargetUsesLegacyCategoryPriority (AntiAir priority). No anti-air code
was changed. Final gate passed after giving the connected-player fixture its
own batch; the initial anti-air failure's causal relationship is unproven.
No smoke files, src/main, build configuration or other gameplay changes belong
to this review-fix commit. No visual verification requested/performed.
