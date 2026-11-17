# B6-INTERMOD-DETAILS Results

- 验证环境：Windows 11 / JDK `17.0.3.1` (`C:\Program Files\Java\jdk-17.0.3.1`)
- 执行命令：
  - `git diff --check`（exit `0`）
  - `git status -s -- src/main/`（exit `0`，空输出，旧版真源零改动）
  - `.\gradlew.bat build verifyItemModelCoverage runGameTestServer --no-daemon`（exit `0`，`BUILD SUCCESSFUL in 47s`）
- 结果：
  - `:compileJava`、`:build`、`:verifyItemModelCoverage` 全部通过；
  - `:runGameTestServer` 完成 **289 项 GameTest**：`All 289 required tests passed :)`（`289 passed / 0 failed / 0 skipped`，含新增 `b6SlashShipCommandsParity`、`b6DynamicLightBlocksSearchlightAndFlareDecay` 与 `b6SystemConfigPolyAsMnShipKillsAndRaidenLabel`）。
- 机器可读记录：[b6-closeout.json](b6-closeout.json)
