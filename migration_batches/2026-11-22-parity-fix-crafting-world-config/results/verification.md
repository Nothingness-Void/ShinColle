# Verification

环境：JDK `17.0.20.1`（Ubuntu），`bash ./gradlew ... --no-daemon`（仓库内 `gradlew` 无可执行位，用 `bash` 调用）。

| 命令 | 结果 |
| --- | --- |
| `./gradlew compileJava` | `BUILD SUCCESSFUL` |
| `./gradlew runGameTestServer`（本分支，两次） | `297` 项运行，`296` 通过 / `1` 失败：`passiveAiStopsAutomaticRangeTargeting` |
| `./gradlew runGameTestServer`（基线 `origin/codex/mc-1.20.1` `8f1d2fd`，未改动） | `298` 项运行，同样只有 `passiveAiStopsAutomaticRangeTargeting` 失败 |

- 测试总数 `298 → 297`：删除旧版不存在的 `progressionAdvancementsLoadAndFriendlyShipTriggerAwards`。
- `passiveAiStopsAutomaticRangeTargeting` 在未改动的基线上同样失败，与本批改动无关，不在本模块范围内修复。
- 本批修改的测试 `chestLootInjectorUsesLegacyConfigLootDefaults`、`worldgenResourcesKeepLegacyPolymetalPlacement` 及全部 crafting/hostile 相关测试均通过。
- `git diff --check` 无输出；`src/main/` 零改动。

<a id="integration-merge"></a>

## integration merge

- 分支 `devin/parity-fix-integration`，保留此前船核/战斗/移动/舰种、客户端/菜单、物品方块链；本次只创建本地 merge commit，不 push。
- D-012～D-016 保留；本批改用 D-017，同步 CHANGELOG、本批 decision 与 WORLDGEN-AUDIT 引用。
- 船厂/VolCore 保留各一个字段、访问器与旧版键 `Tile_SmallShipyard`、`Tile_LargeShipyard`、`Tile_VolCore`，默认分别为 [460800,48,1]、[1382400,48,1]、[9600,16,240]；旧列表没有额外数值范围。保留 crane/tank 配置并使用旧名 `Tile_Crane`、`Item_FluidDrum`；液体运输和经验键为 `Drum_Liquid`、`Exp_Gain`。
- `DropRate_Grudge` 默认 1、范围 0..64；`Despawn_Egg` 默认 12000、范围 -1..1728000；`Polymetal_Gravel` 默认 4、范围 0..100。无标签敌舰蛋按 BasicEntityItem 实际的严格大于条件衔接现代 lifespan；-1 注释与实际代码矛盾按源码处理，见本批 decision。
- 熔岩仍按 TileEntityHelper 每次 40 mB / 800 × fuel magnification，不恢复 incoming 的整桶消耗/封顶逻辑；固体燃料需加后严格小于容量，小船厂补充恰满拒绝断言。大建 canBuild 仍按旧版 powerGoal > 0 且输出空；结构与燃料在外层检查，不重加到 canBuild。
- easyMode 放出统一到 LargeShipyardRecipes 的 90/10 入口，投料 ×10；保留 equipment/egg 回收。
- 删除所有十个 progression JSON（含 modify/delete 的 ocean_boss_encounter）、两个触发器类、注册/部署调用、语言键及对应测试。对 src/modern 执行 `rg 'ModCriteriaTriggers|FriendlyShipDeployedTrigger|friendly_ship_deployed|progression/|advancement\\.shincolle'` 无输出；ship-variants decision 中的旧进度保留声明已撤销。
- 第一轮失败：敌舰旧测试只允许蛋，但新增的全局 onDrop 还按旧版产生怨念；雷电配对选中非预期舰。按要求未改动先重跑一次，仍失败且起重机也选中不同 ID/UUID（expected 786/b8adf200-d44c-443d-ad44-e232a1a31a7c，actual 783/429b8779-ad2e-4b34-89f0-4764b410536f，同位置）。
- 修正掉落断言为恰好一枚基础蛋和配置的怨念，仍排除库存苹果与额外 Boss 奖励；雷电合体改 guard 内侧独立 batch，四项起重机在放入自己的舰前清理扫描区遗留舰。未定位干扰舰最初生成点；未为通过测试改生产扫描逻辑。修复夹具时另补一处 AABB import 编译错误。
- 新增配置键/正常、空、无效配置测试与敌舰蛋寿命默认/0/-1 测试。总数 327 - 1（进度）+ 2 = 328。
- 独立 `bash ./gradlew compileJava --no-daemon`：

```text
BUILD SUCCESSFUL in 1m 28s
1 actionable task: 1 executed
```

- 最后代码修改后 `bash ./gradlew compileJava runGameTestServer --no-daemon`：

```text
========= 328 GAME TESTS COMPLETE ======================
All 328 required tests passed :)
BUILD SUCCESSFUL in 3m 30s
11 actionable tasks: 4 executed, 7 up-to-date
```

- 完整日志：`/home/ubuntu/shincolle-crafting-compile.log`、`/home/ubuntu/shincolle-crafting-final.log`。语言 JSON 可解析、无重复键；无冲突标记，`git diff --check HEAD` 无输出；`src/main/**` 未修改。
- 未做客户端视觉验证。
