# B5-ORE-TAGS Protocol

- 批次 ID：`B5-ORE-TAGS`
- 开始：2026-11-16
- 目标：严格对照旧版 1.12.2 `ModOres`、`ModRecipes`、所有使用 `ore_dict` 的配方、`InventoryHelper`、`TileEntityCrane`、`EventHandler` 与 `ShinColle`，补全现代 Forge Tags（及必要的兼容 tag JSON）、`shincolle` 配方标签化与起重机过滤开关（`checkMetadata` / `checkOredict` / `checkNbt`），并通过编译、资源、模型与 GameTest 全量验收。

## 旧版入口（只读真源）

1. OreDictionary 注册与配置：
   - `src/main/java/com/lulan/shincolle/init/ModOres.java`
   - `src/main/java/com/lulan/shincolle/ShinColle.java`（初始化/调用入口）
   - `src/main/java/com/lulan/shincolle/handler/ConfigHandler.java`（`polyAsMn` 等开关默认值）
   - `src/main/java/com/lulan/shincolle/handler/EventHandler.java`（OreRegisterEvent / 矿辞相关事件）
2. 配方与冶炼：
   - `src/main/java/com/lulan/shincolle/init/ModRecipes.java`
   - `src/main/resources/assets/shincolle/recipes/*.json`（全部 38 个含 `ore_dict` 的旧版配方，含 `ammoLight*` / `ammoHeavy*` 与各工具/方块/耗材配方）
3. 运行期 OreDictionary 匹配与起重机过滤：
   - `src/main/java/com/lulan/shincolle/utility/InventoryHelper.java`（`matchItem` 等匹配函数）
   - `src/main/java/com/lulan/shincolle/tileentity/TileEntityCrane.java`（`checkMetadata`、`checkOredict`、`checkNbt` 字段、保存读取与过滤判定）
   - `src/main/java/com/lulan/shincolle/client/gui/GuiCrane.java` / `ContainerCrane.java`（如有按钮/同步依赖，仅审计 `checkMetadata` / `checkOredict` / `checkNbt` 开关语义，其余 GUI 留 B1）

## 现代入口

- `src/modern/resources/data/forge/tags/**/*.json`
- `src/modern/resources/data/shincolle/tags/**/*.json`
- `src/modern/resources/data/shincolle/recipes/*.json`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`（`matchesLegacyTaskItem` 等）
- `src/modern/java/com/lulan/shincolle/blockentity/CraneBlockEntity.java`
- `src/modern/java/com/lulan/shincolle/menu/CraneTerminalMenu.java`
- `src/modern/java/com/lulan/shincolle/client/screen/CraneTerminalScreen.java`
- `src/modern/java/com/lulan/shincolle/network/`（起重机命令包 / GameplayCommandHandler / Packet，视按钮同步需要）
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## 验收断言

- `forge:tags` 与 `shincolle:tags` 精确镜像 1.12.2 `ModOres.java` 默认注册项及 `polyAsMn=false` 边界（对 `polyAsMn` 的迁移边界有明确源码依据与决策记录）。
- 现代 `data/shincolle/recipes/*.json` 对所有 1.12.2 使用 `forge:ore_dict` 且目标原料在 1.20.1 / Forge Tag 中合法存在并可解析的配方完成 tag 对齐（含原有缺失或硬编码单体配方的核对与安全补充，避免空 tag 配方破损）。
- `CraneBlockEntity`（及配套 Menu/Packet/Screen 若需要）严格按 1.12.2 `TileEntityCrane` + `InventoryHelper` 支持 `checkMetadata`（在 1.20 对应同物品/伤害值语义）、`checkOredict`（Tag 交集匹配）、`checkNbt`（NBT/Tag 严格匹配）开关与存档字段，并保留默认值一致性。
- 新增/扩展 B5 专属 `@GameTest` 覆盖 Forge Tags、配方 tag 解析、`CraneBlockEntity` 三项过滤开关组合及 `LegacyShipBehaviorCatalog.matchesLegacyTaskItem`。
- `git diff --check`、`compileJava`、`cleanProcessResources processResources`、`verifyItemModelCoverage`、`runGameTestServer` 全部通过且 0 失败。

## 非目标

- 不发明 1.12.2 不存在的配方或虚假 mod 矿物物品；如某旧版 mod 联动标签在纯原版+Forge 环境没有原生等价物且使用空白 Forge tag 需确认 1.20.1 `CraftingRecipe` 空 tag 匹配行为是否安全（严禁产生无法加载或报错的配方 JSON）。
- 不展开 B1 完整 GUI、B2 全量 packet 或 B3 多人队伍迁移。
