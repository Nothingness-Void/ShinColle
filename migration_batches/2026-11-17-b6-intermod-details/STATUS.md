# Batch Status: B6-INTERMOD-DETAILS

- 状态：`COMPLETED`
- 开始：2026-11-18
- 完成：2026-11-18
- 最近更新：2026-11-18
- 当前阻塞：无

## Completed work

- 冻结 [protocol.md](protocol.md) 并完成全部 B6 实现与边界核对：
  - 注册全 9 条旧版 `/ship*` Brigadier 指令（含全部别名与 35 个 `EMO_ALIASES`）及 `ShipCacheSavedData.removeShip`、`LegacyShipEntity.stopAi` 开关、`0..100` 近代化存档区间与 `setModernizationSteps`。
  - 实现动态光方块 `TemporaryLightBlock` + `LightBlockEntity`（120-tick 自动恢复空气/水源/流水状态）+ `LightBlockHelper`，并在 `LegacyShipEntity` Searchlight/Flare 路径及 `GameplayClientEffects` 中接入 `ShinColleConfig.canSearchlight()` / `canFlare()`。
  - 在 `TeitokuHelper` 接入 `SystemConfig` 同步与客户端有效值缓存（`RingAbility`、`EasyMode`、`CloseGuiDist`、`RadarUpdate`、`PolyAsMn`）及 `teamCooldown`；新建并注册 `PolyAsMnCondition`（`shincolle:poly_as_mn`）和 `LegacyItemMatchHelper.matchesTag` 运行时 Manganese 标签切换（保持默认静态 Forge Tags 不变）；接通 `GameplayCommandHandler`、`TargetWrenchItem`、`DeskTerminalMenu`、`DeskReferenceScreen`、`HostileEncounterSpawner`、`LegacyShipCombatHelper`、`MorphHelper`、`MarriageRingItem`、`ShinColleSoundHelper` 的全部剩余 `ShinColleConfig` 开关。
  - 完成 `ShipKills`（含 `LegacyShipRensouhouEntity` / `LegacyShipAircraftEntity` 归属宿主计数）在 `ShipInventoryMenu` / `ShipInventoryScreen` 的显示，补全 `Raiden` 合体角色识别与 `en_us` / `zh_cn` / `zh_tw` 翻译键。
  - 实现 `TeitokuEvents#tickOptionalCuriosMarriageRing` 零硬依赖 Curios 反射软钩，并完成 Metamorph（原生 `MorphHelper` + `MorphCompatBridge`）、IC2（`RouteEnergyAccess` + `ForgeCapabilities.ITEM_HANDLER`/`FLUID_HANDLER`）及 `ID.java` 40 个 `HISTORICAL_SOURCE_GAP` 历史名称建档。
  - 在 `GameplayParityGameTests.java` 新增 3 项 B6 `@GameTest`；JDK `17.0.3.1` 全量构建、`verifyItemModelCoverage`、`runGameTestServer --no-daemon`（`All 289 required tests passed :)`）、`git diff --check` 与 `src/main/` 零改动验证全部通过。

## Remaining work

- 无（本批次为收官批次，`ROADMAP.md` 当前指针推进至 `NONE`）。

## Evidence links

- [README.md](README.md)
- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/README.md](results/README.md)
- [results/b6-closeout.json](results/b6-closeout.json)
