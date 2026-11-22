# 后续队列

当前事实：[CURRENT_STATE](CURRENT_STATE.md)；
[本轮完整证据](migration_batches/2026-11-22-review-followups/result.md)。

## NEXT：交还 0.2.1-alpha 验收边界

1. 报告两个新分支、独立 A/B 提交与 release SHA、真实 build/GameTest 输出及主 jar 路径。
   用户已授权普通 push 这两个新分支；不 push 主线/原 hold 分支，不创建 PR。
2. 用户新包验收：ore matching 开启时 water 模板拒绝 lava，合法同材质 ingot 仍匹配；
   Forge 旧 drop 经 Neo 加载后 Desk radar 与 shipcleardrop 可见。服务端 NBT/命令测试
   不冒称完整实际旧世界或客户端画面验证。
3. 停止，不自动重开旧 mount/LEVITATION、RingFly、渲染或其它队列。

源码、资源、版本输入在通过 gate 后未变，不重跑同输入全量验证。
