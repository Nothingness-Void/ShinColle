# 当前状态

当前任务：PR17/18 smoke review follow-ups 已完成；
[两项 finding、legacy 证据及最终门禁](migration_batches/2026-11-23-ship-smoke/review-followups.md)。

- Forge：/home/ubuntu/repos/ShinColle-ship-smoke-120，
  devin/1795444967-ship-smoke-120。起始 local/remote e34f5de 一致且 clean。
- Neo：/home/ubuntu/repos/ShinColle-ship-smoke-121，
  devin/1795444967-ship-smoke-121。起始 local/remote ae383c4 一致且 clean。
- Finding1：friendly 六驱四舰恢复普通 type20 / vanilla limiter；hostile 保留 type43。
- Finding2：hostile Hiei chimney 固定 scale1、X=-0.6、Y=1.17；
  其它 hostile chimney 的 legacy scaling 保持不变。两项修复在每端分开提交。
- 最终 full build/GameTests：Forge381/381、Neo390/390。最后源码更改后无新增
  源码/资源输入；此前 Forge crane actual=null 间歇失败已留原始证据，未改 crane。
- 本轮未重新客户端视觉验证；初次 smoke 截图不冒充 type20 limiter/Hiei review 画面。
- 仅 modern 与文档；src/main/**、版本、build/CI、其它 worktrees 均不改。
  用户授权每个现有 smoke branch 一次普通 push，不创建 PR 或 push 主线。
- Aircraft death lifecycle 明确保留 gap；Pointer、mount/LivingEntity、LEVITATION、
  RingFly 及其它历史队列不扩项。[原 smoke 决定](migration_batches/2026-11-23-ship-smoke/decision.md)。

唯一下一步：[ROADMAP](ROADMAP.md)。
