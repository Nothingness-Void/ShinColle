# 后续队列

当前事实：[CURRENT_STATE](CURRENT_STATE.md)；
[本轮完整证据](migration_batches/2026-11-23-pointer-water-move/decision.md)。

## NEXT：交还 Pointer 水上移动回归验收边界

1. 普通提交/push 两个新分支，报告 SHA、legacy/modern 根因行号和完整 gate 输出；
   不 push 主线/原 hold 分支，不创建 PR。
2. 玩家新包复测真实 Pointer 水面群体移动；本轮 headless 真实 AI tick 不冒称
   实际客户端点击/网络或 1.12 运行时回放。
3. 停止，不自动扩展旧 mount/LEVITATION、RingFly、渲染或其它队列。
   已独立复核的 radar 测试偶发不在此批修改，最终完整 gate 通过。

源码、资源、版本输入在通过 gate 后未变，不重跑同输入全量验证。
