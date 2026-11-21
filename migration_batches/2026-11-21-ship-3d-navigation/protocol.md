# 舰船3D水空寻路冻结协议

只改src/modern与文档，不改src/main。逐行适配旧自定义六类，不使用vanilla PathFinder/NodeEvaluator替代。

旧入口：ai/path/ShipPath、ShipPathPoint、ShipPathHeap、ShipPathFinder、ShipPathNavigate、ShipMoveHelper；BasicEntityShip构造与server onLivingUpdate；BasicEntityShipHostile及IShipAttackBase使用者；EntityHelper.updateShipNavigator、TaskHelper；AI各getShipNavigate调用点。

现代入口：entity/ship/path新六类；LegacyShipEntity server aiStep、全部舰船移动goals/route/crane/combat/catalog、同源使用舰船；GameplayParityGameTests新增水上下3D实际位移与canFly空中无重力，并调整受影响导航fixture。

范围：保持算法、节点分类、距离/超时/转向/速度、vanilla导航保留用途。审计坐骑/飞机旧导航来源，小范围才接入；不能按现代类型凭空套用。现代调试粒子可复用则接，否则明确说明。

验收：compileJava、runGameTestServer全绿，diff --check，src/main零修改；完成后中文本地commit并push origin devin/parity-fix-ai-movement。不改渲染、配置规格、远程伤害玩法。
