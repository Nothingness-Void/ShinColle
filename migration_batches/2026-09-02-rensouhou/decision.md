# Batch Decision

- 结论：`ACCEPTED`
- 影响矩阵：[MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md)
- 关联模块：M4、M5、M6、M8

## Facts established

- Rensouhou 的 Ta 模型变体使用集中 catalog 规则判断宿主状态。
- 视线受阻时保留追击/导航，但不越过旧版攻击门槛直接造成伤害。
- 攻击间隔集中由 combat helper 提供。

## Code changes

- 见 [STATUS.md](STATUS.md) 中列出的三个 modern 入口文件。

## No-change audits

- 本批次没有扩大到其他舰种或恢复完整旧版多人路径。

## Follow-up

- 下一批必须从 `ROADMAP.md` 和矩阵的 `NEXT` 行选择，不重复阅读本批次已冻结的旧版入口。
