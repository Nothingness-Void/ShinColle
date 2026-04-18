# ShinColle AI Handoff

更新时间：2026-04-18

先读：
- `PORTING-MISTAKES.md`
- `PORTING-1.20.1.md`
- `MIGRATION-FLOW-CHECKLIST.md`

## 当前基线

- 实际参与构建的是 `src/modern/java` 和 `src/modern/resources`。
- `src/main/java` 继续作为 legacy 参考实现，不是当前构建主线。
- 已验证 JDK 17 构建与 GameTest 基线：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.3.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat compileJava processResources runGameTestServer
```

- 结果：
  - `BUILD SUCCESSFUL`
  - `All 68 required tests passed`
- 用户已对上一批客户端内容做过一轮手工验收，未发现明显问题；本轮 Phase 5-6 主要是服务端/网络/持久化闭环，typed ship command 的客户端输入路径仍建议后续再做一次短冒烟。

## 已经落地并接线的主线

- `Teitoku + playerUID + owner UID`
- `TeamSavedData / formation runtime / target-class sync`
- `gameplay command/state packets`
- `dedicated ship command packet + shared command service`
- `per-ship behavior catalog baseline`
- `desk / formation / ship inventory` 控制闭环
- `large shipyard / heavy grudge` 结构与能量基线
- `hostile encounter / chest loot / world combat rules`
- `growth loop` 生存链：关键配方、宝箱入口、boss 首舰门槛
- `advancement` 新手链：从 polymetal / grudge 一直到 first ship 与 boss unlock
- `projectile parity`：heavy / air attack 的投射物语义、视觉、粒子、reaction
- `GameplayParityGameTests` 已覆盖 team、morph、shipyard、growth loop、advancement、projectile、playerskill runtime 等关键链路

## 这一批新增确认

- Phase 1-4 收口已完成到 code + GameTest 基线：
  - Phase 1：注册资源覆盖测试已锁住 registered blockstate、block model 引用、item model、菜单 GUI texture、`sounds.json` 引用的 packaged OGG；运行时 `ResourceLocation` path 继续要求小写。
  - Phase 2：`LegacyShipEntity` 统一按友方 `SoundSource.NEUTRAL`、敌方 `SoundSource.HOSTILE` 发声；轻炮、重炮、舰载机、近战命中接到现有 `ModSoundEvents`，idle/hurt/death 继续走 `ShipSoundType` voice bridge。
  - Phase 3：crane 不再有独立 tick 抽取/暂存物品的 destructive stub；路线装卸由舰船抵达 crane route node 时执行。Waypoint stay 改由舰船自身 route wait 状态处理，避免多个舰船共享 BE 倒计时。
  - Phase 3：Large Shipyard 已有完整结构识别、燃料/能量池、材料消耗、large ship egg 输出闭环测试；servant proxy 仍由既有 polymetal/heavy grudge GameTest 覆盖。
  - Phase 4：Crane、Waypoint、Large Shipyard、LegacyCore、ShipInventory 的服务端按钮和菜单读数已有 GameTest 覆盖。
- Phase 5-6 单人指挥闭环已完成到 code + GameTest 基线：
  - 新增 `ServerboundShipCommandPacket` 与 `ShipCommandAction`，覆盖 move、guard、attack、stop、AI flags、follow range、sit toggle、open inventory。
  - `GameplayCommandHandler` 的旧 ship command 入口统一委托到 `ShipCommandService`，避免 gameplay packet 与专用 ship packet 分叉。
  - `PointerItem` 的舰船移动、护卫、攻击、停止、待机切换、打开背包改走专用 ship command packet；Desk / Formation / Morph / target-class 仍走既有 gameplay command 总线。
  - `ShipInventoryScreen` 的 stop、AI flag、follow range 控制改走专用 ship command packet，team/formation 按钮继续沿用现有总线。
  - `ShipCommandService` 集中处理 owner/UID 解析、存活检查、距离限制、`canEngage`、team slot、formation offset、standby 解除、目标清理与状态同步。
  - `LegacyShipEntity` 的 command pos/dimension、guard UUID、route wait、AI flags、follow range 等运行时状态已有 NBT 回归；修正了读档时 `DATA_AI_FLAGS` 与 `DATA_AI_FOLLOW_RANGE` 同步回调互相覆盖的问题。
  - `ShipCacheSavedData / ShipWorldCacheEntry` 现已显式区分 `online / offline / dead`：活体缓存写入 `online=true`，死亡快照写入 `dead=true, online=false`，实体卸载写入 `dead=false, online=false`；对应 save/load 与脱实体 UI fallback 已有 GameTest 覆盖。
  - `buildGameplayStateTag -> applyClientState` 这条 gameplay state 回显链已有 GameTest 覆盖：team world data、world unattackable class、offline ship cache、target-class 与 `PlayerSkillRuntimeState` 都能一起下发到 client mirror。
- Phase 7 第一切片已完成到 code + GameTest 基线：
  - 新增 `LegacyShipBehaviorCatalog`，作为 legacy class id / hostile flag / runtime state 的舰种行为分发入口。
  - `LegacyShipEntity` 的攻击 profile 初始化现在经由 behavior catalog，后续 per-ship combat hook 不再继续堆在实体主类里。
  - 已婚友方舰的 legacy ring passive 分发已移入 behavior catalog，现有 U511/Ro500 隐身、Kaga/Akagi jump aura、第六驱逐队 owner buff 语义保持不变。
  - 新增 GameTest 覆盖婚戒被动 class-id dispatch 与攻击 profile catalog routing。
- Phase 7 第二切片已完成到 code + GameTest 基线：
  - `HostileEncounterSpawner.spawnEncounterAt` 现在返回实际生成的 hostile entity；boss cooldown 只会在生成成功后消耗，避免碰撞失败仍进入冷却。
  - hostile boss 生成路径已有 GameTest 覆盖，确认 boss runtime、hostile sound source、attack profile 初始化正确。
  - 舰船 move/route 与 guard 命令现在会清掉旧 combat target，避免攻击目标和路线/护卫状态互相拖住；对应边界已有 GameTest 覆盖。
- Phase 7 第三切片已完成到 code + GameTest 基线：
  - `LegacyShipBehaviorCatalog` 现在集中给 hostile / elite / boss spawn 提供等级与士气基线。
  - `LegacyShipEntity.initializeHostileRuntime(...)` 会按 catalog 拉起敌舰等级/士气并刷新满血，避免 boss 和 elite 只改变行为/掉落、不改变单人战斗强度。
  - hostile boss spawn GameTest 已扩展检查等级、士气、满血初始化。
- 新增 `PLAYER_CAST_SKILL`，继续复用现有 gameplay command 总线，没有再开第二套协议。
- `TeitokuData` 现在同步 `PlayerSkillRuntimeState`：
  - visible
  - host mode
  - 5 槽 enabled mask
  - 5 槽 cooldown / max cooldown snapshot
  - host ship uid / class id
- 客户端玩家技能输入恢复为旧热栏思路：
  - `1~5` 为主技能槽
  - `Z / X / C` 保留为快捷键
  - `G` 继续开 morph inventory
  - `Shift + G` 继续切换 morph mount
- HUD 已从旧的文字行改成 5 槽技能条。
- 宿主解析已经统一：
  - `rider host`：玩家骑乘自家 ship，直接走 ship 自己的攻击和 cooldown
  - `morph host`
  - `mount host`
- morph slot 语义：
  - `1=light`
  - `2=heavy`
  - `3=air light`
  - `4=air heavy`
  - `5=special；没有 special 时回退 melee`
- rider slot 语义：
  - `1=light`
  - `2=heavy`
  - `3=air light`
  - `4=air heavy`
  - `5=disabled`
- morph special 由包内 `MorphSpecialCatalog` 统一分发，分发表使用 legacy class id，不使用 eggMeta：
  - `36 / 2036` Shimakaze，五连装鱼雷风格多段 heavy strike
  - `37 / 2037` Nagato，Type 91 AP fist 风格主目标重击加小范围 AoE
  - `46 / 2046` Yamato，beam 风格线性重击
  - `56 / 2056` Tenryuu
  - `57 / 2057` Tatsuta
  - `58 / 2058 / 59 / 2059` Atago / Takao heavy cruiser group
  - `60 / 2060 / 61 / 2061 / 62 / 2062 / 63 / 2063` Kongou-class group
- morph special cooldown preview 读取行为 catalog 自身的 cooldown，不再维护第二份 class-id switch。
- 已复核本轮容易误判的 legacy 类：
  - Kaga / Akagi 是舰载机普通攻击与婚戒跳跃 buff，没有发现可迁移的 slot-5 special。
  - U511 / Ro500 是潜艇普通轻攻击重写与隐身/婚戒效果，没有发现可迁移的 slot-5 special。
  - Akatsuki / Hibiki / Ikazuchi / Inazuma 是第六驱逐队合体、骑乘、婚戒 buff 逻辑，不应直接登记为 morph special。
- 第一批非 special 的 legacy ring/marriage passive 已迁移到现代 `LegacyShipEntity`：
  - U511 / Ro500：已婚友方每 128 tick 给自身隐身；owner 在线且 16 格内时同步给 owner 隐身。
  - Kaga / Akagi：已婚友方每 128 tick 给 16 格内同 owner / allied ship 施加 jump boost。
  - Akatsuki / Hibiki / Ikazuchi / Inazuma：已婚友方每 128 tick 给 16 格内 owner 施加对应 Haste / Jump / Strength / Speed。
  - 现代没有 legacy `UseRingEffect` 与 `NumGrudge` 状态，本批用现有 `isMarried()` + friendly/non-hostile gate 承接这类被动；未新增 packet 或存档字段。
- 单人游戏性优先的 shipyard 燃料切片已迁移：
  - small / large shipyard 与 heavy grudge 结构的 fuel slot 统一走 `SmallShipyardRecipes.consumeFuelItem`。
  - `shiptank` 等 lava fluid container 现在按 1000mB 一次提供 20000 power，并保留 drain 后的容器。
  - lava bucket 继续提供 20000 power，并正确返还 empty bucket。
- 单人自动路线补给继续收口：
  - crane route 现在覆盖“只连接 route-energy block、没有 paired item container”的能量转移路径。
  - 修正 ship 在 crane 能量转移成功后重算 load/unload 状态时错误访问空物品容器的风险。
  - crane route 现在有 `shiptank` 液体转移回归：paired chest 中的 lava tank 可以向舰船 cargo 中的 empty tank 输液。
- intermod 现在有 soft bridge 骨架：
  - mod presence 检测
  - bridge loader
  - sync / reset / future attack delegation hook 点
  - 还没有真实 Metamorph API 接入

## 已验证事实

- `compileJava`
- `processResources`
- `runGameTestServer`
- Phase 7 per-ship behavior catalog 已有 GameTest 覆盖，确认 legacy marriage passive 与 attack profile 分发不回退。
- Phase 7 hostile spawn 与 command boundary 已有 GameTest 覆盖，确认 hostile boss runtime 初始化、move/route 清 target、guard 清 route/target。
- Phase 7 hostile scaling 已有 GameTest 覆盖，确认 hostile boss 生成时按 catalog 初始化等级、士气与满血。
- advancement 资源加载正常
- `friendly_ship_deployed` trigger 已注册
- 5 槽 playerskill runtime state 的 save/load 已有回归测试
- extended morph special dispatch 已有回归测试
- morph special legacy class-id 解析、dispatch true/false 集合、cooldown preview 已有回归测试
- class-id 解析测试已覆盖 special 与易混淆非 special 舰种的 friendly/mirror eggMeta 映射。
- legacy ring passive 已有实体级回归测试，覆盖 U511/Ro500 自隐与 Kaga/Akagi 近旁同 owner 舰船 jump boost。
- shipyard fuel 已有纯逻辑回归测试，覆盖 `shiptank` lava 分桶消耗与 lava bucket 容器返还。
- crane route energy 已有实体级回归测试，覆盖 ship 从 paired large shipyard route-energy pool 装载能量且 paired block 不是物品容器的情况。
- crane route liquid 已有实体级回归测试，覆盖 paired chest `shiptank` -> ship cargo `shiptank` 的 lava 转移。
- crane route item 已有实体级回归测试，覆盖 paired chest -> ship cargo 与 ship cargo -> paired chest 的 load/unload row filter。
- waypoint route wait 已有实体级回归测试，覆盖舰船到达 waypoint 后按 stay ticks 等待一次，再推进到 next route node。
- Large Shipyard 已有完整闭环 GameTest，覆盖结构 ring、fuel/energy pool、材料消耗、large egg output。
- Phase 4 菜单已有 GameTest 覆盖，确认 Crane / Waypoint / Large Shipyard / LegacyCore / ShipInventory 的服务端按钮会更新服务端状态并通过菜单读数暴露。
- Phase 5 专用舰船命令包已有 GameTest 覆盖，确认 action、entity id、ship uid、target id、可选 BlockPos、整数参数可 encode/decode 往返。
- 旧 `ServerboundGameplayCommandPacket` 与新 `ServerboundShipCommandPacket` 的 move、guard、attack、stop、AI flags、follow range 已有等价测试，二者最终都落到 `ShipCommandService`。
- 舰船命令权限与距离已有 GameTest 覆盖：非 owner、超距离、死亡舰船、死亡或无效目标不会写入命令状态。
- 单人指挥闭环已有 GameTest 覆盖：Pointer 单舰移动会写入目标点并解除 standby；guard 写入目标 UUID；attack 通过 `canEngage` 后设置目标；stop 清理目标与导航命令。
- team/formation 指挥已有 GameTest 覆盖：selected team slot 只影响已选槽位，current team 影响当前队伍在线舰船，多舰移动保留 formation offset。
- Phase 6 持久化已有 GameTest 覆盖：AI flags、follow range、team slots、slot selection、formation id、ship command runtime 在 save/load 后保持一致。
- 客户端冒烟发现的两个阻塞 bug 已修并加回归：
  - 无 active morph 时右键物品不再因 `MorphHelper.getActiveProfile()` 的 nullable `LazyOptional.map` 崩掉；这会解除 source water / block 右键生成蛋被 morph handler 吞掉的问题。
  - 舰船死亡掉落后不再被 `refreshFromVariant(true)` 从 0 HP 拉回 1 HP；死亡/死亡动画中的舰船不再允许打开 ShipInventory 或接受命令交互。
  - 友方舰船死亡现在走 1.12 风格的 owner-locked recoverable egg：不散落船内 cargo，而是生成带 `RecoveredShip` NBT 的对应 `shipegg*` item entity，并通过 item entity target UUID 锁给 owner 拾取；重新部署会恢复 owner、variant、cargo 与可行动 HP。

## 全 Phase 待办清单

这里是给下一位接手者看的总清单。当前 Phase 1-6 的单人主线已有 code + GameTest 基线，Phase 7 的 per-ship behavior catalog、hostile spawn runtime、route/guard command boundary 第一批切片已接线；用户已做过一轮客户端验收且未发现明显问题，但 typed ship command 与 Phase 7 行为后续仍建议做短客户端冒烟确认输入体验和失败反馈。

### P0 立即验收项

- 客户端手工冒烟记录与待复核点：
  - 用户上一轮实机验收未发现明显问题；之前暴露的生成蛋/交互吞掉、舰船假死后仍能交互等问题已修并有回归。
  - rider host 技能条是否显示正确。
  - morph / mount host 技能条是否显示正确。
  - `1~5` 与 `Z/X/C` 输入是否正常触发技能。
  - Heavy / air projectile FX 与 reaction presentation 是否正常。
  - Boss 门控、首舰门槛、advancement 提示在客户端是否可见。
  - Ship spawn / render / interaction、ShipInventory、Crane、Waypoint、LargeShipyard、LegacyCore 屏幕是否能在真实客户端打开并正常刷新。
  - 友方舰船死亡是否掉出 owner-only recovered egg，其他玩家是否不能捡，owner 拾取后重新部署是否恢复原船数据且不散落 cargo。
  - Phase 5 typed ship command 接线后，再复核 Pointer 的 move / guard / attack / stop / sit / open inventory 和 ShipInventory 的 stop / AI flags / follow range。
- 实体渲染改动后必须进世界冒烟，不能只看主菜单。
- 资源规则继续遵守：运行时 `ResourceLocation` path 必须小写；改 `processResources` 后跑 `cleanProcessResources processResources` 并检查输出。

### Phase 0: 构建工具链 / Mod 入口

- 当前无阻塞待办；ForgeGradle 6、Java 17、现代 `@Mod` bootstrap 已可用。
- 后续只需要维护验证命令与文档一致：
  - Windows Java 17 推荐写法：`$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.3.1'`。
  - 提交前至少跑 `compileJava`；涉及资源或 GameTest 行为时跑 `processResources runGameTestServer`。

### Phase 1: 静态注册表 / 资源

- 已有 GameTest 锁住注册方块/物品/菜单 GUI texture 与 `sounds.json` OGG 引用。
- 后续新增 item/block/menu/sound 时必须同步补资源和覆盖测试，不要只注册 Java ID。
- Legacy 大写资源副本可以保留作参考，但运行时引用必须全部转成小写路径。
- 仍需客户端视觉冒烟确认新增方块模型、物品模型、贴图在真实资源包加载路径下没有紫黑/缺面。

### Phase 2: 音效层

- 当前 `LegacyShipEntity` 已接 idle/hurt/death voice bridge 与 light/heavy/air/melee combat events。
- 待补：
  - 更完整的实体战斗音效家族，例如入水、水上移动、特殊攻击、舰种差异化语音。
  - per-ship / per-class sound hooks，避免所有舰种长期只走通用事件。
  - 客户端实机确认友方 `NEUTRAL`、敌方 `HOSTILE` 的音量分类与距离衰减体验。

### Phase 3: 持久化 Block Entity / 路线补给

- 已收口：Waypoint stay 由舰船侧 `routeWaitTicks` 处理；Crane 不做目标不明的独立抽取 tick；Large Shipyard 已有结构、材料、燃料、能量、输出 GameTest。
- 待补：
  - Crane 与 route node 的真实客户端操作冒烟，尤其是 filter row、load/unload、液体/能量模式显示。
  - 更丰富的燃料自动化和 route energy 对接，不要恢复会吞物品的目标不明暂存 tick。
  - Volcore / Polymetal / Heavy Grudge 的多块联动规则、能量链、自动化循环。
  - Waypoint route 与舰船 AI 的深层重接，例如多舰同时排队、跨维度/远距离路线、异常节点恢复。
  - Large Shipyard 大型结构在真实世界搭建、servant proxy、材料导入、输出阻塞场景的手工验收。

### Phase 4: 菜单 / 界面

- 已有 GameTest 覆盖 Crane、Waypoint、LargeShipyard、LegacyCore、ShipInventory 的服务端按钮和 `ContainerData`。
- 待补：
  - 客户端手工确认所有 Phase 4 screen 的视觉布局、按钮状态、数字刷新、容器 slot 交互。
  - ShipInventory 目前只是 Phase 4 基线：显示 AI flags、route energy、combat stats、mode toggle；旧版完整 AI 页、战斗命令页、packet-heavy 控制流还没回来。
  - Heavy block 控制页目前共用 `LegacyCoreMenu/LegacyCoreScreen`；后续可按 Volcore / Polymetal / Heavy Grudge 分化更深操作。
  - Large Shipyard 终端 UI 需要真实客户端验证 build type、power goal、fuel/energy、output blocking 的显示。

### Phase 5: 网络通信

- 已完成单人指挥闭环所需 packet：
  - `ServerboundShipCommandPacket`
  - `ShipCommandAction`
  - `ShipCommandService`
- 新旧入口统一：旧 `ServerboundGameplayCommandPacket` 的 ship command 分支继续可用，但服务端行为委托给 `ShipCommandService`；Pointer / ShipInventory 的舰船指令已经改走专用 ship packet。
- 已覆盖服务端安全边界：owner/UID gate、距离限制、存活检查、`canEngage`、selected/current team、formation offset、standby 解除、目标清理。
- 待补：
  - 目标阵型、TeamData、FormationRuntimeState 的更细专属编辑包与客户端失败提示。
  - 旧版 packet 驱动的完整 GUI 控制流，把 ShipInventory / AI page / combat page 从 Phase 4 基线推进到完整页面。
  - 多人同屏指挥、盟友权限 UI、复杂 packet versioning。
  - route 编辑、target select、guard/attack 高级模式等更深旧版 packet 族。

### Phase 6: SavedData / 能力系统

- 当前已有 TeitokuData、TeamSavedData、FormationRuntimeState、world combat rules、ship runtime slice。
- 本轮已锁定：
  - AI flags、follow range、commanded position/dimension、guard target UUID、route wait、standby/target clearing 的 NBT 往返。
  - 当前 team、team slots、slot selection、formation id 的 save/load。
  - `ShipInventory` 继续作为 Phase 6 可视化基线，显示 AI flags、follow range、route energy、战斗属性、team/formation/role。
  - `TeitokuHelper` 的 team ship lookup 与 ship uid lookup 支持 GameTest mock player 和真实 ServerPlayer 两条路径。
- 待补：
  - 旧版 `CapaTeitoku` 更完整字段迁移与兼容读写策略。
  - 队伍编辑、目标阵型编辑、Pointer command 高级家族所需的更深持久状态。
  - 更完整的 ship task/combat/runtime state，例如 ammo/fuel/grudge、morale tick、repair/rescue cooldown、formation modifiers。
  - 存档迁移/容错测试：缺字段、旧 NBT、跨版本数据、uid 冲突恢复。

### Phase 7: 实体类型 / 生成

- 当前用单一 `LegacyShipEntity` 承载 legacy eggMeta / class id 变体，spawn egg 和 shipyard 输出已能生成实体。
- 已新增 `LegacyShipBehaviorCatalog` 作为 per-ship runtime hook 入口；攻击 profile 与已婚友方舰 ring passive 已从实体主类分发到 catalog，并有 2 个 GameTest 锁住 dispatch。
- hostile encounter 生成现在可测试且返回实体；boss cooldown 只在 spawn 成功后消耗。路线/护卫命令会清理旧 combat target，减少单人指挥中“还在追旧目标”的边界问题。
- hostile / elite / boss spawn 等级与士气现在也由 behavior catalog 提供，生成后会刷新满血。
- 待补：
  - 继续把每艘舰专属运行时逻辑和 per-ship combat hooks 分批迁入 catalog，不要长期只靠通用 `LegacyShipEntity`。
  - 更完整的 hostile / boss / hime / abyssal spawn 规则与掉落。
  - 舰船 AI route、escort、standby、combat targeting 的复杂边界测试。
  - 生成蛋、shipyard-built egg、hostile mirror、owner claim、first collection 的客户端实机确认。

### Phase 8: 客户端渲染 / 视觉

- 当前有通用 humanoid renderer 与 lowercase-safe texture bridge，但这不是最终视觉对等。
- 待补：
  - 每艘舰精确模型迁移，必须复用旧版 `Model*.java` 的 ModelRenderer 骨架；不要把旧贴图硬套 vanilla PlayerModel。
  - BlockEntityRenderer 覆盖，例如大型方块、核心块、结构状态可视化。
  - 复杂粒子、projectile trail、special beam/torpedo/air strike 的表现。
  - 资源路径小写、纹理尺寸、透明层、render layer、装备显示的真实客户端验收。

### Phase 9: AI / 战斗 / 反应

- 当前已有 escort/standby、owner assist、hostile targeting、legacy stat rebuild、light/heavy/air/melee routing、heavy/air projectile parity、第一批 morph special。
- 待补：
  - 完整 reaction pages 与旧版反应系统。
  - 全投射物家族，而不是只覆盖 heavy / air 的兼容层。
  - per-ship combat hooks、舰种差异、装备行为、特殊攻击、婚戒/合体/骑乘等边界。
  - playerskill / morph special 尾部舰种 parity：当前 catalogue 之外的舰种还要逐个比对 legacy 行为。
  - 单人战斗节奏平衡：cooldown、grudge 消耗、AI 目标选择、boss 门槛体验。

### Phase 10: 世界生成 / 高级配方 / 跨模组

- 当前基本未开始，只保留可玩优先的 vanilla-first 配方和 soft bridge 骨架。
- 待补：
  - 完整 legacy ore-dict / tag 化配方对等，不要只停留在简化配方。
  - DeskReference 全深度文档，覆盖物品、方块、造舰、路线、技能、战斗、世界内容。
  - Worldgen：abyssium / polymetal 矿石、结构生成、姬级 spawner 或 hostile encounter 深化。
  - Loot / advancement / boss progression 与新世界生成内容的闭环。
  - `MorphCompatBridge` 替换为真实 Metamorph API 绑定；其他兼容 mod 只做 soft dependency，不硬崩无依赖环境。

## 下一步建议顺序

1. 做一轮短客户端复核，重点是新接线的 Pointer ship command 与 ShipInventory stop / AI flags / follow range。
2. 进入 Phase 7 单人游戏性：per-ship combat hooks、hostile/boss spawn 深化、AI route/escort/standby 边界。
3. 继续 Phase 8/9：精确模型、reaction pages、全投射物家族、剩余 morph/player-skill special parity。
4. 继续补 recipe / reference / balance 的尾巴，但保持单人友好，不开单人专属数值分支。
5. 如果后续真的要做 intermod，再把 soft bridge 接到真实 Metamorph API。

## 关键现代文件

- `src/modern/java/com/lulan/shincolle/ShinColle.java`
- `src/modern/java/com/lulan/shincolle/teitoku/*`
- `src/modern/java/com/lulan/shincolle/network/*`
- `src/modern/java/com/lulan/shincolle/morph/*`
- `src/modern/java/com/lulan/shincolle/playerskill/*`
- `src/modern/java/com/lulan/shincolle/entity/ship/*`
- `src/modern/java/com/lulan/shincolle/entity/projectile/*`
- `src/modern/java/com/lulan/shincolle/client/MorphClientEvents.java`
- `src/modern/java/com/lulan/shincolle/client/screen/*`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## 一句话总结

项目现在已经从“growth loop 能跑”推进到 Phase 1-6 的资源、音效、Block Entity、菜单、专用舰船命令包、SavedData/Team/Formation 单人指挥闭环都有 GameTest 锁定；下一步可以在短客户端复核后进入 Phase 7+ 的 per-ship gameplay、渲染和世界内容深化。
