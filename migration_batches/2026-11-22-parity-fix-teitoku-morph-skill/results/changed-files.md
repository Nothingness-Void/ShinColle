# 改动文件清单

相对工作基线 `8f1d2fd`；覆盖全部分步工作，不只最后的零号舰/编队补修。A=新增、D=删除、M=修改。没有构建/CI/legacy/运行日志/临时文件。历史局部批次未提交，现统一归入本目录，不保留重复事实副本。

## 实现与测试

- M `src/modern/java/com/lulan/shincolle/ShinColle.java`
- M `src/modern/java/com/lulan/shincolle/client/GameplayClientEffects.java`
- M `src/modern/java/com/lulan/shincolle/client/MorphClientEvents.java`
- A `src/modern/java/com/lulan/shincolle/client/RingClientEvents.java`
- A `src/modern/java/com/lulan/shincolle/client/particle/LegacyRepairGoddessParticle.java`
- D `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyMountRenderer.java`
- M `src/modern/java/com/lulan/shincolle/client/renderer/entity/LegacyShipMountRenderer.java`
- M `src/modern/java/com/lulan/shincolle/client/screen/DeskReferenceScreen.java`
- M `src/modern/java/com/lulan/shincolle/config/ShinColleConfig.java`
- D `src/modern/java/com/lulan/shincolle/entity/mount/LegacyMountEntity.java`
- A `src/modern/java/com/lulan/shincolle/entity/mount/LegacyMountControls.java`
- M `src/modern/java/com/lulan/shincolle/entity/mount/LegacyShipMountEntity.java`
- M `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- M `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- M `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- M `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`
- A `src/modern/java/com/lulan/shincolle/gametest/MorphParityGameTests.java`
- A `src/modern/java/com/lulan/shincolle/gametest/MorphAndTeamParityGameTests.java`
- A `src/modern/java/com/lulan/shincolle/gametest/ShipChunkLoaderGameTests.java`
- M `src/modern/java/com/lulan/shincolle/item/MarriageRingItem.java`
- M `src/modern/java/com/lulan/shincolle/item/PointerCommandEvents.java`
- M `src/modern/java/com/lulan/shincolle/item/TargetWrenchItem.java`
- M `src/modern/java/com/lulan/shincolle/menu/MorphInventoryMenu.java`
- A `src/modern/java/com/lulan/shincolle/morph/MorphDamageHandler.java`
- M `src/modern/java/com/lulan/shincolle/morph/MorphEvents.java`
- M `src/modern/java/com/lulan/shincolle/morph/MorphHelper.java`
- M `src/modern/java/com/lulan/shincolle/morph/MorphRuntimeState.java`
- M `src/modern/java/com/lulan/shincolle/network/GameplayCommandHandler.java`
- M `src/modern/java/com/lulan/shincolle/network/GameplayCommandType.java`
- M `src/modern/java/com/lulan/shincolle/network/GameplayParticleType.java`
- M `src/modern/java/com/lulan/shincolle/playerskill/PlayerSkillHostResolver.java`
- M `src/modern/java/com/lulan/shincolle/registry/ModEntityTypes.java`
- M `src/modern/java/com/lulan/shincolle/teitoku/TeitokuData.java`
- M `src/modern/java/com/lulan/shincolle/teitoku/TeitokuEvents.java`
- M `src/modern/java/com/lulan/shincolle/teitoku/TeitokuHelper.java`
- M `src/modern/java/com/lulan/shincolle/teitoku/TeitokuSavedData.java`
- A `src/modern/java/com/lulan/shincolle/world/ShipChunkLoader.java`
- M `src/modern/resources/assets/shincolle/lang/en_us.json`
- M `src/modern/resources/assets/shincolle/lang/zh_cn.json`
- M `src/modern/resources/assets/shincolle/lang/zh_tw.json`

## 文档（本批次证据及旧结论撤销标记）

- `CURRENT_STATE.md`
- `MIGRATION-CHANGELOG.md`
- `MIGRATION-DECISIONS.md`
- `MIGRATION-TEST-MATRIX.md`
- `migration_batches/2026-11-16-b1-gui-parity/decision.md`
- `migration_batches/2026-11-16-b2-packet-parity/decision.md`
- `migration_batches/2026-11-17-b6-intermod-details/decision.md`
- `migration_batches/2026-11-18-full-ship-behavior-reaudit/decision.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/README.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/protocol.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/STATUS.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/decision.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/README.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/verification.json`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/source-evidence.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/fixtures.md`
- `migration_batches/2026-11-22-parity-fix-teitoku-morph-skill/results/changed-files.md`
