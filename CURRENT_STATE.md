# 当前状态

本文件只保存当前快照；唯一下一步见[ROADMAP](ROADMAP.md)。
权威恢复入口：[宽墙/recovery/PR11/crane结果](migration_batches/2026-11-22-riding-aircraft-formation/broad-wall-recovery-result.md)。

- Forge工作区：/home/ubuntu/repos/ShinColle-1.20-render-backport，分支devin/parity-fix-integration。源码9a34c55，本地ONLY，push hold继续。
- Neo工作区：/home/ubuntu/repos/ShinColle，分支devin/mc-1.21.1-neoforge。源码837edb5，本地ONLY；remote仍77bd8ba。未push，普通merge，无force。
- 当前改动：PR11A协议5145edf、Bfall-through6c5214c；recovery旧>200tick去重复470f762；crane空罐等待9a34c55。Neo已merge/API适配。
- 门禁：Forge build+full368/368绿；Neo build阶段完成，但full376中1个新recovery加载可见性GT失败。**Neo不绿、不可push**。新断言未削弱，未移场。
- 阻塞：Neo捕获到正确UUID/UID且removed=false的Nether原体，但强制区块/succeedWhen160tick后getEntity(id)仍null；尚未到去重断言。需诊断实体section visibility/added-to-level/FULL状态，不继续猜加delay。
- 宽墙：精确卡点参数8组合均450节点后null，legacy3step/无飞行也如此；完整旧runtime未验证。新NF07/08/02/01仍open，NF08与通过的NF10同bbox；对照探针表见权威结果。独立LEV需完整living类型API范围决策，不偷偷代理host效果。
- 新batchE crane：历史0aad501支持null-fluid wildcard，与当前src/main后期helper不同；双端源码已按历史修复并加GT。
- batchF确认音频仅冻结1000饱和、1001停止，legacy一致，跳过后续音频任务；F1水飞live通过。RingFly明确保持legacy。Shimakaze+0.14/残余穿模不改。
- src/main/**只读且本轮未变；PR10 CI未接管；旧mayfly迁移不动；Pointer/crane交互未做新视觉核验。
- 私有client236617冻结；其他窗口56623113未动。日志、完整checks/SHAs、证据与旧队列入口均见权威结果与[批次STATUS](migration_batches/2026-11-22-riding-aircraft-formation/STATUS.md)。

总体迁移矩阵：[舰种](MIGRATION-SHIP-MATRIX.md) / [测试](MIGRATION-TEST-MATRIX.md)；历史入口：[变更索引](MIGRATION-CHANGELOG.md)。
