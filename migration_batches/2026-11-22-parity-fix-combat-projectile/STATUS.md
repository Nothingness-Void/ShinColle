# 验证结果

- 当前分支与交接状态统一见 [CURRENT_STATE.md](../../CURRENT_STATE.md#当前任务)。
- JDK：Ubuntu OpenJDK `17.0.20.1`。
- 仓库脚本没有执行位，使用 `bash ./gradlew`，未修改权限或构建配置。
- `src/main/` 零修改；实现只修改 `src/modern/java`；`git diff --check` 无输出，退出码 0。

## 最终编译

命令：`bash ./gradlew compileJava`

```text
> Task :compileJava

BUILD SUCCESSFUL in 1m 28s
1 actionable task: 1 executed
```

## 最终全量 GameTest

命令：`bash ./gradlew runGameTestServer`

```text
[17:28:09] [Server thread/INFO] [minecraft/GameTestServer]: ========= 302 GAME TESTS COMPLETE ======================
[17:28:09] [Server thread/INFO] [minecraft/GameTestServer]: All 302 required tests passed :)
BUILD SUCCESSFUL in 3m 13s
```

通过 302/302，失败 0。新增 3 项测试；药水测试夹具的两次失败及修复原因见 `protocol.md`。

完整日志：`/home/ubuntu/combat-projectile-compile.log`、`/home/ubuntu/combat-projectile-gametest-final.log`。

## 未验证

未运行客户端视觉烟测；视觉光源保留的客户端效果尚未目视确认。舰载机重攻击发射路径已恢复并参与编译，但新增 GameTest 未单独覆盖该路径的飞行与落点；全量既有航空测试通过不等于该路径的独立端到端验证。
