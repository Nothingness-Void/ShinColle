# 构建与回归证据

## 执行信息

- 记录日期：2026-11-22；环境：JDK 17、Minecraft 1.20.1、Forge 47。
- 最终代码提交：`79b4527`；本次文档收口不重复启动构建或服务器。
- 第一轮 `./gradlew compileJava` 因 wrapper 无执行权限失败，改用等价的 `bash ./gradlew compileJava`，没有改文件权限。

## 最终构建

命令：`bash ./gradlew compileJava`。退出状态：0。此前执行输出摘录：

```text
> Task :compileJava

BUILD SUCCESSFUL in 1m 29s
1 actionable task: 1 executed
```

## 最终 GameTest

命令：`bash ./gradlew runGameTestServer > /home/ubuntu/gt-final.log 2>&1`。
完整日志：[本机最终日志](/home/ubuntu/gt-final.log)。退出状态：非零，Gradle 构建失败，不能报告全绿。

```text
gameplayparitygametests.passiveaistopsautomaticrangetargeting failed! re-enabling automatic targeting should restore the legacy range target goal
========= 302 GAME TESTS COMPLETE ======================
1 required tests failed :(
   - gameplayparitygametests.passiveaistopsautomaticrangetargeting

BUILD FAILED in 3m 14s
```

统计为 **301／302 通过**。唯一失败是用户确认的基线失败，不在本批修复范围。新增／改写覆盖见 [测试矩阵](../../../MIGRATION-TEST-MATRIX.md)。

## 独立基线对照

用户独立在 `/home/ubuntu/baseline` 运行并确认是未改动的 `origin/codex/mc-1.20.1`；本批不进入或修改该目录。收口时只核读目录外的 [基线日志](/home/ubuntu/baseline-gametest.log)，其结果支持用户提供的失败清单；基线工作树身份未由本批另行核验，不伪称自行复跑。

```text
gameplayparitygametests.passiveaistopsautomaticrangetargeting failed! re-enabling automatic targeting should restore the legacy range target goal
gameplayparitygametests.round2hurtimmunitiesvoidrescuegoddessandresistancematchlegacy failed! DAMAGE_RESISTANCE lv2 should mitigate projectile damage by 40% (taken=13.058289)
========= 298 GAME TESTS COMPLETE ======================
2 required tests failed :(
   - gameplayparitygametests.passiveaistopsautomaticrangetargeting
   - gameplayparitygametests.round2hurtimmunitiesvoidrescuegoddessandresistancematchlegacy
```

- 基线 296／298 通过；最终唯一失败在该日志同样出现，且错误文本一致。
- `round2hurtimmunitiesvoidrescuegoddessandresistancematchlegacy` 在本次 302 项中通过。与独立基线结果不同，疑似不稳定，未定位其原因，不宣称为本批修复。
- 早期第四轮回归还暴露起重机完成时跟随距离被 4 的下限截断；恢复旧合法值 2 后最终 `craneCycleWaitsForInventoryThenCompletesEmptyWork` 通过。不是通过放宽测试掩盖问题。

## 静态与未执行检查

- 实现提交前 `git diff --check` 通过，旧版目录零差异；文档提交前再检查文档差异、批次链接与 27 行结论。
- 没有执行客户端烟测、视觉检查或本批独立资源／模型门检查；Java 编译和服务端 GameTest 不能代替这些检查。
- Dust 粒子、变身航空攻击接线等限制见 [未验证项](../decision.md#未验证项与后续边界)。

<a id="integration-merge"></a>

## integration 分支合并验证

- 保留此前各专题修复，FollowMin/FollowMax 统一 FMin/FMax，起重机完成后的 2 可同步、保存及重载；删除 RouteEnergyAccess，不恢复 ShipFuel。
- 起重机四项夹具移入 guard 结构内侧 `(16,160,16)`，箱子和后继节点同样在结构内；失败信息保留预期与实际作业舰 ID/UUID。
- 对照验证：仅移入内侧时 327/327 通过；恢复边界坐标 `(1,160,1)` 后两项失败重现。液体测试预期 ID 783，实际 ID 774（位置相同、UUID 不同）；作业计时测试预期 ID 891，实际 ID 855（位置相同、UUID 不同）。液体缓冲减少 640、预期舰库存仍为 0，是起重机选中另一艘合法候选舰，而不是证明液体凭空消失。隔离夹具消除这些候选干扰；其最初产生于哪项先前测试未进一步追踪。
- 另经双方与旧版 `TileEntityCrane.checkCraningShip/setShipData` 对照发现关联对接隐患：扫描选中的 state=2 舰可能被客户端侧独占 setter 拒绝，但工作循环仍持有该候选舰。恢复旧版“扫描选择即关联”路径，菜单与工作循环使用同一艘舰；在既有生命周期测试中补入“旧关联退回等待、另有 state=2 候选”断言。该隐患不冒充上述边界失败的根因。
- CraneTime 只有一个递增点；等待初始值 20，工作每 tick 加一，完成清零。保留工作舰、菜单拆分 16 位同步、无舰、完成及离开路线断言。
- 变身口粮使用喂食前 buffed GRUDGE 快照，保持旧版属性缓存语义；不因加士气即时改用新倍率。

最终命令（JDK 17）：`bash ./gradlew compileJava runGameTestServer --no-daemon`

```text
========= 327 GAME TESTS COMPLETE ======================
All 327 required tests passed :)
BUILD SUCCESSFUL in 3m 22s
11 actionable tasks: 4 executed, 7 up-to-date
```

客户端视觉仍未验证；本次只创建本地 merge commit，不推送。
