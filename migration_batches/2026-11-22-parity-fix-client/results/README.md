# 最终验收结果

## 最终结果
- 编译退出 0；任务为 UP-TO-DATE，6 秒，见 [编译原文尾部](compile-summary.log)。
- 最后一次完整回归退出 0；301/301 必需测试通过，无失败名，38 秒，见 [最终回归摘要](gametest-final-summary.log)。
- 本次不启动客户端；历史截图和未验项目只见 [批次决定](../decision.md#未完成与视觉边界)。
- 测试命令/通过条件只见 [测试矩阵](../../../MIGRATION-TEST-MATRIX.md#客户端还原收尾门禁)。

## 首次失败保留，不以重跑抹去
本次首次回归退出 1：301 个必需测试中 1 项失败，即 300/301。
原文见 [首次回归摘要](gametest-attempt-1-summary.log)：
`autocombatrationuseslegacytickandfoodrules` 的
`auto combat ration should wait for the legacy 128-tick boundary`。
没有修改生产实现、测试、随机种子或运行配置；仅完整复跑失败命令一次。
随后通过不能证明根因已修复，根因仍未确认。
历史 `eighthfiveshiphookskeeplegacyhostiledestroyerparity` 瞬时失败也仍未确认根因。

## 边界与归档
- 实现保持 `606041e`：本次 Java、JSON、Python 无差异，只更新 Markdown 和本目录日志摘要。
- 五份说明通过 git mv 从 src/modern 迁入本批次，中文化并由 protocol.md 统一导航。
- 已执行 `git diff origin/devin/parity-fix-menu-network-command --stat -- src/main`，退出 0、无输出。随后只有文档/摘要编辑，未改旧源码或资源。
- 原实现目录文档路径已定向检索，无残余引用；批次/核心文档本地链接及 01–19 编号检查通过。
- 本次编译完整日志：`/home/ubuntu/shincolle-client-closeout-compile.log`。
- 首次回归完整日志：`/home/ubuntu/shincolle-client-closeout-gametest.log`。
- 最后回归完整日志：`/home/ubuntu/shincolle-client-closeout-gametest-retry.log`，SHA-256：
  `6ba7631eb78989e96534a3af2bbe61695d1ea04deb332d60df35351315310101`。
