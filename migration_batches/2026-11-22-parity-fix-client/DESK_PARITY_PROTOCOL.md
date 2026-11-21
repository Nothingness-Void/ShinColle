# 提督桌、图鉴与雷达对照附件

真源：完整 0aad501 GuiDesk、GuiBook、GuiHelper、GuiHandler、Values、
TileEntityDesk 和物品实际 GUI 调用；现代复用既有队伍/目标服务端命令。
保留原 1.25 GUI 缩放与固定热区，不设计新桌面。

## 映射
- 方块 type 0；function 0 关闭当前页、1 雷达、2 图鉴、3 队伍、4 目标。
  同页再点关闭该页；潜行不打开/不循环，无新增铃声。
- 实际便携物品 DeskItemRadar(type 1)、DeskItemBook(type 2) 共用菜单，没有方块页签栏。
- LegacyDeskState 沿用 GuiHelper 的含边界矩形；方块保存 function/zoom/chapter/page，
  冷却同步沿用 ContainerDesk 的秒→tick 转换。
- 七章上限 1/28/6/20/26/19/4，从 0 起，左/右键 ±1/±10 同章钳位；
  内容、图标和舰船列表来自 Values/GuiBook。元数据 0/1 图标对应 smallegg/largeegg，
  其它映射拆分注册物品；收藏 class 0 是合法 DDI。模型条目保留收藏门槛并复用旧模型/坐骑。
- 六张桌面纹理和图鉴图片是原始字节小写副本；保留空项与语言回退，不补造译文。
- 雷达如旧版解析客户端活实体；物品浮点坐标来自 PacketHelper，采用 BasicEntityItem 过滤及
  1.12 AxisAlignedBB.expand 语义（现代 expandTowards(128,256,128)）。
  删除现代设施/路线/普通物品联系点。列表再次选同行打开背包，无时间窗。
- 核对当前 src/main 与 0aad501 的 GuiDesk.mouseClicked：地图点只有悬停，没有点击打开。
  曾新增的点选择/打开与滚动偏移 helper 已从实现、生成脚本、测试移除。
- 队伍/目标沿用旧方法体及现有服务端处理；目标预览兼容现代注册 ID 和旧简单类名；
  删除额外世界规则分类。

## 视觉边界
一次历史客户端尝试检查便携图鉴、统一桌面图鉴、当前页关闭及空目标页。
雷达/队伍仅切换，未逐张审阅；满列表、雷达到背包、已收藏模型及细动画未验。
之后的队伍排序/冷却断言和地图点击删除不是该次画面的验证证据。

截图位于 `/home/ubuntu/screenshots/`：
ss_5183b608.png、ss_3f44d2f8.png、ss_17cac8dc.png、ss_babd1fd4.png。
数据/方法/资源检查入口见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)。
