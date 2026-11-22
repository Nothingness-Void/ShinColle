# Batch F: ring flight and recipes

Investigate historical 0aad501/src/main MarriageRing, Teitoku capability and
player event/tick handlers, compared with modern ring item, Ring/Teitoku events,
profile/capability and ability synchronization. Establish water-flight grant and
revocation gates, preserve creative/spectator abilities and legacy tick timing.
Add regression coverage for normal, empty/disabled, water-exit conditions if fixed.

Compare ALL src/main/resources/assets/shincolle/recipes JSON inputs, counts and
outputs against src/modern/resources/data/shincolle/recipes (Forge) and recipe
(NeoForge), explicitly mapping legacy IDs/tag aliases to registered modern IDs.
Use a scripted semantic audit, preserve recipes whose differences are only API
format/name aliases. Fix verified ingredient/output/quantity regressions, including
the target wrench only after verifying item mapping. Record every mismatch and
unmapped/modern-only entry. No src/main edits or invented recipes.

Changes on Forge first; logical commits then merge NeoForge, no force-push.
Final build/full GameTests on both branches after final source edits. Waypoint
VM screenshots remain separately required; do not broaden visual exploration.
