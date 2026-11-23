# 后续队列

当前事实：[CURRENT_STATE](CURRENT_STATE.md)；
[smoke 状态](migration_batches/2026-11-23-ship-smoke/STATUS.md)；
[完整证据](migration_batches/2026-11-23-ship-smoke/decision.md)。

## NEXT：交还 ship smoke 验收边界

1. 报告独立 smoke branches/提交与普通 push、真实双端 full build/GameTest 输出、
   Neo VM minor/moderate/heavy 与 Kongou chimney/rise 截图。不得把 earlier gate 当作 final repeat。
2. 保留已有 radar 断言的 unchanged-base flake 证据；双端最终 smoke 门禁已绿，
   但不冒称修复 radar。若将来复现，由 lead 单开 fixture 调查，不在本批扩项。
3. 保留 aircraft death lifecycle 文档 gap；不扩展飞机、mount/LivingEntity、LEVITATION、
   RingFly、Pointer、数值或其它历史队列。不创建 PR、改版本或 push 主线。
4. 最后代码更改后执行用户要求的完整双端门禁；随后只做证据/控制面文档。
   没有输入变化，不重复已通过的检查。
