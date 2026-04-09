# ShinColle AI Handoff

更新时间：2026-04-10

先读：
- `PORTING-MISTAKES.md`
- `PORTING-1.20.1.md`
- `MIGRATION-FLOW-CHECKLIST.md`

## 当前基线

- 实际参与构建的是 `src/modern/java` 和 `src/modern/resources`。
- `src/main/java` 继续作为 legacy 参考实现，不是当前构建主线。
- 已验证 JDK 17 构建与 GameTest 基线：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat compileJava processResources runGameTestServer
```

- 结果：
  - `BUILD SUCCESSFUL`
  - `All 30 required tests passed`
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
- morph special 分发表不再只剩天龙 / 龙田：
  - `58 / 2058` Tenryuu
  - `59 / 2059` Tatsuta
  - `60 / 2060 / 61 / 2061` Takao-class heavy cruiser group
  - `62 / 2062 / 63 / 2063 / 64 / 2064 / 65 / 2065` Kongou-class group
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

项目现在已经从“growth loop 能跑”推进到“playerskill / morph 主体也能跑”的阶段；当前最需要的不是继续堆大迁移面，而是做一轮客户端实机确认，然后再慢慢补完剩余 special、reference 深度和真实 intermod 对接。
