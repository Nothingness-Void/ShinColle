# Batch: 2026-11-22-parity-fix-menu-network-command

## Question

这个批次只回答：审计报告分配给 `menu-network-command` 模块的 8 条 critical/major 偏离，是否确属偏离；确属偏离的按 1.12.2 还原。

## Scope

- 行为族/模块：舰船 GUI 按钮（`PID.ShipBtn` / `PacketHelper.setEntityByGUI`）、AI 标志、FollowMin/FollowMax、舰船背包 Shift 点击与翻页、`/ship*` 指令权限、`/shipcleardrop`、`/ship get`。
- 旧版入口：`utility/PacketHelper.java#setEntityByGUI`、`reference/ID.java`（`ShipInv_*`）、`network/C2SGUIPackets.java`、`utility/EntityHelper.java#checkOP/createShipEntity`、`command/ShipCmd*.java`、`client/gui/inventory/ContainerShipInventory.java`、`capability/CapaShipInventory.java`（以 `0aad501` 为准）。
- modern 入口：`network/ShipCommandService.java`、`network/ShipGuiButton.java`、`menu/ShipInventoryMenu.java`、`menu/ShipPagedCargoSlot.java`、`client/screen/ShipInventoryScreen.java`、`command/ShinColleCommands.java`、`entity/ship/LegacyShipEntity.java`。

## Out of scope

- 其他模块（Morph GUI 的 `MorphProfile` AI 字段属于 morph 模块，未改动）。
- 旧版 GUI 的完整像素级按钮布局（现代界面仍使用快捷键承载这些按钮）。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
