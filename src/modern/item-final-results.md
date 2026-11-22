# 最后一批物品复核结果

七项均存在偏离；TargetWrench 旧字段不是 static，而是注册 Item 单例的实例内存，且仅在客户端收集点位。现代恢复客户端单例选择，配对时发两坐标到服务端校验；不写 ItemStack NBT。

回收蛋由独立 ItemEntity 子类拒绝伤害，注册类型防火并支持重载；普通船蛋物品不统一增加保护。水面部署恢复两项编辑检查，删除额外碰撞拒绝。ShipTank 严格保留旧版从 stack 而不是 tile 取得传输目标的实际行为，因此不会向机器灌液或从机器抽液。OPTool 删除右键操作，客户端和服务端键盘路径排除舰娘、召唤物和坐骑。婚戒激活键恢复 isActive。

新增三个 GameTest 覆盖上述物品的正常、拒绝、无 NBT 和重载路径；客户端点选算法与服务器配对包分别在测试中驱动，未运行客户端视觉检查。

最终 compileJava：BUILD SUCCESSFUL in 1m 29s。最终 runGameTestServer：302 项完成，301 通过，唯一失败 passiveaistopsautomaticrangetargeting（用户确认的基线失败），日志 /home/ubuntu/gt-final.log。另一个已知基线失败 round2hurtimmunitiesvoidrescuegoddessandresistancematchlegacy 本次未失败。

回归中发现上一轮起重机完成逻辑写入 follow=2 后，被同步回调钳制回 4：失败现场 state=0/time=0、目标和红石均正确，只有 follow=4。已允许同步和读档保留旧版完成值 2；后续回归该测试通过。未修改已知基线 AI 失败。

未修改 src/main 或 baseline；未扩展变身航空战斗库存生命周期。
