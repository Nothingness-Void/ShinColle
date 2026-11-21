# Batch Protocol

## 对照基准

- 语义真源：`codex/mc-1.20.1` 分支内只读的 `src/main/java`、`src/main/resources`（1.12.2）。本批涉及的 legacy 代码均可编译、无半重构段，未需要回退参照 `0aad501`。
- 审计输入：`ShinColle-1.20.1-parity-audit.md` 中分配给 `crafting-world-config` 的 12 项 critical/major。

## 验收条件

1. 大建装备：无 `equipRate`/弹药兜底；实例 roll 大建 `totalMat = 0`；小建/大建按 `>180/200/220`、`>2000/3000/3500` 计算 `enchLv` 并按 `EnchantHelper.applyRandomEnchantToEquip` 附魔。
2. 大建投料：`current > MAX_STOCK` 才拒绝；资源物品、装备（`Equip*.getResourceValue`）、船蛋（`getKaitaiItems(meta-2)`）、重怨念块均可投入；easyMode 投料 ×10、放出 90/10。
3. 宝箱：`config/shincolle/loottable.cfg` 采用旧版格式与默认内容；名称按 1.12 `ResourceLocation` 小写解析，无法解析（`shincolle:Torpedo`）跳过；rolls `1..N/2+1` 整数除法；随机 meta 保持单条目概率。
4. 敌舰：`Check_Ring` 与 `Limit_MobSpawnNumber` 生效，每组 `min~max` 只；Boss 护卫精英 50%。
5. 多金属砂砾：每个海洋区块一次，`Polymetal_Gravel` 次尝试，海底定位、冰封海洋向下找底、半径 `l = rand(num-1)+1`（`num = 2+rand(2)`，每区块一次）、替换目标按 `Polymetal_Gravel_Replace`。
6. 删除旧版不存在的 advancements、custom trigger 及其测试与语言键。
7. `compileJava` 与 `runGameTestServer` 通过（JDK 17）。
