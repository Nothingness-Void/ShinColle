# 协议与验收入口

本文件是四轮既有执行过程的收口补档，不冒充代码实现前已冻结的协议。第四轮原执行记录见 `src/modern/item-final-protocol.md`。

## 旧版入口

以下均位于 `src/main/java/com/lulan/shincolle/`，只读：

- `handler/ConfigHandler.java`；`tileentity/TileEntitySmallShipyard.java`、`TileMultiGrudgeHeavy.java`、`TileEntityCrane.java`；`utility/TileEntityHelper.java`、`InventoryHelper.java`、`EntityHelper.java`。
- `block/BlockGrudgeHeavy.java`、`BlockCrane.java`、`BlockSmallShipyard.java`、`BlockVolCore.java`、`BlockWaypoint.java`。
- `entity/BasicEntityShip.java`、`BasicEntityShipCV.java`、`BasicEntityShipHostile.java`；`ai/path/ShipMoveHelper.java`。
- `item/BasicItem.java`、`ModernKit.java`、`TrainingBook.java`、`BucketRepair.java`、`CombatRation.java`、`AbyssMetal.java`、`ToyAirplane.java`、`KaitaiHammer.java`、`BasicEntityItem.java`、`ShipSpawnEgg.java`、`ShipTank.java`、`TargetWrench.java`、`OPTool.java`、`MarriageRing.java`。
- `utility/InteractHelper.java`、`TargetHelper.java`；`reference/dataclass/Attrs.java`；`handler/ShipStateHandler.java`、`AttrStateHandler.java`。
- 当前起重机源码存在半重构残片时，用 `git show 0aad501:src/main/java/com/lulan/shincolle/tileentity/TileEntityCrane.java` 核对过滤器与搬运原意，不修改旧版，也不把现代实现当作真源。

## 现代入口

对应方块、方块实体、物品、配置、`LegacyShipEntity`、`MorphProfile`、菜单、客户端效果及网络适配；精确清单见 [改动文件](results/changed-files.md)。

## 验收约定

- 逐条先复核；部分误报单独说明，不因审计描述而改掉本来正确的行为。
- 删除自创路线能量接口与其实现、调用、菜单显示、语言资源和测试。
- 恢复旧版实际执行结果，包括 ShipTank 的自身目标，不自行修复旧版疑似笔误。
- 相关测试改断言旧行为，覆盖正常、空、拒绝、资源和存读档边界；不以通过构建代替视觉检查。
- 最终检查：`bash ./gradlew compileJava`；`bash ./gradlew runGameTestServer > /home/ubuntu/gt-final.log 2>&1`；`git diff --check` 和旧版目录零差异。
- 结果与覆盖分别见 [证据](results/README.md) 和 [测试矩阵](../../MIGRATION-TEST-MATRIX.md)。

## 非目标

不修用户确认的两项基线失败；不触碰基线目录；不补外置 IC2；不扩展变身航空攻击消耗；不宣称 Dust 粒子适配已经视觉等价；不改目标分支或创建合并请求。
