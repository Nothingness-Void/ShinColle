# 后续队列

当前事实只看[CURRENT_STATE](CURRENT_STATE.md)；详细恢复只看[本轮结果](migration_batches/2026-11-22-riding-aircraft-formation/broad-wall-recovery-result.md)。

## NEXT（阻塞后交还lead，禁止无新证据重复门禁）

1. Neo recovery加载可见性：诊断捕获原体的added-to-level、PersistentEntitySectionManager visibility、区块FULL/ENTITY_TICKING信号；与真实player加载Nether作对照。现象/错误/已排除项见结果。不是UUID不匹配；加到160tick仍失败，不能猜纯IO延迟。
2. 保留真实/tp到初始未加载维度→/ship get复现、旧双方alive且age>200才去重、cache survivor、dead/200边界断言。不得删测、搬位置、削断言。先拿到可用加载信号再改夹具或生产路径；Forge-first/API最小适配。
3. 扩展D3节点诊断：NF07/08/02/01；优先NF08 vs NF10同bbox控制，取实际start/host goal/speed/每16tick路径索引与重算。当前canonical探针不能冒充最新VM场景。需要最新完整报告/路径日志，本地overflow只有摘要；直接附件URL401。
4. 决策独立mount LEV：faithful living-mount API移植需包含damage/equipment/tick/attributes审计；不得发明host共享效果，不因450预算猜增上限。未经确认不动余下矿车模型。
5. 相关源码稳定后跑受影响门禁；Neo full全绿再一次普通push。**Forge继续禁push**；不接管PR10 CI、不force。
6. 更新批次结果→目标矩阵/索引→核心快照→报告停界。指针/cranewait尚无新client视觉，不冒称通过。

音频任务已由batchF关闭（冻结1000触发/1001停止，legacy一致）；F1水飞通过；RingFly保持旧行为。PR11A/B与crane源码已完成提交，不重复实现。其余旧队列/非本轮项留在[STATUS](migration_batches/2026-11-22-riding-aircraft-formation/STATUS.md)，不自行扩项。
