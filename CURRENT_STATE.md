# 当前状态

当前任务：merged PR11 的 ore-category / ForgeData review follow-ups 已完成，
统一 mod_version=0.2.1-alpha。权威证据与边界：
[本轮结果](migration_batches/2026-11-22-review-followups/result.md)。

- 新 Forge worktree：/home/ubuntu/repos/ShinColle-review-followups-120；
  devin/1795364114-review-followups-120，基于 origin/codex/mc-1.20.1 当前 353c67c。
- 新 Neo worktree：/home/ubuntu/repos/ShinColle-review-followups-121；
  devin/1795364114-review-followups-121，基于 origin/mc-1.21.1 的 6ccd51a。
- 两端 build + full GameTests 已绿。A/B 分开提交；Forge B 已验证平台原生
  ForgeData 读取正确，只加回归测试，无多余迁移代码。最终 release/push SHA 见交接。
- 本轮用户授权只允许普通 push 上述两个新 feature branches，不创建 PR，
  不修改/推送原 devin/parity-fix-integration 或其它既有 worktrees。
- src/main/** 只读且未改；仅 modern 实现/测试、文档及用户指定版本属性改变。
  UI/真实旧世界升级未视觉验证；当前断言为真实 entity NBT、radar payload 和命令路径。
- 原宽墙 VM 复测、LEVITATION gap、RingFly strict parity、Shimakaze 残余穿模等
  旧队列不扩项；[此前入口](migration_batches/2026-11-22-riding-aircraft-formation/pointer-owner-result.md)。

唯一下一步：[ROADMAP](ROADMAP.md)。
