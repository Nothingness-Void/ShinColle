# B2-PACKET-PARITY Results

## Verification Commands & Environment

- JDK：`C:\Program Files\Java\jdk-17.0.3.1` (`Java 17.0.3.1+2-LTS-6`)
- Legacy 只读检查：`git status -s -- src/main/` -> 空输出（exit `0`）
- 格式检查：`git diff --check` -> 仅 CRLF/LF 提示，exit `0`
- 构建、模型校验与全量 GameTest：
  ```powershell
  $env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.3.1'
  $env:Path="$env:JAVA_HOME\bin;$env:Path"
  .\gradlew.bat cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon
  ```
- 退出状态：`BUILD SUCCESSFUL in 1m 5s`（exit `0`）
- 测试结果：**`All 282 required tests passed :)`**（`282` passed / `0` failed / `0` skipped）
- 机器可读逐 PID 与测试汇总：[b2-packet-parity.json](b2-packet-parity.json)
