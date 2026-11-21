> 历史步骤证据：保留当时的指令、失败和待办语境；最终结论与后续交付以[统一批次决定](../../decision.md)为准。

# 舰船3D导航

入口：protocol.md（冻结范围）、decision.md（API适配和边界）、STATUS.md（执行记录）、results/（验收）。

旧版依据：src/main/java/com/lulan/shincolle/ai/path/ 六类；BasicEntityShip:209-210、1028；BasicEntityShipHostile:335-336、878；EntityHelper:721-780、1475-1635。半重构后的普通舰物理以0aad501的BasicEntityShip:1863-1865、2056-2067、3243-3245及getMoveSpeed/getJumpSpeed为补充依据。

现代实现：entity/ship/path下六类保持自定义堆、节点哈希、曼哈顿代价、450搜索上限、70范围、节点生成顺序、超时和卡住处理，未调用原版PathFinder/NodeEvaluator。ShipNavigationHost适配Mob更新和旧移动物理。普通/敌对舰与Rensouhou共享该适配。

移动调用覆盖Follow/Guard/Wander/AttackOnCollide/PickItem/Flee/RangedAttack/OpenDoor、行为目录Task/Skill、路点、起重机、命令服务、战斗口粮、坐骑绑定时的舰船停止。坐标与实体请求分别使用tryMoveToXYZ/tryMoveToEntityLiving，原速度参数保留。原版navigator保留供更新清理，坐下同时清两个路径。

GameTest：真实服务端aiStep水中上升到水面、由水面下潜、canFly空中静止无重力及向高处实际移动；另覆盖null/empty/blocked/sitting路径。旧导航录制fixture迁移到ShipPathNavigate接口，不改旧行为断言。

验收命令：bash ./gradlew compileJava --no-daemon；bash ./gradlew runGameTestServer --no-daemon；git diff --check；git diff --name-only -- src/main。
