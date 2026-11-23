# 当前状态

当前任务：Pointer 水上部分舰船不移动的迁移回归已复现并作最小 mutex 修复。
权威根因、历史行号、失败/对照及双端完整 gate：
[本轮结果](migration_batches/2026-11-23-pointer-water-move/decision.md)。

- Forge 新工作区：/home/ubuntu/repos/ShinColle-pointer-water-move-120；
  devin/1795443831-pointer-water-move-120，基于指定 1b82b2c。
- Neo 新工作区：/home/ubuntu/repos/ShinColle-pointer-water-move-121；
  devin/1795443831-pointer-water-move-121，基于指定 4380e97。
- 两端生产改动仅 floating goal 的独立 legacy mutex；新增真实六舰水/陆移动、
  历史 mutex oracle、空选区及 dry/sitting 断言。完整 build + GameTests 已绿。
- 本轮用户授权普通 push 以上两个新分支；不创建 PR、不操作旧 hold 分支。
  既有工作区未动。额外只读 detached baseline 工作区用于复核既有 radar 测试偶发。
- src/main/** 未改；不改版本、优先级、浮力、速度、导航范围或负 Y 适配。
  无客户端视觉验证，未运行实际 1.12 客户端或原玩家存档。
- 旧 mount/LEVITATION、RingFly、渲染等队列不扩项。

唯一下一步：[ROADMAP](ROADMAP.md)。
