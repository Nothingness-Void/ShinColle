# V0 客户端人工测试用例表

本表用于单人客户端烟测。它只记录测试步骤、预期现象和你的结果，不预填任何“通过”。行为语义以 1.12 代码为准，现代版只做 API 适配。

## 记录规则

| 标记 | 含义 |
| --- | --- |
| `P` | 通过，现象与预期一致 |
| `F` | 失败，功能有问题但客户端仍可继续运行 |
| `B` | 阻塞，崩溃、无法进入世界、无法继续操作或环境无法完成 |
| `NA` | 本环境无法触发，需附原因 |
| `待测` | 尚未测试 |

每次失败都记录：用例编号、舰船蛋 ID、世界坐标、操作顺序、截图或日志关键行。截图用 `F2`，需要看碰撞箱时可按 `F3+B`。不要手改 NBT；不要把多个舰船族群长期堆在同一地点。

## 0. 测试准备

建议使用新建或备份的单人测试世界。先执行：

```text
/gamemode creative
/time set noon
/weather clear
/gamerule doMobLoot true
/gamerule keepInventory true
/give @s shincolle:pointeritem 1
```

清理当前测试区内的现代舰船实体时可执行：

```text
/kill @e[type=shincolle:legacy_ship,distance=..64]
```

常用物品：

```text
/give @s shincolle:targetwrench 1
/give @s shincolle:equipcannon 1
/give @s shincolle:equipairplane 1
/give @s shincolle:equiptorpedo 1
/give @s shincolle:bucketrepair 1
/give @s shincolle:trainingbook 1
/give @s shincolle:modernkit 1
/give @s shincolle:combatration 1
/give @s shincolle:grudge 1
/give @s shincolle:ammo 1
/give @s shincolle:ammo2 1
```

若某物品没有对应命令或不适合当前舰船，从创造栏取同名物品，并在回报中写明。

## 1. 通用逐舰步骤

对下面的 45 个基类逐行执行。友方行测试友方蛋；有敌对蛋的行再测试敌对蛋。每次先单独生成一艘，确认名称、模型、纹理、大小和碰撞箱，再进入行为测试。

1. 执行 `/give @s shincolle:shipeggN 1`，用蛋在安全地面或适合的水面生成，确认只生成一艘。
2. 友方舰船用 Pointer 右键，确认归属为当前玩家；按 `Shift` 右键打开 ShipInventory，记录状态、生命、等级、攻击、速度、射程、士气和补给读数。
3. 测试该行的专属行为；普通移动、待机、攻击和停止统一放在 V0-04 测试。
4. 有敌对蛋的行切换生存模式，在旧版攻击距离内外各测一次；创造模式只用于确认不会持续锁定玩家。
5. 测完执行清理，填写结果和证据。不要因为“能生成”就把行为项标成通过。

## 2. 45 个舰船基类

`友方蛋`、`敌对蛋`填写的是 `shipegg` 后的数字；`-` 表示没有该阵营变体。敌对 Mob 变体在“敌对蛋”列标为 `Mob`。

| # | 1.12 类 / 现代类 | 友方蛋 | 敌对蛋 | 本行重点 | 结果 / 证据 |
| ---: | --- | ---: | ---: | --- | --- |
| 1 | `EntityDestroyerI` | `2` | `-` | 速度、轻攻击、拾取、移动与攻击 | 待测 |
| 2 | `EntityDestroyerRo` | `3` | `-` | 急迫效果、轻攻击、拾取、移动与攻击 | 待测 |
| 3 | `EntityDestroyerHa` | `4` | `-` | 跳跃、轻攻击、拾取、移动与攻击 | 待测 |
| 4 | `EntityDestroyerNi` | `5` | `-` | 台阶高度、力量、轻攻击、拾取与移动 | 待测 |
| 5 | `EntityCARi` / `EntityHeavyCruiserRi` | `11` | `-` | 夜间被动、轻重攻击、骑乘 | 待测 |
| 6 | `EntityHeavyCruiserNe` | `12` | `-` | 射程、路线、护卫、目标有效性 | 待测 |
| 7 | `EntityCarrierWo` | `14` | `-` | 飞机容量、起飞、回收；不要套用未定义的通用轻重击 | 待测 |
| 8 | `EntityBattleshipRu` | `15` | `-` | 重炮齐射、连装炮、目标选择 | 待测 |
| 9 | `EntityBattleshipTa` | `16` | `-` | 旧版轻型连装炮路径；确认没有错误重击路径 | 待测 |
| 10 | `EntityBattleshipRe` | `17` | `-` | 激光、轻攻击、击退 | 待测 |
| 11 | `EntityTransportWa` | `18` | `-` | 货物、补给、自动补给、待机 | 待测 |
| 12 | `EntitySubmKa` | `19` | `-` | 潜艇路径、隐身、拾取 | 待测 |
| 13 | `EntitySubmYo` | `20` | `-` | 潜艇路径、隐身、轻型导弹 | 待测 |
| 14 | `EntitySubmSo` | `21` | `-` | 潜艇路径、隐身与目标切换 | 待测 |
| 15 | `EntityCarrierHime` | `-` | `22` | 近战导弹、航空攻击、光环 | 待测 |
| 16 | `EntityAirfieldHime` | `-` | `23` | 空袭、起飞高度、治疗 | 待测 |
| 17 | `EntityBattleshipHime` | `-` | `28` | 集群重击、挂载、抗性光环 | 待测 |
| 18 | `EntityDestroyerHime` | `-` | `29` | 失明效果、状态效果、模型状态 | 待测 |
| 19 | `EntityHarbourHime` | `-` | `30` | 轨道炮、治疗、起飞高度 | 待测 |
| 20 | `EntityIsolatedHime` | `-` | `31` | 黑洞、双重效果、模型状态 | 待测 |
| 21 | `EntityMidwayHime` | `-` | `32` | 航空高度、治疗、吸收 | 待测 |
| 22 | `EntityNorthernHime` | `-` | `33` | 浮游炮台、挂载与下坐骑 | 待测 |
| 23 | `EntityCarrierWD` / `EntityCarrierWDemon` | `35` | `-` | 双航空队、容量、起飞与回收 | 待测 |
| 24 | `EntityDestroyerShimakaze` | `38` | `2038` Mob | 五连射、使魔、模型状态 | 待测 |
| 25 | `EntityBattleshipNGT` / `EntityBattleshipNagato` | `39` | `2039` Mob | 穿甲攻击、士气、Boss 颜色 | 待测 |
| 26 | `EntitySubmU511` | `40` | `2040` Mob | 速度、隐身、敌对缩放、模型 | 待测 |
| 27 | `EntitySubmRo500` | `41` | `2041` Mob | Garuru、隐身、敌对缩放 | 待测 |
| 28 | `EntitySubmHime` | `-` | `46` | 双导弹、散射、潜水状态 | 待测 |
| 29 | `EntityBattleshipYMT` / `EntityBattleshipYamato` | `48` | `2048` Mob | 两阶段光束、敌对炮击 | 待测 |
| 30 | `EntityCarrierKaga` | `49` | `2049` Mob | 飞机库存、零战、容量 | 待测 |
| 31 | `EntityCarrierAkagi` | `50` | `2050` Mob | 飞机、语音、容量 | 待测 |
| 32 | `EntityCAHime` | `-` | `51` | 夜战暴击、三连击、击退 | 待测 |
| 33 | `EntityDestroyerAkatsuki` | `53` | `2053` Mob | 骑乘、燃料、攻击 | 待测 |
| 34 | `EntityDestroyerHibiki` | `54` | `2054` Mob | 骑乘、视线高度、燃料 | 待测 |
| 35 | `EntityDestroyerIkazuchi` | `55` | `2055` Mob | 骑乘、Raiden、燃料 | 待测 |
| 36 | `EntityDestroyerInazuma` | `56` | `2056` Mob | Raiden、攻击、资源 | 待测 |
| 37 | `EntityCLTenryuu` | `58` | `2058` Mob | 冲刺、技能阶段、夜间效果 | 待测 |
| 38 | `EntityCLTatsuta` | `59` | `2059` Mob | 控制波、技能阶段、夜间效果 | 待测 |
| 39 | `EntityCAAtago` | `60` | `2060` Mob | 五型导弹、夜间重置、反应 | 待测 |
| 40 | `EntityCATakao` | `61` | `2061` Mob | 五型导弹、战斗反应 | 待测 |
| 41 | `EntityBBKongou` | `62` | `2062` Mob | 连携、火炮、生命光环 | 待测 |
| 42 | `EntityBBHiei` | `63` | `2063` Mob | 连携、饱和光环 | 待测 |
| 43 | `EntityBBHaruna` | `64` | `2064` Mob | 连携、幸运光环 | 待测 |
| 44 | `EntityBBKirishima` | `65` | `2065` Mob | 连携、近战、夜视 | 待测 |
| 45 | `EntitySSNH` / `EntitySubmHimeNew` | `-` | `74` | 双导弹、深度、士气、骑乘 | 待测 |

敌对 Mob 变体完整 ID：`2038, 2039, 2040, 2041, 2048, 2049, 2050, 2053, 2054, 2055, 2056, 2058, 2059, 2060, 2061, 2062, 2063, 2064, 2065`。例如 `/give @s shincolle:shipegg2038 1`。

## 3. V0 主线工作流

这些用例验证跨舰种的单人闭环。按顺序执行；结果栏留给测试记录。

| ID | 操作 | 预期 | 结果 / 证据 |
| --- | --- | --- | --- |
| V0-01 | 启动 `runClient`，进入或新建单人世界 | 客户端无崩溃；世界、纹理图集、声音正常加载；日志无 ShinColle crash、missing texture 或 model fallback | 待测 |
| V0-02 | 生成友方 `shipegg53` 和敌对 `shipegg22` | 两者均只生成一艘；名称、阵营、模型、纹理、大小和碰撞箱正确；友方归属当前玩家 | 待测 |
| V0-03 | 手持 Pointer：`Shift` 左键空气循环 Single、Group、Formation；按 `Tab` 切换 caress | actionbar 显示对应模式；caress 右键己方舰船提高士气；无异常重复消息 | 待测 |
| V0-04 | Pointer 普通右键己方舰船切换跟随/待机；冲刺右键方块发移动；普通右键方块发护卫；冲刺右键生物护卫；普通右键敌人攻击；按 ShipInventory 的 Stop | 舰船按命令行动；Stop 清除当前命令并停止；无效目标不会保留错误目标 | 待测 |
| V0-05 | 在 Group 模式右键己方舰船；Formation 模式冲刺左键循环阵型；用 `Shift` 右键空气或 ShipInventory 的 Formation 打开阵型页 | 群组跟随/待机正确；可见 No Formation、Line Ahead、Double Line、Diamond、Echelon、Line Abreast；选择、清除、上下交换、改名有效 | 待测 |
| V0-06 | `Shift` 右键己方舰船打开 ShipInventory；放入兼容的炮、鱼雷或飞机 | 归属、模式、角色、生命、等级、攻击、速度、射程、士气、补给、设备和货物读数正确；装备槽不接受不兼容物品 | 待测 |
| V0-07 | 在 ShipInventory 测试 `T/Y/U/I/P/O/L/V`，`J/K`，`[`/`]`；普通点击与 Shift 点击行为区 | AutoTarget、PVP、AutoSupply、RouteStay、PickItem、Aura、TimeKeeper、AutoPump、跟随范围、路径停留和 AutoCR 阈值能改变；停止/坐下优先于自动行为 | 待测 |
| V0-08 | 放置路径点和箱子，使用 Pointer 的 Shift 右键关联；设置路线并打开 RouteStay | 路线节点可创建、切换和停留；舰船按路线行动；箱子关联不误删物品；重新打开界面状态仍在 | 待测 |
| V0-09 | 用敌对舰船造成少量伤害，再用 `bucketrepair`、`combatration`、`grudge`、`ammo`、`ammo2` 右键己方舰船；观察 ShipInventory 补给读数 | 修理、士气、grudge、弹药或补给按旧版语义变化；资源不足显示失败反馈而非静默成功；自动补给能按开关工作 | 待测 |
| V0-10 | 在创造模式让敌对 Boss 看到并接近玩家；再切生存模式，在旧版范围内外各测一次 | 创造、无敌或旁观目标不会被持续锁定和追击；生存模式只在旧版有效范围内寻敌，离开范围后目标清除。重点观察大型 Boss 的 `18/22` 距离边界 | 待测 |
| V0-11 | 生成敌对航母 `shipegg22` 或 Mob 航母 `shipegg2049`，等待飞机起飞并尝试命中 | 飞机、命中特效可以显示；玩家 actionbar 不出现敌对 `LAUNCH` 或 `HIT` 提示；客户端无刷屏或崩溃 | 待测 |
| V0-12 | 给 `targetwrench`，左键己方舰船解锁 Morph；按 `G` 打开；测试 `Shift+G`、`Z`、`X`、`C` | Morph 页面可切换配置、Toggle active、Aura、Show Held，并显示资源；骑乘/挂载可切换；技能只在有效 Morph 或骑乘状态可用 | 待测 |
| V0-13 | 用驱逐舰测试轻攻击，用 `shipegg20` 测鱼雷，用 `shipegg48` 测 beam，用 `shipegg49` 装飞机测航空，用 Boss/特殊舰测试 bomb、aircraft、static/guided projectile | 各投射物族生成、移动、命中或消失正确；不出现紫黑贴图、错模型、穿模或永久残留；命中目标与伤害类型符合旧版 | 待测 |
| V0-14 | 反复攻击同一目标，观察命中、miss、crit 和冷却；卸空弹药、燃料或 grudge 后再次施放；指定已死亡或无效目标 | hit/miss/crit、技能失败、冷却、目标无效、弹药不足和补给不足都有正确反馈；heavy 或 special 的 miss 不应变成命中 | 待测 |
| V0-15 | 记录舰船归属、队伍、阵型、路线、装备、货物、生命、燃料、弹药、grudge、士气和冷却；退出世界并重新进入 | 状态保存并恢复；缺省或旧字段不会导致崩溃；正在执行的命令和 ShipInventory 读数符合旧版预期 | 待测 |
| V0-16 | 使用实际回收流程获得 recovered egg，重新放置并检查 | 回收蛋可再次生成；所有权、舰船身份、队伍或必要状态按旧版流程保留；若本环境无法触发回收流程标 `NA` 并写原因 | 待测 |
| V0-17 | 对 45 行逐舰表做最终扫测，覆盖友方、敌对、Boss、飞机、投射物、GUI、粒子和声音 | 可到达内容无崩溃、missing texture、model fallback、错误音效或 GUI 重叠；每个失败可定位到舰船蛋 ID 和坐标 | 待测 |

## 4. Pointer 快速对照

| 操作 | 键鼠 |
| --- | --- |
| 切换 Single、Group、Formation | 手持 Pointer，`Shift` 左键空气 |
| 切换 caress | `Tab` |
| 己方舰船跟随/待机 | Pointer 普通右键己方舰船 |
| 群组跟随/待机 | Group 模式普通右键己方舰船 |
| 移动到方块 | 冲刺右键方块 |
| 护卫方块 | 普通右键方块 |
| 护卫生物 | 冲刺右键生物 |
| 攻击敌对舰船 | 普通右键敌对舰船 |
| 打开 ShipInventory | `Shift` 右键己方舰船，且不处于冲刺 |
| 清空当前队伍 | `Shift` 加冲刺左键己方舰船 |
| 循环阵型 | Formation 模式冲刺左键 |
| 打开 Formation | `Shift` 右键空气或方块 |

## 5. ShipInventory 快速对照

| 操作 | 键鼠 |
| --- | --- |
| Escort/Standby | 点击模式按钮 |
| 切换队伍 | 点击 Team |
| 打开阵型 | 点击 Formation |
| 停止 | 点击 Stop |
| AutoTarget、PVP、AutoSupply、RouteStay、PickItem | `T`、`Y`、`U`、`I`、`P` |
| Aura / marriage effect | `O` |
| 跟随距离 | `J` 减少，`K` 增加，每次 2 |
| 路径停留 | `[` 减少，`]` 增加 |
| TimeKeeper、AutoPump | `L`、`V` |
| AutoCR 阈值 | Shift 点击行为区调整 |
| waypoint stay | 普通点击行为区调整 |

## 6. Morph 与资源

Morph 流程必须先用 `targetwrench` 左键己方舰船解锁，再按 `G` 打开 Morph 页面。默认技能键为：`Z` 玩家技能 1，`X` 玩家技能 2，`C` 玩家特殊技能或技能 5；`Shift+G` 切换挂载。Overlay 显示时可用数字键 `1-5` 选择技能槽。

装备建议：驱逐舰放 `equiptorpedo`，战舰放 `equipcannon`，航母放 `equipairplane`。物品不接受、读数不变或反馈缺失时，记录具体舰船、物品 ID 和操作前后数量。

## 7. 回报模板

直接按下面格式回复即可，不需要一次写完整报告：

```text
V0-03: P
V0-10: F，shipegg22，坐标 x y z；创造模式仍追击；截图：...
SHIP-24 friendly shipegg38: P
SHIP-24 hostile shipegg2038: F，起飞正常但 actionbar 出现 LAUNCH
V0-15: B，退出重进后世界崩溃；日志关键行：...
```

先回报 `V0-01` 至 `V0-17`，再按 45 行舰船表逐项回报。这样可以先定位跨舰种问题，再判断某一舰种的专属行为。
