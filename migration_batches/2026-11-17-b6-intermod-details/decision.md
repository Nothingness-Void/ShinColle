# Batch Decision: B6-INTERMOD-DETAILS

- 结论：`COMPLETED`
- 影响矩阵：M0、M1、M2、M3、M4、M5、M6、M8、M9
- 关联模块：Commands, Dynamic Light, Config/SystemSync/polyAsMn, Inter-mod & UI Polish

## Facts established

- 1.12.2 `CommandHandler` 注册的 9 条 `/ship*` 指令及其别名、35 项 `EMO_ALIASES`、`ShipAttrs` 近代化上限、全球 AI 冻结（`stopAI`）与 `ShipCache` 增删回忆已在 1.20.1 通过 Brigadier 与 `RegisterCommandsEvent` 完整闭环。
- 1.12.2 `BlockLightAir` / `BlockLightLiquid` + `TileEntityLightBlock` 采用 120-tick 寿命并在 `orgType` 中区分空气（0）、静水（1）、流水（2）恢复原方块；现代 `blocklightair.json` / `blocklightliquid.json` BlockState 仅含默认变体 `""`，因此状态存入 `LightBlockEntity` 而非新增 BlockState 属性。
- `DataPack` 静态 Tag JSON（`data/forge/tags/items/...`）必须维持 `polyAsMn=false` 默认契约；运行时切换由 `PolyAsMnCondition`（`shincolle:poly_as_mn`，通过 `CraftingHelper.register` 注册）与 `LegacyItemMatchHelper.matchesTag` / `matchesTargetItem` 承担。
- 外置 Mod 现代架构边界：
  - **Metamorph** → 已由原生 `MorphHelper` / `PlayerSkillHostResolver` + `MorphCompatBridge.NOOP`（存在 `metamorph` 时载入 `SoftDependencyBridge`，不引入编译期依赖）承载，并受 `ShinColleConfig.enableMetamorphSkill()` 开关门控。
  - **IC2 / 能源管道** → 1.20.1 无 IC2；机器方块实体（`HeavyGrudgeBlockEntity`、`PolymetalServantBlockEntity`、`LegacyCoreBlockEntity`、`SmallShipyardBlockEntity`）暴露 `ForgeCapabilities.ITEM_HANDLER`（与 `FLUID_HANDLER`），内部能源通过 `RouteEnergyAccess` 流转，未暴露未使用的 `ForgeCapabilities.ENERGY`。
  - **Curios（替代旧版 Baubles）** → 在 `TeitokuEvents#tickOptionalCuriosMarriageRing` 通过 `ModList.get().isLoaded("curios")` 与反射调用 `CuriosApi.getCuriosInventory` -> `getEquippedCurios()` 触发 `MarriageRingItem.tickActiveRingEffects`，零编译期强依赖。
- 40 个 `ID.java` `HISTORICAL_SOURCE_GAP` 类 ID（无 1.12.2 实体类与 `ModEntity.regShipEntity` 注册入口）已在 [ship-universe-audit.json](../2026-09-04-ship-universe-audit/results/ship-universe-audit.json) 与 [results/b6-closeout.json](results/b6-closeout.json) 精确列出，严禁按常量名称虚构实体：
  - `4` (`CLHO`/`EntityLightCruiserHo`), `5` (`CLHE`/`EntityLightCruiserHe`), `6` (`CLTO`/`EntityLightCruiserTo`), `7` (`CLTSU`/`EntityLightCruiserTsu`), `8` (`CLTCHI`/`EntityLightCruiserChi`), `11` (`CVLNU`/`EntityCarrierNu`), `22` (`ArmoredCVHime`/`EntityArmoredCarrierHime`), `23` (`AnchorageHime`/`EntityAnchorageHime`), `24` (`HarbourWD`/`EntityHarbourWD`), `25` (`AnchorageWD`/`EntityAnchorageWD`), `32` (`SouthernHime`/`EntitySouthernHime`), `34` (`CLDemon`/`EntityLightCruiserDemon`), `35` (`BBWD`/`EntityBattleshipWD`), `40` (`STHime`/`EntitySeaplaneHime`), `41` (`AirdefenseHime`/`EntityAirdefenseHime`), `42` (`APMamiya`/`EntityAPMamiya`, `NameMap` 同含 `2042`), `43` (`CLHime`/`EntityCLHime`), `45` (`DDWD`/`EntityDestroyerWD`), `50` (`SupplyDepotHime`/`EntitySDHime`), `55` (`Raiden` 保留合体语义与角色显示；未注册独立实体类，`NameMap` 对应 `"Entity"`), `64` (`DDNA`/`EntityDestroyerNa`), `65` (`DDAH`/`EntityDestroyerAH`), `66` (`STWH`/`EntitySeaplaneWH`), `67` (`NorthernWH`/`EntityNorthernWH`), `68` (`JellyfishHime`/`EntityJellyfishHime`), `69` (`EscortHime`/`EntityEscortHime`), `70` (`EuropeanHime`/`EntityEuropeanHime`), `71` (`CentralHime`/`EntityCentralHime`), `73` (`FrenchHime`/`EntityFrenchHime`), `74` (`NightStraitHime`/`EntityNightStraitHime`), `75` (`EntombedAAHime`/`EntityEntombedAAHime`), `76` (`NorthlandHime`/`EntityNorthlandHime`), `77` (`SupplyDepotSH`/`EntitySDSH`), `78` (`SSSH`/`EntitySubmSH`), `79` (`BBSH`/`EntityBattleshipSH`), `80` (`CASH`/`EntityCASH`), `81` (`CVSH`/`EntityCarrierSH`), `82` (`HarbourSH`/`EntityHarbourSH`), `83` (`LycorisHime`/`EntityLycorisHime`), `84` (`TwinHime`).

## Code changes

- 新增：`ShinColleCommands.java`、`TemporaryLightBlock.java`、`LightBlockEntity.java`、`LightBlockHelper.java`、`PolyAsMnCondition.java`。
- 修改：`ShinColle.java`、`ModBlocks.java`、`ModBlockEntities.java`、`GameplayClientEffects.java`、`LegacyItemMatchHelper.java`、`TeitokuHelper.java`、`TeitokuEvents.java`、`ShipCacheSavedData.java`、`LegacyShipEntity.java`、`LegacyShipRensouhouEntity.java`、`LegacyShipAircraftEntity.java`、`LegacyShipCombatHelper.java`、`LegacyMountEntity.java`、`LegacyShipMountEntity.java`、`MarriageRingItem.java`、`TargetWrenchItem.java`、`MorphHelper.java`、`ShinColleSoundHelper.java`、`HostileEncounterSpawner.java`、`GameplayCommandHandler.java`、`DeskTerminalMenu.java`、`ShipInventoryMenu.java`、`DeskReferenceScreen.java`、`ShipInventoryScreen.java`、`en_us.json`、`zh_cn.json`、`zh_tw.json` 与 `GameplayParityGameTests.java`。

## No-change audits

- 静态 `src/modern/resources/data/forge/tags/items/` 与 `morph` 默认 HP 缩放公式、`ShipInventoryMenu#stillValid` 远程 Desk 兼容逻辑经核对保持不变；Metamorph / IC2 / 40 个 `ID.java` 历史名称按设计边界收口，不引入外部强依赖或虚构实体。

## Follow-up

- 全项目收尾：将 `ROADMAP.md` 当前指针推进至 `NONE`，并更新 `CURRENT_STATE.md`、`MIGRATION-DECISIONS.md`、`MIGRATION-TEST-MATRIX.md`、`MIGRATION-CHANGELOG.md`、`MIGRATION-FLOW-CHECKLIST.md`。
