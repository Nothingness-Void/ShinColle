# Batch Status: B2-PACKET-PARITY

- 状态：`COMPLETED`
- 里程碑：`M3 / M7 / M8: B2-PACKET-PARITY`
- 开始：2026-11-16
- 完成：2026-11-16
- 当前阻塞：无

## Summary of Accomplishments

1. 通过 Swarm 并发完成旧版 6 个核心网络类（`C2SGUIPackets` 30 有效 PID + 1 unused `NO_USE`、`C2SInputPackets` 15 PID、`S2CGUIPackets` 21 PID、`S2CEntitySync` 13 PID、`S2CReactPackets` 5 PID、`S2CSpawnParticle`）共 **84 个有效旧版 PID** 与现代通信层的逐 PID 核对：`23 MATCHED`、`33 NATIVE_EQUIVALENT`、`11 IMPLEMENTED_HERE`、`17 DEFERRED_TO_B3_OR_B6 / PARTIAL_DEFERRED`。
2. 补齐 11 项单人/核心缺失网络与同步语义（复用 `GameplayCommandType` + `ServerboundGameplayCommandPacket` + 原版 `ClientboundBlockEntityDataPacket`，未新增平行协议）：
   - `WaypointBlockEntity` / `DeskBlockEntity`：`getUpdateTag()` + `getUpdatePacket()`，Desk 新增 `radarZoomIndex`/`bookChapter`/`bookPage` 同步及 `SYNC_DESK_FUNC`。
   - `SyncPlayerItem (24)`：`GameplayCommandHandler#handleSetPointerMode` 跨模式（`% 3 != 0`）切回单舰模式时自动选中当前编队首舰（`TeitokuHelper#focusFirstShipInCurrentTeam`）。
   - `SetFormation (31)`：新增 `GameplayCommandType.SET_FORMATION` 与 `TeitokuHelper#setFormationId`（保留 `> 4` 舰生效门控）。
   - `SetUnitName (36)`：新增 `GameplayCommandType.SET_UNIT_NAME` 与 `TeitokuHelper#setUnitName`（按分队 `0..8` 独立命名，不再受限于 Desk Team 是否创建），并更新 `FormationScreen` 改名按钮。
   - `SetUnatkClass (50)`：`handleOpToolToggleUnattackable` 补齐 1.12 `!player.hasPermissions(2)` OP 权限校验与 3 语种提示。
   - `HitHeight (35)`：新增 `SET_SHIP_HIT_ZONE`、客户端 `PointerClientEvents#onClientTickHitZone` 4-tick 射线分区及 `LegacyShipEntity` 摸头区（`hitHeight >= 75`）`+30` 士气反应。
   - `PlayerSkill (12)` & `MountGUI (1)` & `MountMove (0)`：修复 `PlayerSkillHostResolver` / `MorphHelper` 支持 `LegacyShipMountEntity`（`MOUNT`）与背负舰娘乘客（`RIDER`）；补齐 `OPEN_MOUNT_SHIP_INVENTORY`（骑乘/背负按背包键打开舰娘背包）与 `MOUNT_MOVE`（WASD+Jump 比特掩码 `1|2|4|8|16` 接管 `LegacyShipMountEntity` 与 `LegacyMountEntity`）。
3. 在 [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 新增 5 项 B2 `@GameTest`，并修正 `heavyCruisersKeepLegacyNightEffectsAndNePush` 夹具缺失 `updateSkyBrightness()` 的批序时序竞争。
4. 全量验证：`git diff --check` 退出 0，`src/main/` 零改动，JDK 17 `.\gradlew.bat cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon` 绿灯通过，**`All 282 required tests passed :)`**（282/282，0 失败，0 跳过）。

## Evidence links

- [README.md](README.md)
- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/README.md](results/README.md)
- [results/b2-packet-parity.json](results/b2-packet-parity.json)
