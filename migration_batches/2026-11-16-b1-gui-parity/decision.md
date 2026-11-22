# B1 界面对齐历史决定

## 说明
这是 2026-11-16 的历史记录，保留当时证据；不把旧“完全等价”解释为当前完整一致性。
撤销依据统一为 [D-016](../../MIGRATION-DECISIONS.md#d-016客户端行为严格还原撤销历史简化等价声明)，替代结论见 [客户端还原](../2026-11-22-parity-fix-client/decision.md)。

## 2. 当时审计与决定

### 2.1 大型船坞
旧 ContainerLargeShipyard:44–47 输出槽 (168,51)，九托盘 (7+i×18,116)，i=1..9；
(9,83) 是燃料条而非物品槽。旧切换按钮 (23,93,25,20)，输出覆盖 (23,92) / UV(208,82,25,20)；
选材框 (27,14+19×selectMat) / UV(208,64,18,18)；
调量面板 (50,8+19×selectMat) / UV(0,223,48,30)，八按钮 ±1000/100/10/1。
库存 x125、y20/39/58/77 黄色；建造数 x73，<100 红、==1000 黄、其它白。
当时据此调整 LargeShipyardMenu/Screen 的槽位、点击与绘制。

### 2.2 核心
旧 ContainerVolCore:33–41 的 3×3 格为 (62+18×col,19+18×row)，玩家背包 (8+18×col,84+18×row)，热栏 y142。
旧电源按钮 (7,6,13,13)、活动 UV(12,166,13,13)；
燃料条高 31，(38,59-chargeBar) / UV(0,197-chargeBar,12,chargeBar)，界面高166。
当时据此调整 LegacyCoreMenu/Screen。

### 2.3 吊车
**电源绘制/提示等价声明已撤销（见 D-016）**：旧源码注释掉的内容不得绘制。
当时记录 GuiCrane:289–293 的电源坐标 (7,6,13,13)、UV(176,0,13,13)，并新增电源覆盖与提示；
这部分显示决定已撤销。左右键等待增减及服务端物品传输并不因此一并撤销，后续严格生命周期结论见新批次。

### 2.4 桌面参考界面
**作为旧桌面完整等价入口的声明已撤销（见 D-016）**。
当时仅核对 DeskReferenceScreen 对 getSingleplayerServer 的 null 保护，避免多人 NPE；
它不能证明旧雷达/图鉴内容完整，现已由统一 DeskScreen 替代。

### 2.5 当时宣称无需改动的条目
1. GuiSmallShipyard/ContainerSmallShipyard → SmallShipyardScreen/Menu：原记录称完全一致；本次未重新审计该项。
2. GuiRecipePaper/ContainerRecipePaper → RecipePaperScreen/Menu：原记录称幽灵槽/NBT 同步完全一致；本次未重新审计该项。
3. **已撤销（见 D-016）**：GuiShipInventory/ContainerShipInventory → ShipInventoryScreen/Menu 的“槽位100%相同、AI快捷键对齐”声明；替代为旧货舱页、控制与热区，不保留自创快捷键。
4. **已撤销（见 D-016）**：GuiFormation/ContainerFormation → FormationScreen/Menu 的“现代控件完全功能等价”声明。
5. **已撤销（见 D-016）**：GuiMorphInventory/ContainerMorphInventory → MorphInventoryScreen/Menu 的“完全功能等价”声明。
6. **已撤销（见 D-016）**：GuiDesk → DeskTerminalScreen/DeskReferenceScreen/WaypointTerminalScreen 的“完全功能等价”声明；尤其航点不是自创终端。

获取/选择与空值语义同时受 [D-018](../../MIGRATION-DECISIONS.md#d-018) 约束；不恢复 D-016 已删除的简化界面。

## 3. 历史证据
当时 compileJava 和 runGameTestServer 通过，276/276，未有阻止构建的错误。
这只是当时运行记录，不证明上述撤销项的完整视觉/行为一致性；不改写当时通过数。
