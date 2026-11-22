# D3 route failure resolved07:09

The failure was a migrated GameTest fixture, not a demonstrated gameplay or
far-coordinate pathfinder defect. No gameplay/pathfinder change was needed.

Temporary instrumentation ran the same Airfield (egg23,1.9x1.3) at near256 and
the original randomized far position. Near route19 nodes passed below glass;
far route11 nodes ended at relative(-4,0,3), shrank to3 nodes then null while
the body stopped at(-2.95,0,4.05). Block-volume logging revealed minecraft:barrier
at relativeZ5 (including X5 andX-5 sides), not more glass or a precision error.
Evidence logs: /home/ubuntu/shincolle-d3-{trace,block-trace}.log.

Verified chain in MC1.21.1 sources: GameTestInfo.prepareTestStructure at269
calls StructureUtils.encaseStructure(getStructureBounds(),level,!skyAccess).
StructureUtils131-146 places BARRIER on those bounds. The shared empty template
is only9x9x9. Our unchanged origin(4,5,4), goalZ=originZ+9.5 and open-space
probeX=originX+15 were outside that room. The glass-to-barrier gap atZ4..5 could
not fit the full1.9-wide mount. This explains the partial path and null repaths.
The near control was outside that generated enclosure, disproving the leading
float-coordinate hypothesis. No claim of precision-related gameplay fix.

Dedicated flight_space template26x16x24 contains the complete fixture, goal
box, detours and open-space probe. Original randomized far placement, local
origin, all8 styles+fort, glass dimensions, speed,160-step limit and all prior
collision/route assertions are unchanged. Added an explicit unobstructed-goal
box assertion. Removed temporary near control/reflection/block logging.

Final Neo JDK21 build: SUCCESSFUL10s,5 tasks4 executed1 up-to-date.
Full GT: All365 required passed,22.47s test runtime; BUILD SUCCESSFUL42s.
Logs /home/ubuntu/shincolle-d3-fixed-{build,gametest}.log. Runtime used the private
QA gameDir and copied the new SNBT there, real Curios9.6 configured ring slot.
