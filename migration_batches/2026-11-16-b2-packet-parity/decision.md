# Decision Record: B2-PACKET-PARITY

- 结论：`COMPLETED`（11 项单人/核心 PID 按 1.12.2 真源补齐；56 项已有或原生等价；17 项明确归属 `B3`/`B6` 后续专题并留档）
- 影响矩阵：M3、M7、M8

> **修订：部分已撤销（[D-012](../../MIGRATION-DECISIONS.md#d-012)）**。第4项中 Morph 专属 LegacyMountEntity 的存续/入口已撤销；普通舰娘坐骑和舰娘乘客仍保留，不能扩展为“玩家骑舰娘”或“变身+专属坐骑”技能宿主。删除专属命令枚举后，第1项旧序号兼容性不再是本批跨版本互通保证；两端须同步更新。

## 1. Facts established（全 84 有效 PID 核对分布）

- `C2SGUIPackets`（30 有效 PID + 1 unused `NO_USE=34`）：`22 MATCHED`、`6 IMPLEMENTED_HERE`（`24, 31, 35, 36, 50, 77`）、`2 PARTIAL_DEFERRED`（`ShipBtn(0)` 与 `MorphBtn(2)` 的罕用/调试子开关）。
- `C2SInputPackets`（15 PID）：`1 MATCHED`（`MorphGUI(13)`）、`6 NATIVE_EQUIVALENT`（`2, 5, 6, 7, 8, 9`）、`3 IMPLEMENTED_HERE`（`MountMove(0)`, `MountGUI(1)`, `PlayerSkill(12)`）、`5 DEFERRED_TO_B3_OR_B6`（`3, 4, 10, 11, 14`）。
- `S2CGUIPackets`（21 PID）：`17 NATIVE_EQUIVALENT`、`2 IMPLEMENTED_HERE`（`TileDesk(2)`, `TileWaypoint(4)`）、`2 PARTIAL_DEFERRED`（`SyncGUI_ShipInv(100)` 已同步弹药/怨念、仅未在现代 UI 使用 `Kills`；`SyncGUI_EntityItemList(102)` 归 `B3`/`B6`）。
- `S2CEntitySync`（13 PID）：`10 NATIVE_EQUIVALENT`、`3 DEFERRED_TO_B3_OR_B6`（`SyncShip_Buffmap`, `SyncEntity_AttackTime`, `SyncSystem_Config`）。
- `S2CReactPackets`（5 PID）：`5 PARTIAL_DEFERRED`（`FlareEffect` 粒子已接入、客户端光源方块与 4 个 `/ship*` 指令回执归 `B6`）。
- `S2CSpawnParticle`：`PARTIAL`（31 个现代 `GameplayParticleType` + `ClientboundCombatReactPacket` 已覆盖核心战斗/情绪/弹道粒子；少数特化线阵/3D 文本 FX 归 `B6`）。

## 2. Key alignment decisions

1. **复用既有命令与 BE 更新总线，禁止平行协议**：全部新增 C2S/B2 动作统一追加至 `GameplayCommandType` 并走 `ServerboundGameplayCommandPacket` + `GameplayCommandHandler`（末尾追加枚举值保持既有序兼容性）；`TileWaypoint`/`TileDesk` 同步走 Forge 原版 `BlockEntity#getUpdateTag` + `ClientboundBlockEntityDataPacket.create(this)`。
2. **Fleet Unit Name vs Admiral Team Name 解耦**：`FormationScreen` 此前错误复用 `DESK_RENAME_TEAM`，导致未先在提督桌创建多人队伍的单人玩家点击改名静默失败。新增 `SET_UNIT_NAME` 按当前分队（`0..8`）读写 `TeitokuData.teamNames`，保留 `DESK_RENAME_TEAM` 专用于 `DeskTerminalScreen` 修改提督阵营名。
3. **OP 权限门控**：对照 `C2SGUIPackets.java#L677`，为 `OPTOOL_TOGGLE_UNATTACKABLE` 恢复 `player.hasPermissions(2)` 服务端校验及 3 语种拒绝提示。
4. **Ship Mount 与 Passenger Rider 技能/交互闭环**：
   - `PlayerSkillHostResolver` 与 `MorphHelper.resolveHostMode`/`performPlayerSkill` 现统一解析骑乘 `LegacyShipMountEntity`（`MOUNT` -> 宿主舰娘）与肩扛/背负 `LegacyShipEntity`（`RIDER` -> 乘客舰娘）。
   - `MorphClientEvents` 在骑乘或背负舰娘时监听原版背包键触发 `OPEN_MOUNT_SHIP_INVENTORY`，并在骑乘 `LegacyShipMountEntity` / ~~`LegacyMountEntity`~~（后者部分**已撤销**，D-012） 时采集 WASD+Jump 掩码发送 `MOUNT_MOVE`，不干扰普通玩家按 `E` 开背包。
5. **ShipEntitySpecs 敌友边界保留与 `heavyCruisersKeepLegacyNightEffectsAndNePush` 批序去竞争**：
   - 经 `ShipEntitySpecs` 确认 `eggMeta 22`（`EntityCarrierHime`）保持 `hostile=true`，而 `eggMeta 35`（`EntityCarrierWDemon`）为 `hostile=false` 且具备 `CARRIER_WD_MOUNT`；因此 `LegacyShipMountEntity` 骑乘校验继续严格要求 `!host.isHostileVariant()`。
   - 新增 `b2*` 测试改变 GameTest 默认分批顺序后，暴露原测试 `heavyCruisersKeepLegacyNightEffectsAndNePush` 仅调用 `setDayTime(18000L)` 未刷新 `skyDarken` 导致 `level().isDay()` 受前序白天测试残留影响的夹具隐患，在测试内补一行 `helper.getLevel().updateSkyBrightness()` 解决，零改动生产战斗逻辑。

## 3. Code changes

- [GameplayCommandType.java](../../src/modern/java/com/lulan/shincolle/network/GameplayCommandType.java)
- [GameplayCommandHandler.java](../../src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java)
- [ShipCommandService.java](../../src/modern/java/com/lulan/shincolle/network/ShipCommandService.java)
- [TeitokuHelper.java](../../src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java)
- [WaypointBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/WaypointBlockEntity.java)
- [DeskBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/DeskBlockEntity.java)
- [PlayerSkillHostResolver.java](../../src/modern/java/com/lulan/shincolle/playerskill/PlayerSkillHostResolver.java)
- [MorphHelper.java](../../src/modern/java/com/lulan/shincolle/morph/MorphHelper.java)
- ~~`LegacyMountEntity.java`~~（旧修改清单；本批已删除，D-012）
- [LegacyShipMountEntity.java](../../src/modern/java/com/lulan/shincolle/entity/mount/LegacyShipMountEntity.java)
- [LegacyShipEntity.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java)
- [FormationScreen.java](../../src/modern/java/com/lulan/shincolle/client/screen/FormationScreen.java)
- [MorphClientEvents.java](../../src/modern/java/com/lulan/shincolle/client/MorphClientEvents.java)
- [PointerClientEvents.java](../../src/modern/java/com/lulan/shincolle/client/PointerClientEvents.java)
- [en_us.json](../../src/modern/resources/assets/shincolle/lang/en_us.json)、[zh_cn.json](../../src/modern/resources/assets/shincolle/lang/zh_cn.json)、[zh_tw.json](../../src/modern/resources/assets/shincolle/lang/zh_tw.json)
- [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java)

## 4. Follow-up

- `B3-MULTIPLAYER-TEAM`：多人 Team 权限与 `SyncGUI_EntityItemList` 远距雷达推送审定。
- `B6-INTERMOD-DETAILS`：`/ship*` 原版聊天命令及对应 4 个 React 包、`SyncSystem_Config`、`SyncShip_Buffmap`、`SyncEntity_AttackTime`、`ShipBtn`/`MorphBtn` 剩余冷门子按钮及粒子/客户端光源补全。
