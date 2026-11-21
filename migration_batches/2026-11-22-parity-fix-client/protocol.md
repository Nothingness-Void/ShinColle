# 客户端行为还原与收尾协议

## 问题与范围

本批次归档既有客户端还原成果，核对删除自创激活开关后的可达入口，纠正历史等价声明。
只迁移文档、更新权威索引并重跑最终验收；不新增激活入口、不扩展粒子或变身底层、不启动客户端。
生产代码保持 `606041e` 的实现；`src/main/**` 与 `/home/ubuntu/legacy0aad` 只读。

## 定向核对入口

- 现代：`src/modern/java/com/lulan/shincolle/morph/{MorphHelper,MorphEvents,MorphRuntimeState,MorphCompatBridge,MorphCompatBridgeLoader}.java`。
- 现代：`teitoku/TeitokuData.java`、`item/TargetWrenchItem.java`、`client/MorphClientEvents.java`、`network/GameplayCommandHandler.java`、`network/GameplayCommandType.java`（均相对现代包 `com/lulan/shincolle`）。
- 全现代源码仅对 `setMorphActive`、`setActive`、`Active` 赋值及反射/桥调用作定向引用搜索，包含命令、物品、网络、存档和测试；区分解锁、选择、载具切换和真正激活。
- 旧版 `0aad501`：`intermod/MetamorphHelper.java`、`client/gui/GuiDesk.java`、`GuiMorphInventory.java`、`GuiShipInventory.java`、`GuiFormation.java`、`GuiHelper.java`、`GuiHandler.java`；`tileentity/TileEntityCrane.java`、`entity/BasicEntityShip.java`、`block/BlockWaypoint.java`；`utility/{PacketHelper,ParticleHelper,RenderHelper,EmotionHelper}.java`、`client/model/` 与旧 `LayerShipHeldItem`。
- 文档：`AGENTS.md`、核心状态/队列/测试/决定、B1 决定 §2.3–2.5、相关历史模型/粒子/航点决定及本目录五份迁入附件。

## 合并后的资料入口

本协议统一管理原本散落于 `src/modern` 的说明；原文件通过 `git mv` 迁入本目录，再翻译为中文。
不再在实现目录保留第二份事实。

| 专题 | 批次附件 |
| --- | --- |
| 粒子、HUD 与剩余分支 | [粒子审计](PARTICLE_PARITY_AUDIT.md) |
| 编队、Ro500 条件断言 | [编队协议](FORMATION_PARITY_PROTOCOL.md) |
| 提督桌、图鉴与雷达 | [桌面协议](DESK_PARITY_PROTOCOL.md) |
| 舰船背包 | [舰船背包协议](SHIP_INVENTORY_PARITY_PROTOCOL.md) |
| 变身背包 | [变身背包协议](MORPH_INVENTORY_PARITY_PROTOCOL.md) |

## 验收边界

19 项逐项结论、来源和视觉边界只记在 [批次决定](decision.md)；过程状态只记在 [状态](STATUS.md)。
激活入口必须以生产调用链为证据，并反查最可能的替代入口；存档恢复不算新激活。
测试命令、覆盖和通过条件统一见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)，本次输出归档于 [结果](results/README.md)。
跨批次偏离撤销只由 [D-013](../../MIGRATION-DECISIONS.md#d-013客户端行为严格还原撤销历史简化等价声明) 定义；历史文档保留原文并加撤销标记，不改写历史运行结果。
