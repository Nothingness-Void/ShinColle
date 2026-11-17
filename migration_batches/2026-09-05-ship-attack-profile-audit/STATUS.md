# Batch Status

- 状态：`DONE`
- 开始：2026-09-05
- 完成：2026-09-05
- 结论：45 个已注册基础舰种和 64 个现代注册变体的轻攻、重攻、航空攻击开关均已按旧版有效行为完成对照；没有发现需要新增生产分派的差异。
- 关键边界：`EntityBattleshipTa` 的旧版 raw heavy flag 是继承默认值，但实体只实现 `attackEntityWithAmmo` 轻攻击入口；modern 保持 effective heavy disabled，不凭继承位新增重攻。
- hostile 变体：19 个 `+2000` Mob 变体遵循 `BasicEntityShipHostile.getAttackType` 的全通道行为；Hime 风格 hostile 变体继续使用各自原始旧版构造函数。
- 生产代码：无行为变更；仅补充 profile/catalog 语义注释，并新增全注册攻击通道矩阵 GameTest。
- 静态检查：`git diff --check` 通过；legacy 源码和资源仍未修改。
- 构建：Java 17 下 `compileJava` 和 `processResources` 通过。
- GameTest：Java 17 下 `runGameTestServer` 通过，`All 271 required tests passed`。
- 客户端：本批没有 renderer、model、particle、screen 或资源改动，不重复启动客户端；V0 进世界人工验收仍由客户端闸门单独负责。
- 下一步：返回 `SP-CLOSE / V0 T4`，只等待可操作原生客户端窗口完成单人世界人工验收。

详细对照决策见 [decision.md](decision.md)，机器可读结果见 [results/attack-profile-audit.json](results/attack-profile-audit.json)。
