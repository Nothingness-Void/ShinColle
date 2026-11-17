# Batch: SP-CLOSE

## Question

这个批次只回答：当前现代侧是否已经满足 1.12 单人主线的 M0-M9 代码/资源闭环，并能用真实客户端 V0 进世界验收；缺口必须补实现或明确转入 parity backlog。

## Scope

- 全部 45 个唯一 class ID 的已锁定主线行为，以及共享生命周期、runtime、命令、AI、战斗、投射物、渲染和内容入口。
- M0-M9：注册与资源、实体生命周期、fuel/ammo/grudge/morale/repair/rescue/cooldown、命令与菜单、follow/guard/route/formation/target/supply/pickup、per-ship catalog、combat/resource/loot、projectile/reaction、model/renderer/screen、recipe/loot/advancement/encounter/DeskReference。
- V0：真实 Java 17 客户端启动、资源加载、单人世界生成友方/敌对/Boss、Pointer/ShipInventory/命令/路线/护卫/停止/坐下/补给/存档恢复、骑乘/变身/技能/projectile/反馈/GUI/模型/纹理/粒子/声音。
- 证据来源：已完成 SHIP-01 至 SHIP-09 批次记录、`MIGRATION-SCOPE.md`、`TRACKS.md`、`MIGRATION-FLOW-CHECKLIST.md` 和本协议列出的现代入口。

## Out of scope

- 完整多人 ally UI、完整旧版 packet-heavy GUI 页面、全部 1.12 worldgen/ore-dict 历史兼容、真实外部 inter-mod API。
- 不因 V0 工具限制而把未实际执行的进世界操作记录为通过；若当前环境无法完成，保持 V0 未完成并留下阻塞证据。
- 不重写现代架构，不把跨舰种分派塞回 `LegacyShipEntity`，不修改 `src/main/java` 或 `src/main/resources`。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
