# 当前状态

本文件只保存当前事实与交接快照；下一步只看 [ROADMAP.md](ROADMAP.md)，历史过程见 [迁移变更索引](MIGRATION-CHANGELOG.md)。

## 快照
- 分支：`devin/parity-fix-client`；本次文档收尾前实现提交：`606041e`。
- 工作区边界：现代实现只位于 `src/modern/java`、`src/modern/resources`；本次仅文档迁移与权威索引更新，不改生产逻辑。旧 `src/main/**` 继续只读。
- 当前任务：客户端还原成果归档与最终验收，入口为 [本批次](migration_batches/2026-11-22-parity-fix-client/)；不是全量视觉/粒子一致性收官。
- 最近证据：[最终结果入口](migration_batches/2026-11-22-parity-fix-client/results/README.md)；测试规模与覆盖以 [测试矩阵](MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁) 为准。
- 舰种身份与覆盖仍以 [舰种矩阵](MIGRATION-SHIP-MATRIX.md) 为准，本批次不新增舰种。
- 跨批次偏离已按 [D-013](MIGRATION-DECISIONS.md#d-013客户端行为严格还原撤销历史简化等价声明) 撤销；历史的“全量迁移全面收口”不再用作客户端完整一致性的结论。

## 阻塞与非目标
- 粒子、视觉与变身底层已知限制统一见 [批次决定](migration_batches/2026-11-22-parity-fix-client/decision.md#未完成与视觉边界)。
- 删除自创激活开关后，仓库内无可达的新激活入口；外部 Metamorph 空桥不等价于可用联动，证据见 [激活核查](migration_batches/2026-11-22-parity-fix-client/decision.md#变身激活入口核查)。
- 本次不恢复开关、不扩展粒子/联动，不启动额外客户端；提交推送后停止。
- 唯一下一步及进入条件：[ROADMAP.md](ROADMAP.md#当前指针)。
