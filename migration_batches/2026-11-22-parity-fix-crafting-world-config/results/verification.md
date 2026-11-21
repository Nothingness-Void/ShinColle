# Verification

环境：JDK `17.0.20.1`（Ubuntu），`bash ./gradlew ... --no-daemon`（仓库内 `gradlew` 无可执行位，用 `bash` 调用）。

| 命令 | 结果 |
| --- | --- |
| `./gradlew compileJava` | `BUILD SUCCESSFUL` |
| `./gradlew runGameTestServer`（本分支，两次） | `297` 项运行，`296` 通过 / `1` 失败：`passiveAiStopsAutomaticRangeTargeting` |
| `./gradlew runGameTestServer`（基线 `origin/codex/mc-1.20.1` `8f1d2fd`，未改动） | `298` 项运行，同样只有 `passiveAiStopsAutomaticRangeTargeting` 失败 |

- 测试总数 `298 → 297`：删除旧版不存在的 `progressionAdvancementsLoadAndFriendlyShipTriggerAwards`。
- `passiveAiStopsAutomaticRangeTargeting` 在未改动的基线上同样失败，与本批改动无关，不在本模块范围内修复。
- 本批修改的测试 `chestLootInjectorUsesLegacyConfigLootDefaults`、`worldgenResourcesKeepLegacyPolymetalPlacement` 及全部 crafting/hostile 相关测试均通过。
- `git diff --check` 无输出；`src/main/` 零改动。
