# 后续队列

当前事实：[CURRENT_STATE](CURRENT_STATE.md)。

## NEXT：完成并交还 audit parity 0.2.3-alpha

1. A01–A09 分 finding commit，逐项 Forge first then Neo；S01 只修复完整旧版分派已证实的差异。
2. 完整 JDK17/21 build runGameTestServer 门禁；不重复未改变输入的通过门禁。
3. 记录 [STATUS](migration_batches/2026-11-24-audit-parity/STATUS.md)，更新矩阵/变更索引，普通 push 两个 feature branches，复制并核验指定 release jars。
4. 报告 pushed SHAs、逐 finding legacy evidence/commits、S01 verdict 和真实 gate 输出，然后停止；不扩大至历史 backlog。
