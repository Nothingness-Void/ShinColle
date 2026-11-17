# Batch Decision

- 结论：`MATCHED`
- 影响矩阵：[MIGRATION-SHIP-MATRIX.md](../../MIGRATION-SHIP-MATRIX.md)
- 关联模块：M2、M4、M5、M6

## Facts established

- 旧版 `BasicEntityShip.onLivingUpdate` 在服务端每 128 tick、自动口粮和挂载更新之后调用 `updateConsumeItem()`。
- `updateConsumeItem()` 对 class 0、1、2、3 的 idle grudge 使用 5，对 class 9 使用 8；移动 grudge 使用 `consumeGrudgeAction[Move] = 3`，士气消耗为移动整数距离的 0.5 并限制在 5 到 50。
- 旧版低于 `4 * consumeAmmo` 储备线时，先消费普通弹药，再消费容器弹药；普通弹药补充轻炮 30、重炮 15，容器按 9 倍计算，并保留 `Attrs.AMMO` 的整数截断。
- 旧版 grudge 不足时每次只恢复一件物品：先消费 `Grudge`，再消费 `BlockGrudge`；恢复量分别为 `baseGrudge * modGrudge` 与 `baseGrudge * 9 * modGrudge`。现代 `GRUDGE1` 是另一种物品，不能替代旧版方块。
- 旧版攻击延迟使用 `baseAttackSpeed / attackSpeed + fixedDelay`，没有现代侧额外的 melee/ranged 最小值钳制。
- 旧版位置账本由实体构造时的默认位置开始；现代侧对应字段保持运行时状态，不写入 NBT。

## Code changes

- [LegacyShipBehaviorCatalog.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipBehaviorCatalog.java) 增加仅对 class 0、1、2、3、9 生效的 idle/movement 资源映射。
- [LegacyShipCombatHelper.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipCombatHelper.java) 恢复旧版攻击延迟公式。
- [LegacyShipEntity.java](../../src/modern/java/com/lulan/shincolle/entity/ship/LegacyShipEntity.java) 在服务端 128 tick 入口接入位置、待机 grudge 和低弹药补给，并保留旧版普通弹药/容器弹药、普通 grudge/`BlockGrudge` 优先级。
- [GameplayParityGameTests.java](../../src/modern/java/com/lulan/shincolle/gametest/GameplayParityGameTests.java) 增加五个 class ID 的 cadence、资源消费和 NBT 回归测试，并锁定两种 grudge 恢复物品。

## No-change audits

- 五个旧版类的 constructor、owner 被动效果、拾取、挂载偏移已由此前切片覆盖，本批只锁定新增 runtime/combat/save 域。
- 未改 `src/main/java`、`src/main/resources`；未改 GUI、网络协议、资源、渲染、投射物和世界生成。
- catalog gate 对未进入本批的 class ID 返回负值，不把 class 0/1/2/3/9 的语义猜测扩散到其他舰种。
- 现代实体没有独立的旧版 `NoFuel` 标志字段；本批保持现有 `ShipFuel`/资源值作为可用性边界，不新增第二套标志状态。

## Follow-up

- 将矩阵中的 class 0、1、2、3、9 标记为 `LOCKED`，并把下一组五行推进为 `NEXT`。
- 下一批使用同一批次协议处理命令、路线和目标边界；不重新遍历本批已锁定入口。
