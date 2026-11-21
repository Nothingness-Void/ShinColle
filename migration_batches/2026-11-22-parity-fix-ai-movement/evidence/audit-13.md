# 13条审计：处理结果与依据

旧版路径均相对 `src/main/java/com/lulan/shincolle/`；实现路径相对 `src/modern/java/com/lulan/shincolle/`。本表汇总第1—5步问题，不将文档第6步新增为行为问题。

| # | 问题与处理结果 | 旧版依据 | 现代落实/过程证据 |
| --- | --- | --- | --- |
| 1 | 已恢复FollowMin/Max独立状态，默认3/12；GUI范围1–31/2–32，NBT FMin/FMax/FHP，独立同步；删除单一aiFollowRange及推算 | handler/ShipStateHandler:62-64；capability/CapaShipSavedValues:38-49,147-149；client/gui/GuiShipInventory:418-419,1583-1590；各AI分别读Min/Max | LegacyShipEntity、菜单/GUI/命令同步；[step1](step1.md) |
| 2 | 已撤销CLIENT canTeleport；Can_Teleport及ship_teleport=[200,256]放COMMON服务端生效，保留长度异常回退 | handler/ConfigHandler:152,174,373,412,464,547-560 | ShinColleConfig与引用；[step1](step1.md) |
| 3 | 已恢复FollowOwner判定、Min/Max+宽×0.75、拾取max+5、32tick/速度1、严格>冷却和落点主人Y+0.75；删除狼式外圈落点、额外命令门槛和现代速度公式 | ai/EntityAIShipFollowOwner:62-290；utility/EntityHelper.applyTeleport:2179-2239，舰船分支无额外地板/碰撞安全门槛 | LegacyShipFollowOwnerGoal、LegacyShipTeleportHelper；[step2](step2.md) |
| 4 | 已恢复阵型站位、安全点扫描及跟随/实体守卫接入；粒子为明确降级，不宣称箭头等价 | FollowOwner:227-267；Guarding:431-466；utility/FormationHelper:839-873；BlockHelper:52-137,258-273；ParticleHelper历史对照 | LegacyFormationHelper；HEALING_SPARKLE替代ParticleTeam；[step2](step2.md)、[step3](step3.md) |
| 5 | 已恢复守卫迟滞、32tick寻路/速度1、传送、维度取消与阵型阈值；移除每tick moveTo/固定半径/Y+0.1/装备commandSpeed；路线16tick、远端128tick、起重机距离²25 | ai/EntityAIShipGuarding:115-175,244-311,431-531；utility/EntityHelper:1135-1261；FormationHelper:944,988；CapaShipSavedValues:55,155 | LegacyShipGuardGoal、LegacyShipEntity路点/起重机协调；保留装卸内部实现；[step3](step3.md) |
| 6 | 已恢复仅GuardType>0且移动中攻击；aim=20×(150−level)/150+10，删除20tick预热和装备缩减/下限 | ai/EntityAIShipGuarding:187-242,337 | GuardGoal与Entity瞄准公式、独立GuardType存档；[step3](step3.md) |
| 7 | 已删除主人受伤/攻击目标两个Goal和注册，只保留事件propagatePlayerRevenge | handler/AIHandler:98-111；utility/TargetHelper:578-598；handler/EventHandler:516-533 | LegacyShipEntity注册、TeitokuEvents；未发现两个Goal的独立直接测试；[step4决定](step4/decision.md) |
| 8 | 已恢复友方Selector：删除主人伤害记忆捷径，Enemy改Monster/Slime等旧显式类别；自动选择与指令/复仇攻击入口分开 | utility/TargetHelper.Selector:80-253 | Entity.canSelectLegacyTarget/canAttackLegacyTarget及catalog；[step4决定](step4/decision.md) |
| 9 | 已删除独立玩家优先Goal；玩家/友舰合并RangeTarget列表排序，候选>2才从最近3随机 | entity/BasicEntityShipHostile:193-197；utility/TargetHelper.SelectorForHostile:336-398 | RangeTarget/catalog统一列表；fixture明确一船一玩家及四候选、64种子断言最近3；[step4诊断](step4/decision.md) |
| 10 | 已替换MeleeAttackGoal和舰种距离表：宽²×16、32tick寻路、初始20tick、复用CombatHelper近战延迟、按UseMelee注册 | ai/EntityAIShipAttackOnCollide；handler/AIHandler:76-80 | LegacyShipAttackOnCollideGoal、LegacyShipCombatHelper；[step4](step4/decision.md) |
| 11 | 已替换RandomStrollGoal：友10/5、敌12/1、速度0.8、1/180概率；骑乘/坐下/装载/钓鱼禁用，继续条件只看路径 | ai/EntityAIShipWander；handler/AIHandler:84；entity/BasicEntityShipHostile:187 | LegacyShipWanderGoal；[step4](step4/decision.md) |
| 12 | 已删除拾取的combat/无指令/无目标额外门槛；保留旧装载/钓鱼等条件，音量读volumeShip。FollowMax范围第1步已恢复，第4步复核无需重复改 | ai/EntityAIShipPickItem:63-80,100-221 | LegacyShipPickItemGoal；[step4](step4/decision.md) |
| 13 | 已逐行适配六类3D路径算法并接入普通/敌对舰与Rensouhou，server aiStep在super前更新；各舰船移动调用改用ship navigator，保留对应vanilla清理 | ai/path六类；entity/BasicEntityShip:209-210,1028；BasicEntityShipHostile:335-336,878；utility/EntityHelper:197-205,721-780,1475-1635 | entity/ship/path、Entity、goals、catalog、命令/口粮/host停止；[step5](step5/README.md)。飞机/坐骑与debug路径粒子边界见[最终决定](../decision.md) |

## 随机失败不是生产规则错误

- guardTargetUsesLegacyGuardingBounds：实测range=7.01、整数搜索范围7，搜索盒/视线/Selector均可通过；共享batch中其他僵尸进入最近3随机列表。独立batch后通过，未放宽FollowMax或守卫边界。见[step3](step3.md)。
- hostileRangeTargetMixesPlayersAndShipsBeforeNearestThreeRoll：goalCount=2；mock玩家非创造/观察者/无敌且可选；实际5个候选含同坐标遗留舰，命中另一艘距离2舰。只修fixture清理与确定候选集合，不为测试改随机规则。见[step4](step4/decision.md)。
