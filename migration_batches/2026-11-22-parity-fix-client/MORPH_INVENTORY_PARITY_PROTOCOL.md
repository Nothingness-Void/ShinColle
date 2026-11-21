# 变身背包对照附件

真源：0aad501 GuiMorphInventory、GuiHelper GUI 0/7、ContainerMorphInventory、
C2SGUIPackets MorphBtn/MorphBtn2、PacketHelper.setEntityByGUI。

## 界面与状态映射
- 原 GuiShipMorph 小写贴图、256×214、旧图标/文字/含边界矩形和三页；
  六装备槽/玩家背包坐标保持 ContainerMorph，不显示档案箭头、序号和激活开关。
- 模型锚点 (218,100)，旧逐舰模型位置，鼠标偏移 (215-xMouse,60-yMouse)；
  共用旧舰船背包渲染桥的 atan 旋转及实体/相机状态恢复，实际使用机械移植的旧模型。
- 当前/最大生命为 ceil(player.getHealth/getMaxHealth)，不是档案舰船最大 HP。
- MorphBtn → MORPH_GUI_BUTTON，保留 ShipGuiButton ID/value：
  NoFuel 赋 value>0，Sit 忽略 value 直接翻转，Emotion 使用 nextInt(9)，Emotion4 为 0/4，
  模型位 XOR，光环/持物按传入值赋标志；未知 ID 无修改。
- 缺失状态保存在 MorphProfile.MorphGui 并随档案同步；旧档缺字段为 false/0。
  不造怨念补充、消耗或新的燃料规则。MorphBtn2 三个供应热区复用已有补给消耗处理。
- 既有敌对档案的图标/模型位置使用明确 friendlyCounterpart 映射，
  实际战斗属性与预览实体仍用原敌对 spec，不修改身份。
- 删除 GUI 专属循环/激活网络、辅助方法、标签及测试；保留其它功能使用的档案存储、
  显式选择和 active 状态。新激活入口缺失的证据统一见 [批次决定](decision.md#变身激活入口核查)。

## 限制与历史画面证据
MorphProfile 没有历史击杀/航空机库存，相应展示维持 0，不造新生命周期；
预览临时实体不等价于外部世界变身系统。

一次客户端启动，通过已有目标扳手解锁爱宕：
当前/最大 HP 为 20/24，后自然回复至 21/24；三页、鼠标旋转和 Sit/NoFuel 勾选/姿态已检查。
其它舰种与外部世界变身没有视觉检查。
截图目录 `/home/ubuntu/screenshots/`：
- ss_09dec2df.png / ss_84ee9487.png：相反鼠标位置；后者显示 HP。
- ss_9f64688b.png / ss_6d360779.png：坐姿与 NoFuel 姿态/勾选。
- ss_1023c9eb.png / ss_b836dcea.png：第二/第三页。

生成脚本只复用舰船背包的适配函数/渲染桥，不执行其写入或修改舰船背包源码。
回归并入已有变身补给测试，避免新增世界夹具；覆盖页签、标志包、补给、模型位、
敌对元数据、空/旧数据和无效按钮。方法见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)。
