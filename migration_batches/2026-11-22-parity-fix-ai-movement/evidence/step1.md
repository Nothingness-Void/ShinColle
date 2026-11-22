> 历史步骤证据：保留当时的指令、失败和待办语境；最终结论与后续交付以[统一批次决定](../decision.md)为准。

# AI movement parity — step 1

Scope: independent FollowMin/FollowMax state, GUI drag bars, persistence and sync; server-effective teleport configuration. No commits. `src/main/**` remains read-only.

## Source evidence

- Current legacy BasicEntityShip / ID.M are mid-refactor (AttrNum / ShipStateHandler). Historical source consulted with `git show 0aad501:<path>`: BasicEntityShip.java:189-199 initializes FollowMin=3, FollowMax=12, FleeHP=35; reference/ID.java:494-496 maps them to minor state indices 10/11/12. Current handler/ShipStateHandler.java:62-64 agrees.
- capability/CapaShipSavedValues.java:38-49, 147-149: `ShipExtProps.Minor.FMin`, `FMax`, `FHP`. Modern follow state uses FMin/FMax and the existing legacy Minor fallback reader, plus separate entity-data accessors for client synchronization.
- client/gui/GuiShipInventory.java:418-419, 1583-1590: two independent 42-pixel drag bars; integer conversion `position / 42F * 30F + 1F` (min) / `+ 2F` (max), giving 1–31 and 2–32. No cross-clamping between min and max. Modern old J/K single-range shortcut removed.
- ai/EntityAIShipFollowOwner.java:271-277 and EntityAIShipGuarding.java:516-522: min/max plus width*0.75, with +5 to max when picking items. Modern owner and guard distance reads now use these values; this is not a complete movement-state-machine port.
- ai/EntityAIShipFloating.java:143,162: guard uses min+width*0.5, owner uses max+width*0.5. EntityAIShipRangeTarget.java:203 uses max+2 for low attack range; EntityAIShipPickItem.java:216 adds max to pickup base.
- handler/ConfigHandler.java:152,174,373,412,464: ship_teleport={200,256}, Can_Teleport=true. Both are now COMMON values; client config no longer defines canTeleport. Owner goal reads distance squared and cooldown from COMMON. Flee's existing boolean reference also resolves COMMON.

## Next-step map (not a parity claim)

- LegacyShipFollowOwnerGoal: priority 4, owner-only Player resolution; follow/sit/passenger/leash/command/fuel gates; navigation toward owner, near-owner collision-tested teleport. Independent start(max)/stop(min) thresholds restored. Formation is not integrated into this goal. Navigation recalculation still modern 10 ticks, and teleport safety/placement are not a full old EntityHelper.applyTeleport port.
- LegacyShipGuardGoal handles guard attacks only while navigation is moving: independent light/heavy/air cooldowns, sight and warmup gates. Actual guard movement lives in LegacyShipEntity.tickCommandState: guard entity, route waypoint/crane, guard block, commanded position branches. Entity/block guard now read min/max; route arrival logic still has its own fixed distances. No guard teleport implementation has been added in this step.
- formation/LegacyFormationHelper resolves team/slot through Teitoku data; formation disabled for default ID or teams <=4; five formation layouts, 3-block spacing, flagship/previous-position-based direction. Its move target is a BlockPos, not a live owner-follow destination.
- LegacyShipEntity extends PathfinderMob and does not override createNavigation; it uses inherited navigation. Water/pathfinding parity is not addressed here.

## Verification

`bash ./gradlew compileJava --no-daemon`: BUILD SUCCESSFUL in 1m 33s; 1 actionable task: 1 executed. Existing deprecation/unchecked notes remain. Two prior edit-stage compiles exposed and corrected a mouse-coordinate int cast and a legacy fallback reader argument count.

`bash ./gradlew runGameTestServer --no-daemon`: All 302 required tests passed; BUILD SUCCESSFUL in 3m 45s. GUI drag bars and runtime multiplayer sync are visually unverified; no client was launched. Existing persistence GameTest now saves distinct min/max values (7,32); old single-state API references were updated to FollowMax. Full movement/teleport parity remains for subsequent steps.

Final review also restored the two-element teleport-array fallback (`ConfigHandler.java:547-560`) so malformed/empty lists cannot cause an indexing error. Final post-review verification is reported in the handoff.
