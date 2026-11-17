# B5-ORE-TAGS Decision

- 结论：`DONE`
- 影响模块：M4（Crane / Task 过滤匹配）、M8（`CraneTerminalScreen` 过滤切换按键与本地化）、M9（Forge Item Tags 与 `shincolle:recipes`）
- 遗留/延后边界：
  - `ConfigHandler.polyAsMn` 在 1.12 默认为 `false`（不将 `polymetal` 注册为锰矿 OreDict）；现代维持默认 `false` 不注入 `forge:ingots/manganese` 标签，运行时配置开关与跨 mod 动态切换继续归入 `B6-INTERMOD-DETAILS`。
  - 现代 [CraneTerminalScreen.java](../../src/modern/java/com/lulan/shincolle/client/screen/CraneTerminalScreen.java) 复用了既有的 `gui_crane.png` 按钮贴图位置（`(23,22)`/`(37,22)`/`(51,22)` 与按下高亮 `(176,13/24/46)`）；按本次不主动启动客户端边界，未运行交互式 GUI 客户端窗口测试，屏幕按钮渲染留待客户端烟测或 `B1-GUI-PARITY`。

## 1.12.2 真源对照结论

### 1. Forge Item Tags 与 OreDictionary 注册 (`ModOres.java`)
- 1.12 `ModOres.java` 注册 `orePolymetal`、`blockAbyssium`、`blockPolymetal`、`blockGrudge`、`blockGrudgeHeavy`、`itemGrudge`（含 `Grudge:0` 与 `Grudge:1`）、`ingotAbyssium`、`ingotPolymetal`、`nuggetAbyssium`、`nuggetPolymetal`、`foodCombatRation`（全部 6 种 `CombatRation:0..5`）共 11 组默认条目（均已在 `data/forge/tags` 对应存在），并受 `polyAsMn=false` 默认开关抑制锰矿条目。
- 现代 Forge 47.1.47 内置已提供 `forge:ingots/{iron,gold,copper}`、`forge:gems/{diamond,lapis}`、`forge:dyes/blue`、`forge:gunpowder`、`forge:obsidian`、`forge:rods/wooden`、`forge:crops/wheat`、`forge:nether_stars`、`forge:storage_blocks/diamond`；缺少 `forge:paper` 及 8 个跨 mod 兼容标签。
- 现代补建 9 个 JSON 文件：`forge:paper` 指向 `minecraft:paper`；`flour`、`cooked_rice` 与 `ingots/{tin,bronze,lead,steel,silver,uranium}` 声明为空列表 `[]`，避免配方解析警告并允许数据包/跨 mod 扩展生效。
- 注意 MC 1.14+ 扁平化后 `forge:dyes/blue` 不再包含 `minecraft:lapis_lazuli`（青金石独立在 `forge:gems/lapis`），与 1.12 `itemRecipePaper.json` 同时接受 `[gemLapis, dyeBlue]` 的设计保持一致。

### 2. 合成表语义对齐 (`src/main/resources/assets/shincolle/recipes/*.json`)
- 全量核查 84 个 1.12 JSON 配方与 56 个现代 JSON 配方，确认旧版实际包含 **39** 个 `forge:ore_dict` 配方（含 `ammoLight24.json` 使用 `"gunpowder"`），并发现现代实现存在 6 类历史偏差（非仅缺少 Tag）：
  1. `ammo1.json` 与 `ammo3.json` 错误写成金锭+怨念+火药/烈焰粉配方，按 1.12 `ammoContainerLight.json` 与 `ammoContainerHeavy.json` 修正为 `3x3 shincolle:ammo -> ammo1` 与 `3x3 shincolle:ammo2 -> ammo3`，同时补齐逆向拆解 `ammo_from_ammo1` 与 `ammo2_from_ammo3`（各 9 个）。
  2. `ammo2.json` 产出数量 `2` 修正为 1.12 `ammoHeavy4a.json` 的 `4`。
  3. `blockwaypoint.json` 从无序改为 1.12 `blockWaypoint.json` 竖直有序 `["g", "s"]`（怨念在上、木棍 `#forge:rods/wooden` 在下，产出 16）。
  4. `combatration.json` 改为 1.12 `itemCombatRation0.json` 的 2 行配方 `["bcg", "www"]`（`bread / #forge:flour / #forge:cooked_rice`、`golden_carrot`、`grudge` + `#forge:crops/wheat`）；`combatration2.json` 修正中行为 `["www", "bcg", "www"]`（`w` 支持 `forge:crops/wheat` 与 `forge:flour`，`b` 支持 `shincolle:cooked_meat` 及 4 种原版生鱼 `cod/salmon/pufferfish/tropical_fish`，对应 1.12 `clownfish`）。
  5. `marriagering.json` 按 1.12 `itemMarriageRing.json` 从 `abyssmetal` + `nether_star` 修正为 `shincolle:abyssnugget` + `#forge:nether_stars`。
  6. 其余 12 个已有配方（`abyssmetal_from_iron_and_grudge`、`ammo`、`blockdesk`、`blockframe`、`blocksmallshipyard`、`blockvolblock`、`blockvolcore`、`ownerpaper`、`recipepaper`、`repairgoddess`、`shiptank1`）将硬编码原版材料替换为对应 `forge:` Tag。
- 补齐 26 个缺失的 1.12 分支配方：
  - 13 个常驻配方：`ammo_from_ammo1`、`ammo2_from_ammo3`、`ammo_from_{abyssium,copper,gold,diamond}`、`ammo2_from_{abyssium,copper,gold,diamond}`、`blockvolblock_alt`（`blockVolcano1b`）、`blockvolcore_alt`（`blockVolcanoCore1a`）、`repairgoddess_alt`（`itemRepairGoddess1b`）。
  - 13 个跨 mod 条件配方：使用 Forge 原生顶层 `"conditions": [{"type": "forge:not", "value": {"type": "forge:tag_empty", "tag": "..."}}]` 门控 `ammo_from_{tin,bronze,lead,steel,silver,uranium}`、`ammo2_from_{tin,bronze,lead,steel,silver,uranium}` 与 `combatration2_rice`（`itemCombatRation2b`）；当目标 Tag 为空时自动屏蔽，不污染原版配方表。

### 3. 运行期 Tag / Meta / NBT 匹配 (`InventoryHelper`, `TileEntityCrane`, `ContainerCrane`, `GuiCrane`)
- 1.12 `InventoryHelper#matchTargetItem(target, temp, checkMetadata, checkNbt, checkOredict)` 当物品不同时只在 `checkOredict == true` 下检查 OreDictionary 交集；同物品时分别独立判断 damage 与 NBT，不因损毁度使 `checkNbt` 误判。
- 1.20.1 中耐久写入 `ItemStack` 的 `"Damage"` NBT 键，故 `LegacyItemMatchHelper#matchesTargetItem` 在 `checkNbt` 比较前剔除 `ItemStack.TAG_DAMAGE` 保持与 1.12 一致；`LegacyShipBehaviorCatalog#matchesLegacyTaskItem` 与 `CraneBlockEntity#matchesTransferFilter` 统一复用该 helper。
- `CraneBlockEntity` 补齐 `checkMetadata`/`checkOredict`/`checkNbt` 默认 `false` 状态、1.12 NBT 键名 `"meta"`/`"dict"`/`"nbt"`、`CraneTerminalMenu` 按钮 `6/7/8` 与 `CraneTerminalScreen` 工具提示键 `gui.shincolle.crane.{usemeta,useoredict,usenbt}`。
