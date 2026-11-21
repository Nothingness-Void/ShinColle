# 冻结协议

只修改 src/modern 与文档；src/main 只读。六项逐条对照，误报不改，不新增玩法。

| 问题 | 旧入口 | 现代入口 / 断言 |
| --- | --- | --- |
| 主人两个伤害目标Goal | AIHandler:98-111、TargetHelper:578-598、EventHandler:516-533 | 删除Goal与注册；事件传播仍设置复仇 |
| 友方自动Selector | TargetHelper.Selector:80-253 | LegacyShipEntity.canEngage；Monster/Slime、主人记忆排除、可见性 |
| 敌对统一目标列表 | BasicEntityShipHostile:193-197、SelectorForHostile:336-398 | BehaviorCatalog.findLegacyRangeTarget；真实玩家与舰船混合最近3随机 |
| 碰撞近战 | EntityAIShipAttackOnCollide、AIHandler:76-80 | 新Goal；宽度平方×16、初始20 tick、CombatHelper间隔、32 tick寻路、UseMelee |
| 游荡 | EntityAIShipWander、AIHandler:84、BasicEntityShipHostile:187 | 新Goal；1/180、友10/5敌12/1、速度0.8、禁用/空位置 |
| 拾取 | EntityAIShipPickItem:63-80,100-221 | 删除三个门槛、FollowMax公式复核、配置音量；命令/战斗/装载/钓鱼 |

验收：compileJava、runGameTestServer、diff --check、src/main零差异。结束中文本地提交，不push。非LivingEntity目标保持已有现代类型边界，明确报告，不扩大目标/伤害系统。
