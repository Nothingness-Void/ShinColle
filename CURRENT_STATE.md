# 当前状态

本文件只保存当前快照；唯一下一步见[ROADMAP](ROADMAP.md)。
权威恢复入口：[Pointer client ownership review](migration_batches/2026-11-22-riding-aircraft-formation/pointer-owner-result.md)；上一轮 [section/profile](migration_batches/2026-11-22-riding-aircraft-formation/section-readiness-profile-result.md)。

- Forge：/home/ubuntu/repos/ShinColle-1.20-render-backport，devin/parity-fix-integration；源码检查点 96336ca，仍本地 ONLY，push hold 继续。
- Neo：/home/ubuntu/repos/ShinColle，devin/mc-1.21.1-neoforge；源码检查点 cd06bdc，经普通 merge 包含 Forge 修复。允许全绿后普通 push，最终同步 SHA 见交接报告；此前 PR11/recovery/crane 全部保留。
- Pointer 的 client consumption 由未同步 numeric owner cache 改读已有 synced owner UUID；服务端授权不变。新增零 numeric UID / UUID 有效的 ship、mount、teammate sneak 回归。
- 双端 build + full GameTest 已全绿：Forge 370，Neo 378。合并后 Neo src/modern 与已测试的 cd06bdc 完全相同；后续仅文档。中间一次已知 crane 间歇失败见批次结果，不宣称修好该间歇性。
- recovery 已解除门禁阻塞：已注册但 hidden 的 destination section 在夹具中明确提升 ENTITY_TICKING；全部原有去重断言保留。真正玩家加载 Nether 的私有客户端对照自然可见且 ticking；详情只看权威结果。
- 新实际五 profile trace 找到并修复高墙顶 waypoint 缺少 legacy +0.1 的渐近卡点；保留全 bbox 固体安全检查。完整旧 VM 场景的 NF10 通过/其余失败分组仍待新包复测；不把控制场景当作原世界逐项验收。
- LEVITATION 明确保留缺口：不移植 LivingEntity，不代理 host 效果，不改 450 节点预算。旧精确 snapshot 的 bounded-search null 与本次非空路径墙顶卡点区分。
- RingFly 保持 legacy；音频已由 batch F 关闭；F1 水飞通过；Shimakaze +0.14 与残余模型穿模不变。
- src/main/** 只读且未改；Forge 禁 push、禁止 force、PR10 CI 未接管、旧 mayfly 迁移不动。Pointer/crane 无新视觉复测；本轮轨迹是原生实体数值验证，不是渲染截图验收。
- 私有 client 236617 已回原位置并冻结；其他窗口 56623113 未动。旧队列保持[批次 STATUS](migration_batches/2026-11-22-riding-aircraft-formation/STATUS.md)，不自行扩项。

总体矩阵：[舰种](MIGRATION-SHIP-MATRIX.md) / [测试](MIGRATION-TEST-MATRIX.md)；历史入口：[变更索引](MIGRATION-CHANGELOG.md)。
