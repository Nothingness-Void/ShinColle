# Frozen Protocol

## Legacy references

只读取以下旧版入口作为语义真源：

- `src/main/java/com/lulan/shincolle/entity/BasicEntityShip.java`
- `src/main/java/com/lulan/shincolle/entity/BasicEntityShipHostile.java`
- 重攻击、miss 判定、投射物发射和目标伤害相关方法。

## Modern references

只读取以下现代入口，必要时只修改这些文件：

- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java`
- `src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java`
- `src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java`

## Acceptance assertions

- [ ] 对 `HEAVY + justLaunch + canMiss`，先执行旧版等价的 miss 判定；miss 不得直接对目标造成非零伤害。
- [ ] 命中、miss、冷却和资源消耗仍由现有 catalog/helper 路径控制，不新增第二套攻击分派。
- [ ] 至少有一个确定性 GameTest 断言 miss 结果，以及一个命中回归断言。
- [ ] `git diff --check` 通过。
- [ ] 按 [MIGRATION-TEST-MATRIX.md](../../MIGRATION-TEST-MATRIX.md) 运行 `compileJava`、`processResources`、`runGameTestServer`。
- [ ] legacy Java/resources 目录无差异。

## Frozen non-goals

- 不启动客户端；本批不改 renderer、资源、粒子或 screen。
- 不借此批次修改敌对 Boss 寻敌距离、玩家创造模式筛选或 actionbar 权限。
- 不扩大到尚未由旧版入口证明的舰种行为差异。
