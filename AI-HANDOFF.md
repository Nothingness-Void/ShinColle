# ShinColle AI Handoff

更新时间：2026-04-20

先读：
- `PORTING-MISTAKES.md`
- `PORTING-1.20.1.md`
- `MIGRATION-FLOW-CHECKLIST.md`

## 绝对规则

- 迁移工作只允许修改 `src/modern/java`、`src/modern/resources`，以及对应的 1.20.1 迁移文档。
- `src/main/java`、`src/main/resources` 与 `mc-1.12.2` 分支只作为 1.12 真源参考，默认只读，不得把 1.20.1 迁移改动提交、合并或回写进去。
- 后续所有迁移分支、提交、PR 都应落在 `codex/mc-1.20.1` 这条 1.20.1 工作线上；`mc-1.12.2` 不是目标分支。
- 判断行为、数据、资源、UI、输入语义时，以 1.12 实现为真源；实际代码适配和实现落点始终写在 1.20.1 modern 侧。
- 如果本地 `mc-1.12.2` 被误移动、误合并或误污染，应立即重置回 `upstream/mc-1.12.2`，不要在错误基线上继续工作。

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
  - `All 79 required tests passed`
- 用户已对上一批客户端内容做过一轮手工验收，未发现明显问题；近期新增了单人主线 GameTest、Phase 8 legacy model source 解析覆盖、ship fuel / ammo / grudge runtime supply 回归，以及 Phase 5-10 单人主线收口测试。Codex 已做 `runClient` 启动/资源冒烟，日志到达资源 reload、sound engine 与 atlas 创建，未见 ShinColle crash、missing texture 或 model fallback；完整世界内交互仍建议后续实机复核。

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
- Phase 7 第四切片已完成到 code + GameTest 基线：
  - `LegacyShipBehaviorCatalog` 现在拥有第一批高价值 per-ship combat hook：Shimakaze、Nagato、Yamato、Tenryuu、Tatsuta、Atago/Takao、Kongou-class，以及对应 hostile mirror。
  - `LegacyShipEntity` 的玩家/AI heavy combat path 先查 catalog，不再在实体主类新增 class-id switch；hook 复用现有 projectile visual、particle 与 reaction 表达，未进入 Phase 8 renderer 范围。
  - `BossPhaseProfile.forSpec(...)` 已收口到 behavior catalog；hostile loot profile 也改由 catalog 提供，保留 boss bonus、abyss metal 与 egg drop chance 规则。
  - `LegacyShipPickItemGoal` 现在让位于 active move/route/guard、combat target、sit 状态与 auto-supply disabled，避免自动拾取抢走指挥路径。
  - 新增 GameTest 覆盖 combat hook class-id/mirror 分发、special heavy cooldown / miss 语义、boss/loot profile、pickup goal 边界；顺手让 air attack 与 world-rule payload 旧测试对持久测试世界更稳定。
- 本轮单人主线 smoke 已完成到 code + GameTest 基线：
  - 新增跨系统 GameTest，覆盖 support item、AI flags / follow range、typed move / attack / stop、boss unlock / spawn，以及 heavy hook cooldown。
  - 新增 Phase 8 资源解析 GameTest，覆盖当前单人可到达舰船 legacy model source，以及 aircraft / takoyaki / mount static model source，要求 packaged source 能 parse 成非空可渲染 model definition。
  - 2026-04-19 `runClient` 启动/资源冒烟到达 resource reload、sound engine、texture atlas creation；检查日志未见 ShinColle crash、missing texture、model fallback。该验证不等同于进世界手测，Pointer、ShipInventory、rider/morph input 与 combat FX 仍列为 P0 实机项。
- 2026-04-20 单人 runtime supply 切片已完成到 code + GameTest 基线：
  - `LegacyShipEntity` 新增持久/同步的 fuel、light ammo、heavy ammo、grudge，读档兼容旧 `NumAmmoLight` / `NumAmmoHeavy` / `NumGrudge`。
  - 友方非 melee 攻击现在按舰种族消耗 fuel + ammo + grudge；hostile 舰不受资源耗尽限制，避免单人遭遇战跑空。
  - Combat ration 会补 ship fuel，grudge / ammo 支援物品会补对应 runtime supply；auto supply 能在资源低位时消耗船舱支援物品。
  - ShipInventory 增加 supply tier 行和 tooltip 明细，离线 ship cache 也能读新增 NBT。
  - 新增 GameTest 覆盖资源耗尽阻止攻击、补给后恢复攻击、单次攻击消耗、NBT 持久化、旧 tag 迁移、ration/grudge/light ammo/heavy ammo 直接补给。
- 2026-04-20 Phase 5-10 双层收口已完成到 code + GameTest 基线：
  - Phase 5/6：玩家技能、rider ship skill、morph attack / special 失败时会给出 actionbar 原因，包括 no host、invalid target、skill unavailable、cooldown、out of range、no fuel、no grudge、no light/heavy ammo。
  - Phase 7/9：`LegacyShipBehaviorCatalog` 显式声明 `BehaviorCoverageTier`、`BehaviorCoverage` 与单人 AI 优先级，GameTest 锁住所有 reachable friendly / hostile / boss roster 都已归类为 `SPECIFIC_HOOK` 或 `GENERIC_MAINLINE`。
  - Phase 10：新增 `SinglePlayerResourceSourceCatalog`，把 polymetal、abyssmetal、grudge、ammo、combat ration、fuel、shipyard/build、DeskReference 的 world / recipe / loot / hostile / advancement 来源记录成可测矩阵。
  - DeskReference logbook 的 Logistics 章节新增 Resource Sources 页，直接展示单人主线资源来源。
  - `MIGRATION-FLOW-CHECKLIST.md` 已将 Phase 5-10 标为 `Done (SP mainline)`，并新增 Full 1.12 parity backlog。
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
- 当前 GameTest baseline：`All 79 required tests passed`
- 单人主线 smoke 已有 GameTest 覆盖，确认 support item、typed ship command、boss gate/spawn 与 heavy combat cooldown 在同一闭环内可用。
- 单人 runtime supply 已有 GameTest 覆盖，确认 fuel/light ammo/heavy ammo/grudge 的攻击门槛、消耗、NBT、旧 tag 迁移和直接补给路径。
- Phase 5-10 双层收口已有 GameTest 覆盖，确认玩家可见失败反馈、行为 catalog 覆盖矩阵、AI 优先级、Phase 10 资源来源矩阵和 DeskReference resource source 行均可用。
- Phase 8 legacy model source 解析已有 GameTest 覆盖，确认单人可到达舰船与 summon/mount 静态模型源不会静默解析为空模型。
- `runClient` 启动/资源冒烟已到 resource reload、sound engine、texture atlas creation，已查日志未见 ShinColle-specific crash / missing texture / model fallback；完整世界内交互仍待手测。
- Phase 7 per-ship behavior catalog 已有 GameTest 覆盖，确认 legacy marriage passive 与 attack profile 分发不回退。
- Phase 7 hostile spawn 与 command boundary 已有 GameTest 覆盖，确认 hostile boss runtime 初始化、move/route 清 target、guard 清 route/target。
- Phase 7 hostile scaling 已有 GameTest 覆盖，确认 hostile boss 生成时按 catalog 初始化等级、士气与满血。
- Phase 7 special combat hook 已有 GameTest 覆盖，确认 Shimakaze/Nagato/Yamato/Tenryuu/Tatsuta/Atago/Takao/Kongou-class 与 hostile mirror 通过 catalog 分发，并能触发 heavy cooldown 且保留 heavy miss 不直接伤害语义。
- Phase 7 boss/loot profile 已有 GameTest 覆盖，确认 boss action cycle、escort summon egg、base cooldown 与 hostile loot metadata 从 catalog 返回。
- Phase 7 pickup goal 边界已有 GameTest 覆盖，确认 route、guard、combat、sit、auto supply disabled 不会被自动拾取打断。
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

这里是给下一位接手者看的总清单。当前 Phase 0-4 为完整 Done；Phase 5-10 已按 `Done (SP mainline)` 收口，单人主线的网络/GUI/持久化、实体/AI/战斗、视觉资源防线、投射物反馈、世界资源闭环和 DeskReference 指引都有 code + GameTest 基线。完整 1.12 parity 现在单独作为 backlog：多人 ally UI、完整旧版 GUI packet pages、全 inter-mod、完整 worldgen 矩阵、模型精确渲染和全部边角舰种细节不阻塞本轮完成。后续仍建议做短客户端冒烟确认输入体验、视觉表达和失败反馈。

### P0 立即验收项

- 客户端手工冒烟记录与待复核点：
  - 用户上一轮实机验收未发现明显问题；之前暴露的生成蛋/交互吞掉、舰船假死后仍能交互等问题已修并有回归。
  - Codex 本轮只完成 `runClient` 启动/资源冒烟；仍需进世界生成友方舰船和 hostile / boss 实体做交互验证。
  - rider host 技能条是否显示正确。
  - morph / mount host 技能条是否显示正确。
  - `1~5` 与 `Z/X/C` 输入是否正常触发技能。
  - Heavy / air projectile FX 与 reaction presentation 是否正常。
  - Boss 门控、首舰门槛、advancement 提示在客户端是否可见。
  - Ship spawn / render / interaction、ShipInventory、Crane、Waypoint、LargeShipyard、LegacyCore 屏幕是否能在真实客户端打开并正常刷新。
  - 友方舰船死亡是否掉出 owner-only recovered egg，其他玩家是否不能捡，owner 拾取后重新部署是否恢复原船数据且不散落 cargo。
  - Phase 5 typed ship command 接线后，再复核 Pointer 的 move / guard / attack / stop / sit / open inventory 和 ShipInventory 的 stop / AI flags / follow range。
  - runtime supply 接线后，复核 ShipInventory supply 行/tooltip、combat ration fuel、grudge/ammo 支援物品和弹药不足时的攻击失败反馈。
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

- 当前已有 TeitokuData、TeamSavedData、FormationRuntimeState、world combat rules、ship command/runtime supply slice。
- 本轮已锁定：
  - AI flags、follow range、commanded position/dimension、guard target UUID、route wait、standby/target clearing 的 NBT 往返。
  - ship fuel、light ammo、heavy ammo、grudge 的 NBT 往返，以及旧 `NumAmmoLight` / `NumAmmoHeavy` / `NumGrudge` 迁移。
  - 当前 team、team slots、slot selection、formation id 的 save/load。
  - `ShipInventory` 继续作为 Phase 6 可视化基线，显示 AI flags、follow range、route energy、runtime supply、战斗属性、team/formation/role。
  - `TeitokuHelper` 的 team ship lookup 与 ship uid lookup 支持 GameTest mock player 和真实 ServerPlayer 两条路径。
- 待补：
  - 旧版 `CapaTeitoku` 更完整字段迁移与兼容读写策略。
  - 队伍编辑、目标阵型编辑、Pointer command 高级家族所需的更深持久状态。
  - 更完整的 ship task/combat/runtime state，例如 morale tick、repair/rescue cooldown、formation modifiers 和更细的 supply failure feedback。
  - 存档迁移/容错测试：缺字段、旧 NBT、跨版本数据、uid 冲突恢复。

### Phase 7: 实体类型 / 生成

- 当前用单一 `LegacyShipEntity` 承载 legacy eggMeta / class id 变体，spawn egg 和 shipyard 输出已能生成实体。
- 已新增 `LegacyShipBehaviorCatalog` 作为 per-ship runtime hook 入口；攻击 profile、已婚友方舰 ring passive、第一批 special combat hook、boss phase profile 与 hostile loot profile 已从实体主类分发到 catalog，并有 GameTest 锁住 dispatch。
- 友方 ranged / air combat 现在已有通用 fuel + ammo + grudge 门槛和消耗；hostile 舰保留无限资源，避免遭遇战因资源耗尽失效。剩余舰种的特殊消耗、失败反馈和细分补给策略仍需后续补。
- hostile encounter 生成现在可测试且返回实体；boss cooldown 只在 spawn 成功后消耗。路线/护卫命令会清理旧 combat target，减少单人指挥中“还在追旧目标”的边界问题。
- hostile / elite / boss spawn 等级与士气现在也由 behavior catalog 提供，生成后会刷新满血。
- 自动拾取现在让位于 active move/route/guard、combat target、sit 与 auto-supply disabled，避免 route/guard/combat 优先级被拾取目标抢走。
- 待补：
  - 继续把剩余舰种专属运行时逻辑和 per-ship combat hooks 分批迁入 catalog，不要长期只靠通用 `LegacyShipEntity`。
  - 更完整的 hostile / boss / hime / abyssal spawn 规则、掉落和 encounter 深度。
  - 舰船 AI route、escort、standby、combat targeting 的更复杂边界测试。
  - 生成蛋、shipyard-built egg、hostile mirror、owner claim、first collection 的客户端实机确认。

### Phase 8: 客户端渲染 / 视觉

- 当前有通用 humanoid renderer 与 lowercase-safe texture bridge，但这不是最终视觉对等。单人可到达舰船与 summon/mount static legacy model source 已有 parser GameTest，能防止 packaged source 缺失或解析为空模型；真实 renderer 仍未迁到每舰精确模型。
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

- 当前单人主线已完成：polymetal、abyssmetal、grudge、ammo、combat ration、fuel、shipyard/build、DeskReference 的来源都登记在 `SinglePlayerResourceSourceCatalog`，并由 GameTest 检查 data-pack 资源存在。
- DeskReference 已有 Resource Sources 页，能把单人主线资源来源直接展示给玩家。
- Backlog：
  - 完整 legacy ore-dict / tag 化配方对等。
  - 全旧版 worldgen 矩阵、结构生成矩阵、历史 dungeon/loot 分布。
  - DeskReference 全深度百科，不只覆盖单人主线资源闭环。
  - `MorphCompatBridge` 替换为真实 Metamorph API 绑定；其他兼容 mod 只做 soft dependency，不硬崩无依赖环境。

## 下一步建议顺序

1. 做一轮进世界客户端复核：生成 1 个友方舰船和 1 个 hostile / boss，重点验证 Pointer ship command、ShipInventory stop / AI flags / follow range / supply readout、support item refill、rider/morph 输入，以及 Phase 7 第一批 special combat hook 的现有 FX 表达。
2. 若客户端复核发现单人阻塞，优先修 crash、missing texture/model fallback、命令失效、GUI 不刷新和存档破坏。
3. 其余工作按 Full 1.12 parity backlog 排期：多人 ally UI、完整旧版 GUI packet pages、全 inter-mod、完整 worldgen 矩阵、精确模型/BER、全部边角舰种细节。

## 关键现代文件

- `src/modern/java/com/lulan/shincolle/ShinColle.java`
- `src/modern/java/com/lulan/shincolle/teitoku/*`
- `src/modern/java/com/lulan/shincolle/network/*`
- `src/modern/java/com/lulan/shincolle/morph/*`
- `src/modern/java/com/lulan/shincolle/playerskill/*`
- `src/modern/java/com/lulan/shincolle/entity/ship/*`
- `src/modern/java/com/lulan/shincolle/entity/projectile/*`
- `src/modern/java/com/lulan/shincolle/world/SinglePlayerResourceSourceCatalog.java`
- `src/modern/java/com/lulan/shincolle/client/MorphClientEvents.java`
- `src/modern/java/com/lulan/shincolle/client/screen/*`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## 一句话总结

项目现在已经按“双层收口”完成 Phase 5-10 的 `Done (SP mainline)`：单人主线网络/GUI/存档、行为 catalog、AI/战斗、视觉资源防线、投射物反馈、世界资源闭环和 DeskReference 指引都有 GameTest 锁定。下一步只需要做进世界客户端复核；完整 1.12 parity 作为独立 backlog 继续排期。
