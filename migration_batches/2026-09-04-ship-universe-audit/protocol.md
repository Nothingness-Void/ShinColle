# Frozen Protocol

## Legacy references

只读取以下旧版入口完成活动实体全集核对：

- `src/main/java/com/lulan/shincolle/init/ModEntity.java`：`regShipEntity` 调用及注册顺序。
- `src/main/java/com/lulan/shincolle/reference/ID.java`：`ID.ShipClass` 名称映射，用于识别历史残留名称和 class ID。
- `src/main/java/com/lulan/shincolle/entity/`：由注册表引用的具体舰船类及 hostile 变体文件是否存在。

## Modern references

只验证以下现代入口：

- `src/modern/java/com/lulan/shincolle/entity/ship/ShipEntitySpecs.java`：egg meta、class ID、hostile 标记和规格集合。
- `src/modern/java/com/lulan/shincolle/registry/ModEntityTypes.java`：现代实体注册承载方式。
- `MIGRATION-SHIP-MATRIX.md`：逐舰状态唯一事实表。

## Acceptance assertions

- [ ] 旧版活动注册的每个具体舰种/class ID 都能在现代规格中找到对应项。
- [ ] friendly/hostile 变体、class ID 和历史别名映射没有漏项或错位。
- [ ] `ID.java` 中只存在名称但没有旧版实体类/注册入口的条目被单独分类，不被误报为已迁移舰种。
- [ ] 现代矩阵行数、现代规格唯一舰种集合和旧版活动集合的差异都有结果记录。
- [ ] `git diff --check` 通过。
- [ ] 本批不运行客户端、编译或 GameTest；没有运行时代码变更时不重复自动化回归。

## Frozen non-goals

- 不凭历史字符串猜测缺失的舰船实现。
- 不将本次静态全集核对替代 V0 原生客户端验收。
- 不修改任何 `src/main/java`、`src/main/resources` 或现代运行时代码。
