> 历史步骤证据：保留当时的指令、失败和待办语境；最终结论与后续交付以[统一批次决定](../../decision.md)为准。

# 状态

第4步实现及受影响测试已完成，最终315/315通过；按用户要求仅本地中文提交，不push。失败与修正见decision.md。

最后一次检查：

```text
BUILD SUCCESSFUL in 1m 26s
1 actionable task: 1 executed

========= 315 GAME TESTS COMPLETE ======================
All 315 required tests passed :)
BUILD SUCCESSFUL in 3m 17s
```

新增Selector、混合列表、碰撞近战、游荡及更新的拾取断言均通过，旧特殊攻击回归已修复。

诊断实证：目标Goal=2，mock玩家可选且非创造/观察者/无敌；搜索盒实际有5个可选候选（多个同坐标遗留舰），选中了另一艘距离2的舰。只修fixture：清旧舰、断言确定的2/4个候选集合、finally按引用清理（包括移出搜索盒者），保留失败诊断。生产规则保持候选>2才从最近3个随机。隔离后的检查亦是在复用世界上执行，清理补齐前后均315/315通过。

命令：

```sh
bash ./gradlew compileJava --no-daemon
bash ./gradlew runGameTestServer --no-daemon
```

日志：`/home/ubuntu/shincolle-ai-step4-compile.log`、`/home/ubuntu/shincolle-ai-step4-gametest.log`；诊断失败证据：`/home/ubuntu/shincolle-ai-step4-diagnostic.log`。diff --check及src/main零差异检查通过。未启动客户端，实际游荡/近战运动与音量听感未验证；非LivingEntity目标API边界未扩大。
