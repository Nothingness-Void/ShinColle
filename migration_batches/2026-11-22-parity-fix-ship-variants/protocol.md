# 批次协议

从 `origin/devin/parity-fix-ai-movement` 恢复敌舰规模、战斗与击杀奖励，以及舰种专属攻击的 1.12.2 行为。审计范围为分配的七项，保留先前批次修复，旧版源码只读。

旧版查阅入口为 `BasicEntityShipHostile`、`BuffHelper`、`EntityHelper`、`EntityAIShipRangeAttack`、受影响舰种及旧版变身逻辑。现代查阅入口为 `LegacyShipEntity`、`LegacyShipBehaviorCatalog`、`HostileRuntimeState`、Boss 与变身辅助类，以及对应注册、生成入口、资源和 GameTest 引用。

验收覆盖敌舰显式与默认规模、按规模的蛋和经验、无额外 Boss 动作与初始化等级士气、特殊攻击共用延迟，以及爱宕、高雄、金刚级无自创技能。编译及全量 GameTest 的执行方式见 [测试矩阵](../../MIGRATION-TEST-MATRIX.md)。非目标为本组审计之外的变身、生成与其他模块重构。
