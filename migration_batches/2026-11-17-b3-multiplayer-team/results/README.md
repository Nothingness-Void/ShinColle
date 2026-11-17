# B3-MULTIPLAYER-TEAM Results

## Part 2（PVP/友伤、交互权限与多人 Desk Radar 同步收口）— 2026-11-18

- JDK：`17.0.3.1`（`C:\Program Files\Java\jdk-17.0.3.1`）
- 规范与只读检查：`git diff --check` 与 `git status -s -- src/main/` 通过（exit `0`，`src/main/` 无改动）
- 验证命令：
  - `.\gradlew.bat cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon`（编译 + 构建 + 模型覆盖验证，另在独立复跑 `.\gradlew.bat runGameTestServer --no-daemon` 全绿收口）
- 执行结果：exit `0`（`BUILD SUCCESSFUL in 40s`）
- GameTest 计数：**`All 286 required tests passed :)`**（`286 passed / 0 failed / 0 skipped`，在 B3 Part 1 `284` 项基线上新增 2 项 B3 Part 2 确定性测试）：
  - `b3Part2FriendlyFirePvpModesAndCommandRevengeTargeting`
  - `b3Part2OwnerPaperKaitaiHammerAndDeskRadarSync`
- 结构化结果文件：[b3-part2.json](b3-part2.json)

## Part 1（核心队伍生命周期、跨维度广播与方块同盟/OP 权限对齐）— 2026-11-17

- JDK：`17.0.3.1`（`C:\Program Files\Java\jdk-17.0.3.1`）
- 规范与只读检查：`git diff --check` 与 `git status -s -- src/main/` 通过（exit `0`，`src/main/` 无改动）
- 验证命令：
  `.\gradlew.bat cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon`
- 执行结果：exit `0`（`BUILD SUCCESSFUL in 1m`）
- GameTest 计数：**`All 284 required tests passed :)`**（`284 passed / 0 failed / 0 skipped`）：
  - `b3TeamLifecycleCooldownAndNameValidation`
  - `b3TeamAllyBannedExistenceAndBlockOwnershipPermissions`
- 结构化结果文件：[b3-part1.json](b3-part1.json)
