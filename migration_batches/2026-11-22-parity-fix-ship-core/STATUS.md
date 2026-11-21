# 批次状态

- 模块：`ship-core`
- 分支：`devin/parity-fix-ship-core`
- 状态：代码已实现，编译通过；全量 GameTest 未全绿。
- 六项复核及旧版历史依据见 [decision.md](decision.md)。
- 验证命令：JDK 17，`sh gradlew compileJava --no-daemon`，随后 `sh gradlew runGameTestServer --no-daemon`。当前工作树的 `gradlew` 无执行权限，故通过 `sh` 调用，未更改文件权限。
- JDK `17.0.20.1`：编译复跑输出 `BUILD SUCCESSFUL in 1m 35s`。首次编译发现拾取目标中的语法错误，修正后通过。
- 首次全量 GameTest：287/299 通过；第一次复跑：294/299 通过；最后一次使用已下载的依赖运行 `sh gradlew compileJava runGameTestServer --offline --no-daemon`，编译通过，GameTest **298/299 通过、1 失败、0 跳过**。
- 最后一次输出：`299 GAME TESTS COMPLETE`、`1 required tests failed :(`、`BUILD FAILED in 35s`。唯一失败 `firstDestroyersAndRiKeepLegacyIdleSupplyAccounting`：`destroyer should pay legacy movement grudge and cumulative clamped morale cost: grudge=978, morale=3885`；前次复跑该处为 `morale=3875`，怨念同为 978。新增核心回归用例未列在失败清单中。
- 后续验证建议：将剩余用例的舰娘放到独立的测试结构内，记录两次手动 tick 前后的士气和战斗时间戳，排除共享测试世界对士气的干扰后再重跑。当前尚未确认差值来源。
- 菜单删减未做客户端目视检查。
