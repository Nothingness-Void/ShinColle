# 编队对照附件

真源：0aad501 GuiFormation、GuiHelper GUI-4 热区、FormationHelper.getFormationBuffValue、
BuffHelper.calcAttrsWithoutBuff 及 EntityAbyssMissile 构造。旧目录只读。

现代入口：FormationScreen、FormationMenu、LegacyFormationScreenState、属性展示访问器和回归辅助断言。
- 六阵型/九分队直接选，保留原热区、阵位点、加成条、士气、舰名及属性提示。
- 切队等待 60 GUI tick，重排 40；按旧 drawScreen 推进，不造毫秒计时；等待不允许重排。
- 舰船行首次查看，同一行再次打开背包，不限点击间隔。移除 250ms 窗口、点击改选舰、独立打开/清队按钮。
- CLEAR_CURRENT_TEAM 还被指挥棒 sprint+shift 使用，保留网络与服务器处理，不能整条删除。
- 同步实际属性、无阵型属性、阵位、最低移动和全部 36 组预览，不造客户端属性公式。
- Ro500 测试基于实际 launch/target 的旧总距离/水平距离阈值及回退规则计算期望，不修改导弹算法。

覆盖/门禁见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)。
历史视觉只检查空编队、第九分队、阵型预览及等待禁止重排；
满编舰名/士气、实际打开舰船背包未验。截图：
`/home/ubuntu/screenshots/ss_4a368cb0.png`。
