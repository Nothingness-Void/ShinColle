# Frozen Protocol — B6-INTERMOD-DETAILS

## Legacy references

只列出为回答本批次问题必须读取的旧版文件：

- `src/main/java/com/lulan/shincolle/handler/CommandHandler.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdChangeShipOwner.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdEmotes.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdGetShip.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdKill.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdShipAttrs.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdShipClearDrop.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdShipInfo.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdStopAI.java`
- `src/main/java/com/lulan/shincolle/command/ShipCmdUpdateOwnerUID.java`
- `src/main/java/com/lulan/shincolle/utility/CommandHelper.java`
- `src/main/java/com/lulan/shincolle/handler/ConfigHandler.java`
- `src/main/java/com/lulan/shincolle/init/ModOres.java`
- `src/main/java/com/lulan/shincolle/network/S2CEntitySync.java`（`SyncSystem_Config`）
- `src/main/java/com/lulan/shincolle/block/BlockLightAir.java`
- `src/main/java/com/lulan/shincolle/block/BlockLightLiquid.java`
- `src/main/java/com/lulan/shincolle/tileentity/TileEntityLightBlock.java`
- `src/main/java/com/lulan/shincolle/utility/BlockHelper.java`（`placeLightBlock`）
- `src/main/java/com/lulan/shincolle/item/MarriageRing.java`
- `src/main/java/com/lulan/shincolle/client/gui/inventory/GuiShipInventory.java`（`Kills` / `Raiden`）
- `src/main/java/com/lulan/shincolle/reference/ID.java`

## Modern references

预计创建、修改或验证的现代文件：

- `src/modern/java/com/lulan/shincolle/ShinColle.java`
- `src/modern/java/com/lulan/shincolle/command/ShinColleCommands.java`
- `src/modern/java/com/lulan/shincolle/block/TemporaryLightBlock.java`
- `src/modern/java/com/lulan/shincolle/blockentity/LightBlockEntity.java`
- `src/modern/java/com/lulan/shincolle/world/LightBlockHelper.java`
- `src/modern/java/com/lulan/shincolle/registry/ModBlocks.java`
- `src/modern/java/com/lulan/shincolle/registry/ModBlockEntities.java`
- `src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java`
- `src/modern/java/com/lulan/shincolle/crafting/LegacyItemMatchHelper.java`
- `src/modern/java/com/lulan/shincolle/crafting/PolyAsMnCondition.java`
- `src/modern/java/com/lulan/shincolle/config/ShinColleConfig.java`
- `src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java`
- `src/modern/java/com/lulan/shincolle/teitoku/TeitokuEvents.java`
- `src/modern/java/com/lulan/shincolle/teitoku/ShipCacheSavedData.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/entity/mount/LegacyMountEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/mount/LegacyShipMountEntity.java`
- `src/modern/java/com/lulan/shincolle/item/MarriageRingItem.java`
- `src/modern/java/com/lulan/shincolle/item/TargetWrenchItem.java`
- `src/modern/java/com/lulan/shincolle/morph/MorphHelper.java`
- `src/modern/java/com/lulan/shincolle/sound/ShinColleSoundHelper.java`
- `src/modern/java/com/lulan/shincolle/world/HostileEncounterSpawner.java`
- `src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java`
- `src/modern/java/com/lulan/shincolle/menu/DeskTerminalMenu.java`
- `src/modern/java/com/lulan/shincolle/menu/ShipInventoryMenu.java`
- `src/modern/java/com/lulan/shincolle/client/screen/DeskReferenceScreen.java`
- `src/modern/java/com/lulan/shincolle/client/screen/ShipInventoryScreen.java`
- `src/modern/resources/assets/shincolle/lang/en_us.json`
- `src/modern/resources/assets/shincolle/lang/zh_cn.json`
- `src/modern/resources/assets/shincolle/lang/zh_tw.json`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [x] 9 条旧版 `/ship*` 指令（含别名与权限边界）通过 `RegisterCommandsEvent` 接入并可独立在服务端闭环执行。
- [x] `BLOCK_LIGHT_AIR` 与 `BLOCK_LIGHT_LIQUID` 具备 `15` 亮度、`LightBlockEntity` 120-tick 自动还原原空气/水源且由 Searchlight 与 Flare 触发。
- [x] `SystemConfig`（`ringAbility` 等）登录与状态包同步、`ShinColleConfig` 全量运行时接入、`polyAsMn` 标签匹配与 `shincolle:poly_as_mn` 配方条件均对齐且不破坏默认 `polyAsMn=false` 契约。
- [x] `ShipKills` 击杀计数与 `Raiden` 合体角色标识同步至 `ShipInventoryMenu` / `ShipInventoryScreen`；Curios 采用零硬依赖反射钩子；Metamorph/IC2 现代等价与 40 个 `ID.java` `HISTORICAL_SOURCE_GAP` 名称明确建档。
- [x] 新增 3 项 B6 `@GameTest` 并通过全量 `289/289` 回归、`git diff --check` 与 `src/main/` 零改动门禁。

## Frozen non-goals

- 不改动静态 `src/modern/resources/data/forge/tags/` JSON（保持默认 `polyAsMn=false` 测试与数据包行为稳定）。
- 不为仅存在常量的 40 个 `ID.java` 历史名称伪造新实体。
