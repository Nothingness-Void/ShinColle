# 当前状态

当前任务：ship smoke 严格 legacy parity，独立 origin-based branches；
[协议](migration_batches/2026-11-23-ship-smoke/protocol.md)、
[当前批次状态](migration_batches/2026-11-23-ship-smoke/STATUS.md)、
[完整决定与证据](migration_batches/2026-11-23-ship-smoke/decision.md)。

- Forge：/home/ubuntu/repos/ShinColle-ship-smoke-120，
  devin/1795444967-ship-smoke-120，base origin/codex/mc-1.20.1 1b82b2c。
- Neo：/home/ubuntu/repos/ShinColle-ship-smoke-121，
  devin/1795444967-ship-smoke-121，base origin/mc-1.21.1 4380e97。
- 实现、五项 smoke GameTests、Neo VM minor/moderate/heavy 与 Kongou chimney/rise 截图已完成；
  最后代码更改后的双端 full build/GameTests 已绿。此前 radar flake 有独立 unchanged-base 失败证据，本批不改 radar。
- Pointer flee review 已先行独立提交、push 两个 Pointer branches；未合并进 smoke。
- 只改 modern 实现/测试/资源与文档；src/main/** 和旧 worktrees 不变。
  用户授权普通 push 两个 smoke feature branches，不创建 PR，不改版本/build/CI。
- Aircraft recurring death smoke 因现代立即 discard 的 lifecycle gap 已明确获准仅文档化；
  不加 corpse 生命周期或 one-shot 近似。Hostile/death 差异有纯测试，未单独客户端死亡截图。
- 原 mounts/LEVITATION、RingFly 等旧队列不扩项；
  [上一批证据](migration_batches/2026-11-22-review-followups/result.md) 保持历史记录。

唯一下一步：[ROADMAP](ROADMAP.md)。
