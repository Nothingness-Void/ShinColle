# 后续队列

当前事实：[CURRENT_STATE](CURRENT_STATE.md)；
[PR17/18 证据](migration_batches/2026-11-23-ship-smoke/review-followups.md)。

## NEXT：交还 smoke review 验收边界

1. 每端两项独立修复提交与门禁文档一起，一次普通 push 到现有 smoke feature branch。
2. 报告起始 HEAD 核对、每项 finding 的真伪/legacy 行号、独立 SHA，
   Forge381/Neo390 全量通过原文；不把旧截图冒充本轮视觉验证。
3. 保留已有 crane/radar 间歇断言历史，不声称本批修复其原因。
4. 停止；不新增 PR、版本/build/CI、飞机生命周期或其它历史任务。
   无输入变化，不重复已通过的全量检查。
