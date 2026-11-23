# 当前状态

当前任务：将已合并 PR15/16 的 origin bases 正常 merge 到两个 smoke branches，
已保留 Pointer 与 smoke/review；统一 0.2.2-alpha、双端完整门禁已绿，主 jars 已构建并验证元数据。
[本轮整合入口](migration_batches/2026-11-23-ship-smoke/integration-release.md)。

- Forge worktree：/home/ubuntu/repos/ShinColle-ship-smoke-120，
  devin/1795444967-ship-smoke-120；merge origin/codex/mc-1.20.1 0f6d350。
- Neo worktree：/home/ubuntu/repos/ShinColle-ship-smoke-121，
  devin/1795444967-ship-smoke-121；merge origin/mc-1.21.1 563779b。
- 五个冲突均为 docs；矩阵/变更索引保留双方条目，CURRENT/ROADMAP 汇总两条工作线。
  源码无冲突：Pointer floating TARGET、flee MOVE/LOOK/JUMP 和真实 water/land tests
  与 smoke HP/death/type20/type43、Hiei 固定 chimney 参数全部保留。
- 用户明确授权 gradle.properties mod_version=0.2.2-alpha；
  normal merge/commit/push，不 rebase/force push，不 push 主线，不创建 PR。
- 最后源码/版本输入后 full build+GameTests：Forge386/386，Neo395/395，均成功。
  Neo 首轮仅历史 radar 断言失败，一次完整重跑通过；未改 radar。完整输出见本轮入口。
  后续只有文档；不重复同输入构建。最终普通 push 与 release 复制 SHA/哈希由交接报告记录。
  目标 /home/ubuntu/release/shincolle-0.2.2-alpha-mc1.20.1-forge.jar、
  shincolle-0.2.2-alpha-mc1.21.1-neoforge.jar。
- src/main/**、其它 worktrees、build/CI 及其它数值/机制不改。Aircraft lifecycle、
  LEVITATION 等 gap 保留；本轮无新视觉验证。
- 历史证据：[Pointer](migration_batches/2026-11-23-pointer-water-move/decision.md)、
  [flee review](migration_batches/2026-11-23-pointer-water-move/flee-review.md)、
  [smoke review](migration_batches/2026-11-23-ship-smoke/review-followups.md)。

唯一下一步：[ROADMAP](ROADMAP.md)。
