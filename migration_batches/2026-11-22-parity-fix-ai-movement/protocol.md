# AI 移动与目标一致性：统一批次协议

## 问题与范围

将第1—5步的状态、跟随、阵型、守卫、目标、杂项AI与3D导航记录收束到本目录。第6步只整理文档，不变更实现、资源或测试，不重新执行 GameTest，也不将历史阶段失败误写为最终失败。

## 入口与事实归属

- 本批次结论：[decision.md](decision.md)；13项逐条证据：[evidence/audit-13.md](evidence/audit-13.md)。
- 历史步骤原始记录保存在 [evidence/](evidence/)，其中“不提交/不push/待复核”等为当时指令与检查点，不代表最终状态。
- 当前分支、工作区与任务只归 [CURRENT_STATE.md](../../CURRENT_STATE.md)；下一步只归 [ROADMAP.md](../../ROADMAP.md)。
- 跨批次约束及撤销索引归 [MIGRATION-DECISIONS.md](../../MIGRATION-DECISIONS.md)；命令、覆盖及通过条件归 [MIGRATION-TEST-MATRIX.md](../../MIGRATION-TEST-MATRIX.md)。本目录 STATUS 只归档已有运行输出与未验证边界。

## 旧版与现代入口

旧版：ShipStateHandler、CapaShipSavedValues、GuiShipInventory、ConfigHandler；EntityAIShipFollowOwner/Guarding/RangeTarget/AttackOnCollide/Wander/PickItem；AIHandler、TargetHelper、EventHandler；FormationHelper、BlockHelper、EntityHelper及 ai/path 六类。半重构缺失处使用既有0aad501对照记录，不修改 src/main。

现代：LegacyShipEntity、LegacyShipBehaviorCatalog、ship/goal、ship/path、LegacyFormationHelper、ShinColleConfig、菜单/GUI/命令同步与 GameplayParityGameTests。第6步不重新读取或编辑这些实现；依据已归档的第1—5步证据整理。

## 文档验收与非目标

旧散落位置全部移除、所有旧路径引用更新；13项审计均有结果与依据；历史偏离标记撤销；明确粒子替代、未接入实体、debug缺口和0aad501使用处；文档链接检查及diff检查通过；仅Markdown/已有证据JSON移动。保留原始证据，不伪造新测试结果，不晋级客户端或全舰种验收。用户授权中文提交并推送指定专题分支。
