# 后续队列

当前事实只看[CURRENT_STATE](CURRENT_STATE.md)；本轮细节只看[Pointer ownership 结果](migration_batches/2026-11-22-riding-aircraft-formation/pointer-owner-result.md)。

## NEXT：报告本轮并停在用户新包复测边界

1. 双端已绿；Neo 普通 push，包含 PR11A/B、recovery/crane 和本轮修复；Forge 继续本地 hold，不 force，不接管 PR10 CI。
   本轮先交还 Pointer 新包复测：正常远距选自己舰娘/坐骑、蹲点当前队员不切模式、外舰/非队员 fall-through。当前仅 source/client-data-shaped GT 验证，不冒称实际 LocalPlayer 点击/渲染验收。
2. 用户新包复测 NF07/08/02/01 与 NF10 的原宽墙场景。进入条件：新的实际 VM 结果或明确授权；记录 start/goal/path nodes/index/repath，区分非空路径墙顶渐近与 450 节点 null。退出条件：原场景给出可复现通过/失败证据，不靠控制场景推断全项通过。
3. LEVITATION 仅保留已知 gap；用户明确禁止当前 LivingEntity 移植。不改 host 效果共享、搜索预算、RingFly 或矿车模型。音频与 F1 已关闭，不重复测试。
4. 批次结果 → 目标矩阵/索引 → CURRENT_STATE/本路线 → 报告，停止。其它原队列见[STATUS](migration_batches/2026-11-22-riding-aircraft-formation/STATUS.md)，未经授权不扩项。

门禁进入条件：相关源码确实改变；命令/范围只看[测试矩阵](MIGRATION-TEST-MATRIX.md)。源码输入未变不重跑，最后报告精确 SHA 与实际输出。
