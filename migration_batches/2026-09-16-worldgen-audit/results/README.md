# Worldgen Audit Results

## Environment

- Java: `C:\Program Files\Java\jdk-17.0.3.1`
- Date: `2026-11-16` (Asia/Tokyo; re-verified after 2026-09-16 gravel resource fix)
- Worktree: `d:\文档\ShinColle`

## Observed checks

- `git diff --check`: exit `0`（legacy `src/main/` 无修改）
- `.\gradlew.bat compileJava processResources`: exit `0` (`UP-TO-DATE` against current tree; 2026-09-16 build passed after `polymetal_gravel.json` edit)
- `.\gradlew.bat verifyItemModelCoverage`: exit `0`
- Output check: `build/resources/main/data/shincolle/worldgen/configured_feature/polymetal_gravel.json` contains only `minecraft:stone` and `minecraft:gravel` in `target.blocks`.
- `.\gradlew.bat runGameTestServer`: 经 `GAMETEST-REPAIR-B4` 同步 `worldgenResourcesKeepLegacyPolymetalPlacement` 断言后，2026-11-16 全量运行 `All 91 required tests passed :)`（exit `0`）。

Machine-readable detail: [worldgen-audit.json](worldgen-audit.json)
