# D round2 decisions

Aircraft regression: BasicEntityAirplane.collideWithEntity is empty, but its
moveEntityWithHeading invokes normal block-colliding move. Modern aircraft
constructor set noPhysics=true, so server tick's move ignored solids. Removed
that flag; client prediction now also uses move rather than raw setPos. Entity
collision remains disabled. Homing/return obstacle steering uses the already
ported ShipPathFinder(canFly=true), whose solid-block test derives from legacy
ShipPathFinder:484–491, with the legacy default follow-range32 and attack AI's
16-tick repath interval. Open-air target steering and attack numbers unchanged.
GameTest covers both airframes, swept glass collision, open-air movement and
solid-free aerial paths beyond a finite wall. It is not a full carrier combat
or continuous flight trajectory equivalence test.

Mount regression: LegacyShipMountEntity.interact already starts owner riding,
but isPickable returned false, excluding it from the client's entity hit-result
selection. Legacy BasicEntityMount inherits living entity pickability and its
applyPlayerInteraction starts riding. Restored live mount picking; existing
owner/control gates unchanged. Existing B2 test now checks pickability, rejects
intruder interaction and calls owner interact instead of forcing startRiding.

Line-abreast: no verified migration regression in the reviewed movement chain.
FormationHelper/LegacyFormationHelper give the same six command blocks. Both
EntityAIShipGuarding/LegacyShipGuardGoal use min/max squared4/7 for block formation,
5/9 for entity formation and max64 with pickup, refresh targets every8 ticks and
repath every32. ShipPathNavigate's stuck check32 and random jump signs are also
inherited, even the suspicious dx<0.2 comparisons; do not 'correct' them.

Disproof checks: the apparent extra travel in ShipNavigationHost.update is NOT
a new double-move: legacy BasicEntityShip.onLivingUpdate calls
EntityHelper.updateShipNavigator before super, and that helper explicitly calls
moveEntityWithHeading after updating path/move helpers. Another apparent miss,
ShipMoveHelper's formation-speed override, is already applied to modern
MOVEMENT_SPEED by LegacyShipEntity.resolveEffectiveMoveSpeed/rebuildLegacyStats
(min team speed plus formation MOV); both helper turn limits are60 degrees.
These do not establish why one live ship oscillated at4.6–5.9 blocks. Do not
claim the observed settling is proven legacy-identical: its cause remains OPEN;
an instrumented same-start six-ship trajectory or legacy runtime comparison
would discriminate inherited path/turn oscillation from another migration bug.
No threshold, teleport or formation layout changes made to mask that finding.

Shimakaze: both pose bodies match legacy (see shimakaze-cart-pose). 1.21 adds a
0.1875 minecart passenger anchor absent in1.12/Forge1.20; NeoForge LegacyRiding
compensates that attachment only. Forge offsets/model remain unchanged. Residual
boot/cart geometry clipping with the old exact pose is not grounds to move legs.
