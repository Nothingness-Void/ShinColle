# Crane empty-tank fluid wait regression

Scope: only legacy UntilFull/UntilEmpty bucket completion conditions when crane
tank is empty, both branches, Forge local only. Preserve other crane conditions.
Legacy: src/main/java/com/lulan/shincolle/tileentity/TileEntityCrane.java644–649,
713–718; src/main/java/com/lulan/shincolle/utility/InventoryHelper.java235–251.
Modern: src/modern/java/com/lulan/shincolle/blockentity/CraneBlockEntity.java603
and existing crane fluid GameTests. Exact paths may be located by filename first.
Input: /home/ubuntu/.devin-files/devin-remote-overflows-1000/97b1d141/content.txt.
Assertions: empty tank+empty ship bucket UntilFull must wait; empty tank+filled ship
bucket with no pump UntilEmpty must wait. Retain successful transfer/completion,
empty/no-bucket and incompatible-fluid cases from existing coverage.
Non-goals: pointer selection, pathing, fluid capacities/numbers, new mechanics.

Completed source commit9a34c55; Neo merge5444260/API837edb5. Historical0aad501
uses IInventory overload (only null inventory short-circuits), unlike the later
read-only src/main helper's null-fluid shortcut. Details and latest gate blocker:
[authoritative result](broad-wall-recovery-result.md). No client visual claim.
