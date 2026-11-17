# Batch Status

- 状态：`DONE`
- 开始：2026-11-16
- 最近更新：2026-11-16
- 当前阻塞：无（B5 OreDictionary → Forge Tags 审计、资源补全、运行期匹配与 94 项 GameTest 闭环全部完成；交互式客户端 GUI 烟测按未请求启动客户端边界登记为延后项）

## Completed work

- 完成 1.12.2 真源对照审计：`ModOres.java`、`ModRecipes.java`、`ConfigHandler.java`、全量 84 个旧版 JSON 配方（其中 **39** 个含 `forge:ore_dict`）、`InventoryHelper.java`、`TileEntityCrane.java`、`ContainerCrane.java`、`GuiCrane.java`、`GuiHelper.java` 与 `EventHandler.java`。
- 补全运行期 Tag / Damage / NBT 匹配：
  - 新增 [LegacyItemMatchHelper.java](../../src/modern/java/com/lulan/shincolle/crafting/LegacyItemMatchHelper.java)，按 1.12 `InventoryHelper#matchTargetItem` 语义实现 `checkMetadata`、`checkOredict`（`ItemStack#getTags` 交集）与独立于 `Damage` 的 `checkNbt`。
  - 更新 [LegacyShipBehaviorCatalog.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java) 将 `matchesLegacyTaskItem` 复用统一 helper。
  - 更新 [CraneBlockEntity.java](../../src/modern/java/com/lulan/shincolle/blockentity/CraneBlockEntity.java)、[CraneTerminalMenu.java](../../src/modern/java/com/lulan/shincolle/menu/CraneTerminalMenu.java)、[CraneTerminalScreen.java](../../src/modern/java/com/lulan/shincolle/client/screen/CraneTerminalScreen.java) 与 [en_us.json](../../src/modern/resources/assets/shincolle/lang/en_us.json) / [zh_cn.json](../../src/modern/resources/assets/shincolle/lang/zh_cn.json) / [zh_tw.json](../../src/modern/resources/assets/shincolle/lang/zh_tw.json)，补齐 `M/D/N`（`checkMetadata`/`checkOredict`/`checkNbt`）过滤开关、`"meta"`/`"dict"`/`"nbt"` NBT 持久化及按钮/提示文案。
- 补全 Forge Item Tags：新增 `forge:paper`（`minecraft:paper`）与 8 个跨 mod 兼容空 Tag 声明（`forge:flour`、`forge:cooked_rice`、`forge:ingots/{tin,bronze,lead,steel,silver,uranium}`）。
- 修正并补齐合成表：修正 18 个现代配方的 Forge Tag、配方形状与产出数量（含损坏的 `ammo1`/`ammo2`/`ammo3`、`blockwaypoint` 竖直形状、`combatration` 两行形状、`combatration2` 原始行序与原版生鱼、`marriagering` `abyssnugget`），并补齐 26 个 1.12 遗漏配方（13 个常驻配方 + 13 个基于 `forge:not(forge:tag_empty)` 条件的跨 mod 配方）。
- 在 [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 修正 `blockwaypoint`/`combatration2` 旧测试网格与 `forge:paper` 校验，并新增 3 个 B5 专项 `@GameTest`；最终 `cleanProcessResources processResources compileJava verifyItemModelCoverage runGameTestServer` 通过，`All 94 required tests passed`。

## Remaining work

- 无（按 `ROADMAP.md` 晋级 `GAMETEST-BACKFILL` 为下一 `NEXT` 批次；交互式客户端 GUI 鼠标点击烟测留待 `B1-GUI-PARITY` / 人工客户端窗口验证）。

## Evidence links

- [README.md](README.md)
- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/README.md](results/README.md)
- [results/b5-ore-tags.json](results/b5-ore-tags.json)
