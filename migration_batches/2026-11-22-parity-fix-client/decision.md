# 客户端还原结论

## 真源与实施边界

以完整 `0aad501` 为行为、数值和分支真源；当前 `src/main` 只读。
尤其用完整旧树恢复吊车生命周期、模型/情绪/持物、HUD/粒子和各 GUI；
雷达纠错同时核对当前 `src/main` 与 `0aad501` 的 `GuiDesk.mouseClicked`，两者都没有地图点打开。
跨批次的撤销规则只见 [D-013](../../MIGRATION-DECISIONS.md#d-013客户端行为严格还原撤销历史简化等价声明)。

## 19 个问题逐项结论

| 编号 | 问题 | 结论、真源及边界 |
| --- | --- | --- |
| 01 | 航点交互 | 按 `BlockWaypoint` 恢复严格 owner UID 相等、潜行、空手及客户端分支；删除自创航点终端。测试玩家需先初始化旧 PlayerUID，未放宽所有权。 |
| 02 | 吊车 | 对照 `TileEntityCrane#setShipData` 的关联/清除及 `BasicEntityShip`：CraneState>1 每 tick 累加 CraneTime，完成清零；菜单同步实体 ID 和时间。不改物品传输、不用倒计时反推；删除旧版没有的灯、着色、所有者/坐标/能量文字、感叹号及提示，旧版注释掉的电源绘制/提示不显示。 |
| 03 | 模型、死亡姿态、发光 | 以 `RenderShipEntity` 的 MD_* 清单机械移植 45 舰船模型及基础/情绪类；1.12 薄兼容层保持 Z→Y→X、UV、mirror、亮度及子部件变换；逐模型死亡/攻击姿态与 Glow 跟随。删除通用 `LegacyShipModel`、舰船专用 Transform/State 目录；其他渲染器仍用的解析器保留。未做全舰种视觉认证。 |
| 04 | 脸红 | 旧模型按 `Morale > ID.Morale.L_Happy`（3900）判定，不再按 emotion==2。 |
| 05 | 表情滚动 | 移植 `EmotionHelper.rollEmotion/rollEmotionAdv`、眨眼、抚摸、表情计时及攻击 tick；本地瞬态状态留在实体非同步字段，攻击粒子设置并递减旧计时。 |
| 06 | 手持物品层 | `LayerShipHeldItem` 注册到旧模型渲染路径；主副手、方块/Full3D/普通物品缩放、逐模型 postRender 手臂链、潜行偏移及左右手变换沿用旧版，最终调用现代 ItemInHandRenderer。 |
| 07 | 载具技能 HUD | 按 `RenderHelper:152–284`，Position_HUD 默认 {0.5,0.6}；18px 槽、21px 间距、原 UV、冷却文字/遮罩及航空机数量；删除自创标题/资源/模式/提示布局。仅档案、无舰实体时航空机数仍为 0。 |
| 08 | 表情粒子 | `ParticleEmotion` 全 case、帧、寿命、位置/运动/透明度移入 `LegacyEmotionParticle`，不再映射原版 HEART/NOTE/SMOKE；原贴图字节副本。未视觉验证。 |
| 09 | 文字粒子 | `ParticleTexts` 五类 MISS/CRI/DHIT/THIT/DODGE 的旧贴图/UV、25 tick、运动移入 `LegacyTextParticle`，替换烟雾近似；不含尚未移植的 ParticleTextsCustom。 |
| 10 | 编队：阵型/分队/等待 | `GuiFormation` 六阵型、九分队旧热区；切队等 60 GUI tick，重排等 40，等待禁止重排并显示旧剩余秒数。 |
| 11 | 编队：舰船行 | 首次只改查看行，再点同一行打开背包，无 250ms 窗口、无选舰切换；删除独立打开/清队按钮。CLEAR_CURRENT_TEAM 仍被指挥棒 sprint+shift 使用，保留服务器处理。 |
| 12 | 编队：示意/加成/士气 | 机械适配 `GuiFormation:533–571,752–828` 的阵位点、选舰、条和士气；同步实际属性、阵位、最低移动与 36 组预览，不虚构客户端公式。 |
| 13 | 舰船背包：货舱 | `GuiShipInventory` 三页旧热区及 >0/>1 门槛；按 PacketHelper 分派 INV_PAGE 并刷新槽，保留 60 槽底层；容量取实体已有来源，不按舰种造规则。 |
| 14 | 舰船背包：控制 | 十二页旧按钮/滑块，独立 FollowMin/Max、撤退血量、模型位、工船输入/输出/燃料面；旧第 9–12 页为空预留，未造动作；移除 J/K 等自创快捷键。 |
| 15 | 舰船背包：编队悬停 | `GuiShipInventory:994–1057` 实际阵位及攻击/移动/命中/闪避/防御修正；菜单同步服务器来源。目标背包未成功实机打开，渲染/热区/悬停未视觉验证。 |
| 16 | 提督桌 | `GuiDesk/GuiHelper/GuiHandler` 统一雷达/图鉴/队伍/目标四页，同页再点关闭当前页；删除潜行循环模式和自创分类终端；便携雷达/图鉴遵照原物品入口。 |
| 17 | 图鉴 | `GuiBook/Values` 七章，上限 1/28/6/20/26/19/4，从 0 起，左右键 ±1/±10 同章钳位；旧图文/模型收藏门槛，合法 class 0 保留；删除九页英文摘要。 |
| 18 | 雷达 | 旧列表/地图/悬停；只有重复点击同一列表行打开背包，无时间窗。曾加入的地图点选择/打开已经删除，生成脚本、测试和说明同步纠正。 |
| 19 | 变身背包 | `GuiMorphInventory` 旧 256×214、三页、鼠标跟随模型、玩家当前/最大 HP、NoFuel/Sit 等旧按钮；缺失 GUI 状态在档案保存/同步；删除计数、前后切换和激活开关及专属处理。实际激活缺口见下文。 |

## 变身激活入口核查

结论：本仓库现代生产实现中，删除自创开关后，没有可达的“从未激活变为激活”的游戏入口。
没有恢复开关，也没有新增自动激活、命令或物品。

证据链与反证检查：
1. `MorphRuntimeState.active` 默认 false；写 true 的通用方法只有 `setActive(boolean)`，以及从存档 `Active` 读取；旧存档可恢复 active，不是新的激活入口。
2. 对全部现代源码定向搜索 `setMorphActive`、`setActive` 及字段赋值，排除 GameTest 后，`TeitokuData.setMorphActive` 只有声明、没有生产调用；其它调用是 false 清除。测试主动设 true 不能算玩家入口。
3. `MorphEvents.onHostileShipKilled → MorphHelper.unlockMorph` 只解锁/选择；`TargetWrenchItem.onLeftClickEntity` 也只解锁/选择。`setSelectedMorphProfile` 不激活，某些无效选择反而清除 active。
4. `MorphClientEvents` 的 G 只发 OPEN_MORPH_SCREEN；Shift+G 是载具模式切换，依赖已有 active，不能激活档案。网络枚举/处理不再有 MORPH_TOGGLE_ACTIVE；命令注册代码未发现变身激活路由。
5. 专门反查最可能的外部入口：`MorphCompatBridgeLoader.load` 即使检测到 metamorph 也只创建 SoftDependencyBridge；其 syncActiveMorph/reset 为空、继承 isAvailable=false，能力描述 active_bridge=false。NOOP 同样为空，未实现外部状态回读或激活。
6. `0aad501/intermod/MetamorphHelper` 调用外部 `MorphManager`、`MorphAPI.acquire/demorph`，变身选择/激活由外部 Metamorph 提供。这解释了 D-011 背景，但不能证明现代空桥等价。

核查范围是仓库内已注册生产调用链；不把任意外部插件直接调用公共 setter、管理员离线改 NBT 算内建入口。
保留现有档案、显式选择、存档恢复和已激活状态的技能/坐骑读取，避免扩大删除影响。外部适配实现仍是已知限制，不擅自设计替代交互。

## 未完成与视觉边界

- 剩余 17 类粒子的原版近似/缺失分支不等价，名称和重载逐条见 [粒子审计](PARTICLE_PARITY_AUDIT.md)；ParticleTeam 全模式亦未认证。
- MorphProfile 没有历史击杀数/航空机库存时展示 0，不编造历史或库存生命周期。
- 航点、吊车、持物、HUD、表情/文字粒子和全舰种动画/死亡/发光未完成针对性视觉验收；映射或编译通过不替代画面证据。
- 编队仅空队、第九分队、预览和等待被实机检查，满编舰名/士气、实际打开背包未验；截图 `/home/ubuntu/screenshots/ss_4a368cb0.png`。
- 桌面仅便携图鉴、统一图鉴、当前页关闭及空目标页；满列表、雷达开包、收藏舰模型和细动画未验。截图见 [桌面协议](DESK_PARITY_PROTOCOL.md)。
- 变身背包只实机检查爱宕的鼠标旋转、HP、三页、NoFuel/Sit 勾选与姿态；其它舰种及外部世界变身未验。截图见 [变身协议](MORPH_INVENTORY_PARITY_PROTOCOL.md)。
- 中途曾出现 `autocombatrationuseslegacytickandfoodrules` 与 `eighthfiveshiphookskeeplegacyhostiledestroyerparity` 瞬时失败，后续通过；根因均未确认，不能用布局变化或后续通过反推已修复根因。
- 航点失败通过初始化 PlayerUID 修正测试设置，不放宽权限；Ro500 断言按实际发射/目标的旧总距离/水平距离阈值计算条件期望，不改导弹算法。
