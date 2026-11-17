# Batch Status

- 状态：`COMPLETE`
- 开始：2026-09-04
- 完成：2026-09-04
- 范围：class `38`、`39`、`44`、`46`、`47`；潜艇、潜艇姬、Yamato、Kaga 的 runtime/combat/model 联动域
- legacy：只读，未修改
- 当前阻塞：无

## Completed work

- 完成冻结入口的旧版/现代逐项对照；追加的直接依赖已记录在 `protocol.md`。
- 为 U-511、Ro-500、Submarine Hime、SSNH、Yamato、Kaga 保留旧版模型源和贴图路径，并补回选定旧版 scale-level 分支的现代运行时投影。
- 补回潜艇姬双导弹的旧版侧向发射坐标、miss 散射的 `nextFloat()` 语义和 projectile host/friendly-fire 边界。
- 保留 beam、area projectile 的旧版宿主排除、敌对同队免伤和 miss 不造成伤害边界。
- 补齐六个选定模型源的 `showEquip` 状态位及非静默 parser/bake 覆盖断言。
- 修正 SSNH GameTest 对 miss 后旧版目标散射分支的期望，避免把偶发合法 `ARC` 错判为现代差异。

## Verification

- `git diff --check`：通过。
- `compileJava`：通过，JDK `17.0.3.1`。
- `cleanProcessResources processResources`：通过；本批没有新增资源路径。
- `runGameTestServer`：通过，`All 261 required tests passed`。
- 客户端烟测：Java 17 启动、OpenGL 4.6、ShinColle 初始化、资源 reload、单人集成连接通过；未见 ShinColle 崩溃或模型/贴图 fallback 日志。
- 原生客户端窗口未暴露给当前自动化面板，进世界人工操作仍属于 V0，不记为已通过。

## Evidence links

- Protocol: [protocol.md](protocol.md)
- Decision: [decision.md](decision.md)
- Results: [results/ship-06.json](results/ship-06.json)
