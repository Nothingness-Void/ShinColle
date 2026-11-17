# Batch Decision

- 结论：`AUDIT_BASELINE_ONLY`
- 影响矩阵：[MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md)
- 关联模块：M4、M6、M8

## Facts established

- 已有 modern 目标筛选包含旧版的距离、创造模式、无敌和视线边界。
- hostile aircraft/Rensouhou 的起飞与命中提示已有敌方抑制分支。

## Code changes

- 无。

## No-change audits

- “旧版规则已对应”只代表静态审计基线，不等于用户报告的实机体验问题已关闭。

## Follow-up

- 若继续处理寻敌距离，先用旧版对应常量和可复现坐标创建新的 `target-range` 修复批次，再修改 modern 代码。
