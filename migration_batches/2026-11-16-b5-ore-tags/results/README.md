# B5-ORE-TAGS Results

## Environment

- Java: `C:\Program Files\Java\jdk-17.0.3.1`
- Date: `2026-11-16` (Asia/Tokyo)
- Worktree: `d:\文档\ShinColle`

## Observed checks

- `git diff --check`: exit `0` (`src/main/` 只读，无改动)
- `.\gradlew.bat cleanProcessResources processResources compileJava verifyItemModelCoverage runGameTestServer --no-daemon`: exit `0` (`BUILD SUCCESSFUL in 46s`)
- `runGameTestServer`: `94` tests run — **`All 94 required tests passed :)`** (`0` failed, `0` skipped)，服务端日志无配方或 Tag 解析错误
- T3 客户端烟测：本次未运行（按“不主动启动客户端”边界规则；`CraneTerminalScreen` 按钮坐标与 `gui_crane.png` UV 偏移已静态核对，交互渲染烟测留待客户端窗口/B1）

Machine-readable detail: [b5-ore-tags.json](b5-ore-tags.json)
