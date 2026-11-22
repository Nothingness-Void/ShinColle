# Batch Decision

- 结论：`COMPLETED`（7 条修复；1 条按钮子项 `NoFuel` 记录为不实现，理由见下）
- 影响矩阵：[MIGRATION-TEST-MATRIX.md](../../MIGRATION-TEST-MATRIX.md) T2（行为、网络、存档）
- 关联模块：menu-network-command

## Facts established

- 旧版 `PacketHelper.setEntityByGUI` 确有 43 个 `ShipInv_*` 按钮（`ID.B` 0..42），现代没有对应网络入口——审计属实。
- 旧版按钮直接写 `ID.F.*` / `ID.M.*`：`TarAI → PassiveAI`、`PVPAI → PVPFirst`、`OnSightAI → OnSightChase`、`AAAI → AntiAir`、`ASMAI → AntiSS`、`PickitemAI → PickItem`；旧版没有 `AutoSupply`、`RouteStay`、`AllowPvp` 标志——审计属实。
- 旧版 `UseCombatRation`（AutoCR）是阈值 `StateMinor`，自动吃口粮只受 `EntityHelper.getMoraleLevel(morale) >= UseCombatRation` 控制（`BasicEntityShip.java:1202`），没有额外开关。
- 旧版 FollowMin/FollowMax 两值，默认 3/12（`ShipStateHandler`），跟随/护卫用 `FollowMin/Max + width*0.75`，漂浮用 `+ width*0.5`，远程索敌回退 `max(2, FollowMax + 2)`，拾取范围 `pickRangeBase + FollowMax`——审计属实。
- 旧版 `/shipcleardrop` 只搜 `BasicEntityItem`，Y `0..256`；`/ship get` 生成路径 `setHealth(max)` → `readFromNBT` → `<1` 设 1 → `CanDrop=true`，不改坐守/跟随/守卫——审计属实。
- 旧版 `checkOP`：创造模式即 OP，否则 `canSendCommands`；`ShipCmd*` 的 `checkPermission` 只接受 `EntityPlayer`；`/shipinfo`、`/shipemotes` 不检查 OP——审计属实。
- 旧版 `ContainerShipInventory.transferStackInSlot`（`0aad501`）：货仓装备先进装备槽再进玩家背包；玩家装备先进装备槽再进货仓；玩家非装备只进货仓，失败不动——审计属实。
- InvPage：当前 `src/main` 的 `ContainerShipInventory` 引用缺失的 `CapaShipInventory`/`SlotShipInventory`（半重构状态）。按用户规则查 `0aad501`：`CapaShipInventory` 共 `6 + 18*3 = 60` 槽，`setInventoryPage` 只接受 0..2（否则归 0），`getSizeInventoryPaged() = 24 + DrumState*18`，`SlotShipInventory` 货仓槽索引 = `slotIndex + inventoryPage*18`。据此认定翻页是旧版原意，审计属实并已还原。

## Code changes

- 新增 `ShipGuiButton`（43 个旧版按钮 ID）与 `ShipCommandAction.SHIP_GUI_BUTTON` / `ServerboundShipCommandPacket.guiButton`；`ShipCommandService.setEntityByGui` 逐项对应 `PacketHelper.setEntityByGUI`（布尔按 `value > 0`，ModelState01..16 异或 `1 << n`，Sit 取反）。
- 删除 `SET_AI_FLAGS`、`SET_FOLLOW_RANGE`、`GameplayCommandType.SET_SHIP_AI_FLAGS/SET_SHIP_FOLLOW_RANGE`、`AI_FLAG_ALLOW_PVP/AUTO_SUPPLY/ROUTE_STAY` 及实体字段/NBT/门槛；舰船互攻门槛改回 `PVPFirst`；自动口粮只按 AutoCR 阈值。
- `LegacyShipEntity` 单值 `aiFollowRange`（4..64，默认 14）改为 `FollowMin/FollowMax`（默认 3/12，NBT `ShipFollowMin/ShipFollowMax`，兼容旧 `FMin/FMax`）；`LegacyShipFollowOwnerGoal`、护卫、漂浮、索敌回退、拾取范围改用两值。
- 舰船背包改为 60 槽 + 不持久化的页码（旧版 capability 字段同样不存档），`ShipPagedCargoSlot` 按页映射；可用货仓 `getInventorySizePaged()`（鼓数 = 装备栏 Drum 数，`EntityTransportWa` 固定 2）。
- `ShipInventoryMenu.quickMoveStack` 按 `0aad501` 顺序重写；装备槽校验改为 `LegacyEquipmentItem`（旧版 `BasicEquip`），不再按舰种 `canEquip` 限制。
- `/ship*`：OP 类指令 `requires` 改为玩家来源，执行时 `checkOp`（创造或 `PlayerList.isOp`，后者在 1.20.1 已包含单人开作弊主机判断，等价 `canSendCommands`）；非 OP 发 `chat.shincolle.command.notop`。
- `/shipcleardrop` 只清除带 `LegacyEntityItemMarker` 标记的 `ItemEntity`；Y 范围旧版 `0..256` 是 1.12 世界全高，1.20.1 世界下限为 -64，故适配为 `getMinBuildHeight()..getMaxBuildHeight()`（GameTest 结构位于 Y<0，固定 0..256 会漏掉旧版本应覆盖的整列）。标记仅打在旧版生成 `BasicEntityItem` 的位置（舰船回收蛋、敌舰蛋、重型怨念块掉落），是对旧版实体子类的最小 API 承载，不是新玩法。
- `/ship get` 生成路径恢复旧版血量与 `CanDrop`，删除 `clearCommandState`/`setOrderedToSit(false)`/`setCanFollow(false)`/`commandGuardBlock`。
- `ShipInventoryScreen` 去掉 AutoSupply/RouteStay/单值跟随，改为旧版按钮快捷键（Z/X/C/B/N 攻击开关、T PassiveAI、Y PVPFirst、U OnSight、I AntiAir、G AntiSS、J/K 与 Shift+J/K FollowMin/Max、Shift+[/] FleeHP、PageUp/PageDown 翻页等）。

## No-change audits

- `ShipInv_NoFuel`（按钮 39）：旧版 `ShipBtn` 发送端没有任何舰船 GUI 发送该按钮，唯一发送者是 `GuiMorphInventory`（经 `PID.MorphBtn`，属 morph 模块）；舰船侧 `ID.F.NoFuel` 由燃料状态驱动，现代以 `getShipFuel() <= 0` 承载。新增独立可写的 NoFuel 状态会制造旧版舰船 GUI 不可达的新状态，因此本批次不实现，`setEntityByGui` 对 39 不做处理。
- `MorphProfile` 的 `AiAllowPvp/AiAutoSupply/AiRouteStay` 属 morph 模块，超出本批次范围，未改动。

## Follow-up

- morph 模块复核 `MorphProfile` 自创 AI 字段与 `MorphBtn NoFuel`。
- [V0-CLIENT-GATE CLIENT-TEST-CASES](../2026-09-05-v0-client-gate/CLIENT-TEST-CASES.md) 中 V0-07 的 AutoSupply/RouteStay/跟随范围描述已被本批次推翻，客户端复测时按本批次快捷键表执行。
