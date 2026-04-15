# ShinColle AI Handoff

更新时间：2026-04-16

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
  - `All 44 required tests passed`
- 还没有重新做一轮客户端手工冒烟，所以“测试已绿”不等于“所有 HUD / 粒子 / 交互都已实机确认”。

## 已经落地并接线的主线

- `Teitoku + playerUID + owner UID`
- `TeamSavedData / formation runtime / target-class sync`
- `gameplay command/state packets`
- `desk / formation / ship inventory` 控制闭环
- `large shipyard / heavy grudge` 结构与能量基线
- `hostile encounter / chest loot / world combat rules`
- `growth loop` 生存链：关键配方、宝箱入口、boss 首舰门槛
- `advancement` 新手链：从 polymetal / grudge 一直到 first ship 与 boss unlock
- `projectile parity`：heavy / air attack 的投射物语义、视觉、粒子、reaction
- `GameplayParityGameTests` 已覆盖 team、morph、shipyard、growth loop、advancement、projectile、playerskill runtime 等关键链路

## 这一批新增确认

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

## 仍未完成的内容

- 客户端手工冒烟还没重跑：
  - rider host 技能条
  - morph / mount host 技能条
  - 1~5 与 `Z/X/C` 输入体验
  - projectile FX + reaction presentation
- `recipe` 仍是可玩优先的 vanilla-first 基线，不是 full legacy ore-dict parity。
- `DeskReference` 已补说明，但还不是 full legacy 文档深度。
- `playerskill / morph special` 已恢复主体，不等于所有 legacy 舰种 special 全回来了。
- `MorphCompatBridge` 只是骨架，没有真实外部 API 绑定。

## 下一步建议顺序

1. 跑客户端手工冒烟，确认 hotbar、rider/morph/mount 切换、projectile FX、boss gate。
2. 继续补 playerskill / morph special 的尾部舰种 parity。
3. 继续补 recipe / reference / balance 的尾巴，但保持单人友好，不开单人专属数值分支。
4. 如果后续真的要做 intermod，再把 soft bridge 接到真实 Metamorph API。

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

项目现在已经从“growth loop 能跑”推进到“playerskill / morph 主体也能跑”，并开始补单人实际游玩中的 shipyard 细节；当前最需要的是做一轮客户端实机确认，然后再慢慢补完剩余 special、reference 深度和真实 intermod 对接。
