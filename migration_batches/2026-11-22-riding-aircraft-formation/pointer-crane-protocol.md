# E round 3 Pointer selection to crane dispatch

Report: `/home/ubuntu/attachments/202f5d69-b0ba-4736-809b-e573d79237ab/批次-E-r3-实机测试报告.md`.
Follow actual left-click selection through fleet/focus state, then server right-click
dispatch to crane/route node. A fleet list entry is not by itself evidence of active
command focus. Compare legacy PointerItem.java (including historical 0aad501),
Pointer helpers and teitoku selection state with modern PointerCommandEvents,
Pointer item, fleet service/state/menu and ShipCommandService. Read their direct
event/packet/capability dependencies and existing focused GameTests as needed.
Check event reachability on both Forge and NeoForge, UI vs server state, owner and
dimension gates. Verify failure with a regression test before naming a root cause;
perform a counter-check for empty/foreign selection. Restore only proven parity
regressions; preserve commands and crane logistics rules. Forge-first local commit
(push held), merge to Neo, build/full GameTests, normal Neo push. No src/main edits,
no test weakening or speculative fleet-selection redesign.
