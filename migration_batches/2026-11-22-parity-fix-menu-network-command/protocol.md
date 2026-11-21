# Frozen Protocol

## Legacy references

- `src/main/java/com/lulan/shincolle/utility/PacketHelper.java` `setEntityByGUI`（L1494-1640，43 个 `ShipInv_*` 按钮）。
- `src/main/java/com/lulan/shincolle/reference/ID.java` `ID.B.ShipInv_*`（0..42）。
- `src/main/java/com/lulan/shincolle/network/C2SGUIPackets.java` `PID.ShipBtn`（L314-322）。
- `src/main/java/com/lulan/shincolle/handler/ShipStateHandler.java`：FollowMin=3、FollowMax=12、FleeHP=35 默认值。
- `src/main/java/com/lulan/shincolle/ai/EntityAIShipFollowOwner.java`、`EntityAIShipGuarding.java`、`EntityAIShipFloating.java`、`EntityAIShipRangeTarget.java`、`EntityAIShipPickItem.java`：FollowMin/FollowMax 用法。
- `src/main/java/com/lulan/shincolle/utility/EntityHelper.java` `checkOP`（L325-345）、`createShipEntity`（L2033-2087）。
- `src/main/java/com/lulan/shincolle/command/ShipCmdShipClearDrop.java`、`ShipCmdGetShip.java`、`ShipCmdKill.java`、`ShipCmdShipAttrs.java`、`ShipCmdStopAI.java`、`ShipCmdChangeShipOwner.java`、`ShipCmdUpdateOwnerUID.java`、`ShipCmdEmotes.java`、`ShipCmdShipInfo.java`。
- `src/main/java/com/lulan/shincolle/item/BasicEntityItem.java` 及其生成点（`BasicEntityShip`、`BasicEntityShipHostile`、重型怨念块）。
- `0aad501:src/main/java/com/lulan/shincolle/capability/CapaShipInventory.java`、`0aad501:.../client/gui/inventory/ContainerShipInventory.java`、`0aad501:.../client/gui/inventory/SlotShipInventory.java`：当前 `src/main` 中这些类缺失或处于半重构状态，按用户规则以 init commit 确认原意。

## Modern references

- `network/ShipCommandAction.java`、`ServerboundShipCommandPacket.java`、`ShipCommandService.java`、`ShipGuiButton.java`（新增）、`GameplayCommandHandler.java`、`GameplayCommandType.java`。
- `entity/ship/LegacyShipEntity.java`、`LegacyShipBehaviorCatalog.java`、`goal/LegacyShipFollowOwnerGoal.java`、`goal/LegacyShipPickItemGoal.java`。
- `menu/ShipInventoryMenu.java`、`menu/ShipPagedCargoSlot.java`（新增）、`client/screen/ShipInventoryScreen.java`。
- `command/ShinColleCommands.java`、`item/LegacyEntityItemMarker.java`（新增）、`blockentity/HeavyGrudgeBlockEntity.java`。
- `gametest/GameplayParityGameTests.java`、`resources/assets/shincolle/lang/{en_us,zh_cn,zh_tw}.json`。

## Acceptance assertions

- [x] 43 个 `ShipInv_*` 按钮（除 `NoFuel`，见 decision）在服务端按旧版写入对应状态。
- [x] 删除自创 AI 标志 `AUTO_SUPPLY`、`ROUTE_STAY`、`ALLOW_PVP`；PVP 判定回到 `PVPFirst`。
- [x] FollowMin/FollowMax 两值、默认 3/12、旧版联动（min>=max → max=min+1；max<=min → min=max-1）。
- [x] Shift 点击按 `0aad501` `transferStackInSlot` 顺序；装备判定为装备物品类型（旧版 `instanceof BasicEquip`）。
- [x] 背包 60 槽（6 装备 + 3×18 货仓），页码 0..2，非法值归零；可用货仓 `24 + DrumState*18`。
- [x] `/ship*` OP 指令只接受玩家发送者，创造模式或服务器 OP 视为 OP。
- [x] `/shipcleardrop` 只清除旧版 `BasicEntityItem` 语义掉落，Y 范围为整列世界高度（旧版 `0..256` 即 1.12 全高；1.20.1 适配为 `getMinBuildHeight()..getMaxBuildHeight()`）。
- [x] `/ship get` 生成路径先满血再读 NBT、血量 <1 设为 1、`CanDrop=true`、不改坐守/跟随/守卫。
- [x] `git diff --check` 通过；`compileJava` 与 `runGameTestServer` 通过。

## Frozen non-goals

- 不新增旧版没有的 GUI 状态或指令。
- 不修改 `src/main/**`。
