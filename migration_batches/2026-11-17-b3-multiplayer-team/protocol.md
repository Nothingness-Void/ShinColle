# Frozen Protocol — B3-MULTIPLAYER-TEAM

## Legacy references

只列出为回答本批次问题必须读取的旧版文件：

- `src/main/java/com/lulan/shincolle/team/TeamData.java`
- `src/main/java/com/lulan/shincolle/utility/TeamHelper.java`
- `src/main/java/com/lulan/shincolle/proxy/ServerProxy.java`（teamData / playerUID / savedData 相关字段与方法）
- `src/main/java/com/lulan/shincolle/capability/CapaTeitoku.java`（hasTeam / teamCooldown / playerUID / sync 逻辑）
- `src/main/java/com/lulan/shincolle/utility/TargetHelper.java`（checkSameOwner / checkUnattackable / hostile/ally 队伍判定）
- `src/main/java/com/lulan/shincolle/item/OwnerPaper.java` / `OwnerPaper` 相关队伍签名逻辑（若涉及）

## Modern references

核对、修改与验证的现代文件：

- `src/modern/java/com/lulan/shincolle/team/TeamData.java`
- `src/modern/java/com/lulan/shincolle/team/TeamSavedData.java`
- `src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java`
- `src/modern/java/com/lulan/shincolle/teitoku/TeitokuData.java`
- `src/modern/java/com/lulan/shincolle/teitoku/TeitokuSavedData.java`
- `src/modern/java/com/lulan/shincolle/teitoku/TeitokuEvents.java`
- `src/modern/java/com/lulan/shincolle/ownership/PlayerOwnerData.java`
- `src/modern/java/com/lulan/shincolle/item/OwnerPaperItem.java`
- `src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`（`canCommanderEdit`、`canEngage`、同盟/敌对判定分支）
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`
- `src/modern/java/com/lulan/shincolle/config/ShinColleConfig.java`
- `src/modern/java/com/lulan/shincolle/item/KaitaiHammerItem.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipAircraftEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipRensouhouEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipProjectileEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/projectile/LegacyShipBeamEntity.java`
- `src/modern/java/com/lulan/shincolle/network/GameplayCommandType.java`
- `src/modern/java/com/lulan/shincolle/network/ShipCommandService.java`
- `src/modern/java/com/lulan/shincolle/client/screen/DeskReferenceScreen.java`

## Acceptance assertions

- [x] 核清 `TeamData`、`TeamHelper`、`ServerProxy`、`CapaTeitoku` 与 `TargetHelper` 中多人队伍创建、解散、改名、冷却、Ally/Ban 双向联动及同盟免伤/控制权限的 1.12.2 真源语义。
- [x] 完成本批规划小切片的最小现代修复与边界保护（无新增平行通信架构）。
- [x] 为新增/修正的 Team/同盟/UID 逻辑补齐 `@GameTest` 回归（覆盖权限、双向同步、无效输入、边界状态）。
- [x] `src/main/` 保持只读零改动。
- [x] `git diff --check`、JDK 17 `.\gradlew.bat cleanProcessResources build verifyItemModelCoverage runGameTestServer --no-daemon` 全部通过（0 失败，0 跳过）。

## Frozen non-goals

- 不展开 `B6-INTERMOD-DETAILS`（`ForgeConfigSpec`、外置 mod 联动及 `/ship*` 斜杠命令）。
- 若 `B3` 审计发现多组不耦合问题且当前仅执行第一小切片，超出本切片范围的项明确标记为后续续做，不在本批硬塞大改。
