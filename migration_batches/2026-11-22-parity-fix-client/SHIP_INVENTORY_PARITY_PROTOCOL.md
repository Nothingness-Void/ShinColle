# 舰船背包对照附件

真源：0aad501 GuiShipInventory 的货舱页、十二控制页和编队悬停，
GuiHelper 热区及 PacketHelper/EntityHelper GUI 按钮语义；旧树只读。
现代入口为 ShipInventoryScreen/Menu、LegacyShipInventoryControls/Catalog/Presentation 及既有按钮分派同步。

- 旧贴图/坐标/热区；保留 60 槽（6 装备+3×18 货舱）和三页。
- 容量只取实体已有 getInventoryPageSize()，不从 GUI 元数据按舰种虚构；
  Wa 的现有容量以 EntityTransportWa:267 为依据。页面按旧 >0/>1 门槛开放。
- INV_PAGE 请求刷新实际菜单；先发同步快照再完整刷新槽，避免跨页相等物品不刷新的问题。
- 独立最小/最大跟随、撤退血量、分组模型位、工船输入/输出/燃料面按旧按钮 ID。
  第 9–12 页只有页签/指示、没有控件，保持预留空页，不加动作。
- 移除 J/K 等现代快捷操作；实际编队属性、士气和位置来自服务端快照，不用客户端造值。
- 控件/滑块公式与服务器安全断言共用；展示方法和原资源有机械对照检查。

历史客户端已进入测试世界，但没有成功打开目标背包；渲染、热区和编队悬停未视觉验证，
不能以客户端启动或其它背包画面冒充该验收。
测试方法只见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)。
