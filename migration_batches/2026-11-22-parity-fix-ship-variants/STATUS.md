# 舰种个体行为验证

使用 OpenJDK 17.0.20.1。执行命令和覆盖范围见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md)。

## 最终验证

- `compileJava` 成功，耗时 7 秒。
- `runGameTestServer` 成功，耗时 40 秒。通过 320/320，失败 0。
- `git diff --check` 通过，`src/main/java` 与 `src/main/resources` 相对起始分支无差异。

```text
========= 320 GAME TESTS COMPLETE ======================
All 320 required tests passed :)
BUILD SUCCESSFUL in 40s
```

## 中间验证

- 首轮编译通过，GameTest 通过 319/320。唯一失败为 `rensouhouOnSightChaseStopsBlockedPursuit`。原夹具混用固定世界高度和测试结构相对高度，修正为相同的结构相对坐标，并将墙扩至两格高以遮住视线。
- 第二轮编译通过，GameTest 通过 319/320。唯一失败为 `hostileScaleUsesSpawnAndSaveDataWithLegacyExperience`。重炮断言误用带敌对偏移的 class ID 查属性表，已修正为对应基础舰种的 class ID。连装炮夹具与海上 Boss 进度断言在这一轮通过。
