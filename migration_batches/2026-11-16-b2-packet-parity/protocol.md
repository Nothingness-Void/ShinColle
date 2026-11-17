# Frozen Protocol — B2-PACKET-PARITY

## Legacy references

只列出为回答本批次问题必须读取的旧版文件：

- `src/main/java/com/lulan/shincolle/network/C2SGUIPackets.java`（PID 0..3, 20..36, 50..51, 70..77）
- `src/main/java/com/lulan/shincolle/network/C2SInputPackets.java`（PID 0..14）
- `src/main/java/com/lulan/shincolle/network/S2CGUIPackets.java`（PID 0..5, 40..49, 80..81, 100..102）
- `src/main/java/com/lulan/shincolle/network/S2CEntitySync.java`（PID 0..3, 51..57, 80..81）
- `src/main/java/com/lulan/shincolle/network/S2CReactPackets.java`（PID 0..3, 20）
- `src/main/java/com/lulan/shincolle/network/S2CSpawnParticle.java`
- 直接调用点与语义真源：`PacketHelper.java`、`InteractHelper.java`、`ReactionHandler.java`、`ShipSkillHandler.java`、`BasicEntityShip.java`、`TileEntityDesk.java`

## Modern references

核对、修改与验证的现代文件：

- `src/modern/java/com/lulan/shincolle/network/ModNetwork.java`
- `src/modern/java/com/lulan/shincolle/network/GameplayCommandType.java`
- `src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java`
- `src/modern/java/com/lulan/shincolle/network/ServerboundGameplayCommandPacket.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandAction.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandService.java`
- `src/modern/java/com/lulan/shincolle/network/ServerboundShipCommandPacket.java`
- `src/modern/java/com/lulan/shincolle/network/ClientboundCombatReactPacket.java`
- `src/modern/java/com/lulan/shincolle/network/ClientboundGameplayStatePacket.java`
- `src/modern/java/com/lulan/shincolle/network/ClientboundSpawnParticlePacket.java`
- `src/modern/java/com/lulan/shincolle/network/ClientboundSyncTeitokuDataPacket.java`
- 修改的调用入口：`WaypointBlockEntity.java`、`DeskBlockEntity.java`、`TeitokuHelper.java`、`PlayerSkillHostResolver.java`、`MorphHelper.java`、`LegacyMountEntity.java`、`LegacyShipMountEntity.java`、`LegacyShipEntity.java`、`FormationScreen.java`、`MorphClientEvents.java`、`PointerClientEvents.java`、`en_us.json`、`zh_cn.json`、`zh_tw.json`
- 测试入口：`src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [x] 对全部 84 个有效旧版 PID + `NO_USE(34)` + `S2CSpawnParticle` 建立逐 PID 映射矩阵（`MATCHED` / `NATIVE_EQUIVALENT` / `IMPLEMENTED_HERE` / `DEFERRED_TO_B3_OR_B6`），无未解释遗漏。
- [x] 对单人/通用核心缺失分支按 1.12.2 语义补齐最小现代实现，包含边界检查、权限校验与 `encode/decode` 往返。
- [x] 新增 5 项 B2 专项 `@GameTest` 覆盖全部新补分支及 `encode/decode` / handler 边界，未新增第二套平行通信架构。
- [x] `src/main/` 保持只读零改动（`git status -s -- src/main/` 为空）。
- [x] `git diff --check`、JDK 17 `.\gradlew.bat cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon` 全部通过（`282/282`，0 失败，0 跳过）。

## Frozen non-goals

- 不展开 `B3-MULTIPLAYER-TEAM` 的多人 Team 权限与结盟玩法扩展。
- 不展开 `B6-INTERMOD-DETAILS` 的 `ForgeConfigSpec` 全量配置、外置 mod 联动及 `/ship*` 原版聊天命令迁移。
- 不修改任何与网络同步无关的实体战斗数值、worldgen 或配方。
