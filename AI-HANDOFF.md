# ShinColle AI Handoff

这份文档给新的 AI 快速接手当前 1.20.1 迁移进度用。

先读：
- `PORTING-MISTAKES.md`
- `PORTING-1.20.1.md`
- `MIGRATION-FLOW-CHECKLIST.md`

## 当前总原则

- 优先参考旧版 `src/main/java`，不要闭门造车。
- 新功能迁移顺序要沿旧版依赖链走，先补基础框架，再补上层 GUI / AI / 玩法。
- 对小贴图、小姿态问题不要反复打断大框架迁移，除非会阻塞验证。

## 当前已经立住的大框架

- 现代注册层、资源复用、声音层已经可用。
- `desk`、`waypoint`、`crane`、`small shipyard` 已有现代 block entity / menu / screen / 持久化基线。
- 舰娘实体已经有现代通用实体、生成蛋、归属、GUI、装备槽、基础战斗与第一批旧版数值链。
- 小型船坞已经恢复旧式 `4 材料 + 1 燃料 + 1 输出` 建造主循环。

## 这一批最新完成的关键点

- 新增旧版驱动的提督能力基线：
  - `src/modern/java/com/lulan/shincolle/teitoku/TeitokuData.java`
  - `src/modern/java/com/lulan/shincolle/teitoku/TeitokuDataProvider.java`
  - `src/modern/java/com/lulan/shincolle/teitoku/TeitokuSavedData.java`
  - `src/modern/java/com/lulan/shincolle/teitoku/TeitokuEvents.java`
  - `src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java`
- 新增现代网络骨架：
  - `src/modern/java/com/lulan/shincolle/network/ModNetwork.java`
  - `src/modern/java/com/lulan/shincolle/network/ClientboundSyncTeitokuDataPacket.java`
- 这批 Teitoku 已恢复的旧版字段：
  - `playerName`
  - `playerUID`
  - `hasRing`
  - `ringActive`
  - `ringFlying`
  - `marriageNum`
  - `bossCooldown`
  - `teamCooldown`
  - `collectedShips`
  - `collectedEquipment`
  - `targetClasses`
- 已接入同步时机：
  - 登录
  - 重生
  - 切维度
- 已接入玩法入口：
  - `MarriageRingItem` 会回写提督戒指状态
  - `LegacyShipEntity` 婚舰时会增加 marriage count 并登记 ship collection
  - `LegacyShipSpawnEggItem` 生成友方舰娘时会登记 ship collection

## owner / UID 迁移现状

- 旧版很多系统不是只靠 `UUID`，而是靠 `playerUID` 跑：
  - 队伍
  - 编队
  - target class
  - 舰娘/航点/起重机/船坞归属
- 当前现代 owner 基线已经从 `UUID + name` 扩到 `UUID + name + playerUID`：
  - `src/modern/java/com/lulan/shincolle/ownership/PlayerOwnerData.java`
- 已接回 `OwnerUID` 持久化的现代对象：
  - `src/modern/java/com/lulan/shincolle/blockentity/WaypointBlockEntity.java`
  - `src/modern/java/com/lulan/shincolle/blockentity/CraneBlockEntity.java`
  - `src/modern/java/com/lulan/shincolle/blockentity/SmallShipyardBlockEntity.java`
  - `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`

## 下一步最应该继续啃的主线

不要回去修零碎表现问题，优先继续旧版框架链：

1. `team data`
- 参考旧版：
  - `src/main/java/com/lulan/shincolle/proxy/ServerProxy.java`
  - `src/main/java/com/lulan/shincolle/utility/TeamHelper.java`
  - `src/main/java/com/lulan/shincolle/reference/dataclass/TeamData.java`
- 目标：
  - 把基于 `playerUID` 的队伍数据重新挂回现代 saved data / capability 体系

2. `formation`
- 参考旧版：
  - `src/main/java/com/lulan/shincolle/utility/FormationHelper.java`
  - `src/main/java/com/lulan/shincolle/client/gui/GuiFormation.java`
- 目标：
  - 让当前舰娘旧式数值链真正能消费 formation，而不是只有 placeholder

3. `target class`
- 参考旧版：
  - `src/main/java/com/lulan/shincolle/capability/CapaTeitoku.java`
  - `src/main/java/com/lulan/shincolle/proxy/ServerProxy.java`
  - `src/main/java/com/lulan/shincolle/utility/TargetHelper.java`
  - `src/main/java/com/lulan/shincolle/client/gui/GuiDesk.java`
- 目标：
  - 先补 server-side target-class 数据操作与同步
  - 再补 desk / ship GUI 上层编辑入口

4. `ship GUI command packets`
- 参考旧版：
  - `src/main/java/com/lulan/shincolle/utility/PacketHelper.java`
  - `src/main/java/com/lulan/shincolle/network/*`
  - `src/main/java/com/lulan/shincolle/client/gui/GuiShipInventory.java`
  - `src/main/java/com/lulan/shincolle/client/gui/inventory/ContainerMorphInventory.java`
- 目标：
  - 在现在已有的 `SimpleChannel` 上继续补 serverbound ship command / desk command 这类包

## 现在不要误判的几件事

- 现代 `SimpleChannel` 已经不是未开始，而是 `Partial`。
- 提督 / Admiral 数据已经不是未开始，而是 `Partial`。
- owner 体系已经不是单纯 `UUID-only` 了，`playerUID` 已经开始回来了。
- 当前舰娘实体和战斗仍然是“旧版主链的第一批恢复”，不是最终完整实现。

## 构建与验证

- Java 必须用 JDK 17：
  - `C:\Program Files\Java\jdk-17.0.3.1`
- 最低编译验证：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.3.1'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
./gradlew compileJava processResources
```

- 当前这批代码已经通过上面的编译验证。
- 这次提交前没有重新做客户端运行冒烟；重点是先把大框架落盘并提交。

## 交接时优先看的现代文件

- 模组入口：
  - `src/modern/java/com/lulan/shincolle/ShinColle.java`
- 提督数据：
  - `src/modern/java/com/lulan/shincolle/teitoku/*`
  - `src/modern/java/com/lulan/shincolle/network/*`
- owner/UID：
  - `src/modern/java/com/lulan/shincolle/ownership/PlayerOwnerData.java`
- 舰娘实体：
  - `src/modern/java/com/lulan/shincolle/entity/ship/*`
- 船坞：
  - `src/modern/java/com/lulan/shincolle/blockentity/SmallShipyardBlockEntity.java`
  - `src/modern/java/com/lulan/shincolle/crafting/SmallShipyardRecipes.java`

## 交接一句话总结

当前仓库已经过了“能启动、能放几个物品”的阶段，正在把旧版 `playerUID -> team/formation/target class -> ship/desk GUI -> combat/runtime` 这条真正的大依赖链一段段接回 1.20.1。
