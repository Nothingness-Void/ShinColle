# 逐项复核

| 项目 | 状态 | 依据与实现 |
| --- | --- | --- |
| 士气档位与 128 刻变化 | fixed | `Enums.Morale`、`BuffHelper.updateBuffMorale` 与 `BasicEntityShip.updateMorale` 一致。现代属性、档位和口粮统一使用严格的 `>5100/3900/2100/900`；脱战低于 3100 加 40，高于 3900 减 10。 |
| 独立燃料与断粮 | fixed | `ShipStateHandler.decrGrudge` 扣完怨念后尝试消耗一件补给，再判定断粮。`BasicEntityShip.updateFuelState` 清空/重建舰娘与坐骑 AI，断粮时归零士气，切换时发送原粒子。移除 `ShipFuel` 的实体字段、存档、菜单和失败提示；旧存档中此字段不再参与行为。敌对 Mob 不走友方补给。 |
| 新实体、缺字段默认值 | fixed | `ShipStateHandler` 初始化轻弹、重弹与怨念为 0，士气为 60；保持有效旧字段和现代字段的既有读取优先级。 |
| 无上限补给 | fixed | 历史 `BasicEntityShip.addAmmoLight/addAmmoHeavy/addGrudge` 仅截断负值；现代库存和离线缓存按实际数值显示。 |
| 航空攻击士气消耗 | fixed | `ShipStateHandler.decrMorale` 明确轻航空减 6、重航空减 8。 |
| 饥饿与补给反馈 | fixed | `ShipStateHandler.decrGrudge/decrAmmo`、`ConfigHandler`、`ShipInventoryHandler` 和 `ReactionHandler` 确认怨念饥饿倍率、可配补给、普通物品优先及容器九倍，并恢复按原间隔发送进食/缺弹粒子。 |

`skipped`：无误报。旧版缺料粒子来自弹药补给后仍未达到储备的分支；怨念耗尽使用另外一组断粮粒子，不叠发两种反馈。

## 旧树说明

当前 `src/main` 处于状态处理器拆分后的半重构状态：`BasicEntityShip` 的部分调用仍引用旧 `ID.Morale` 和原资源方法。使用 `0aad501` 确认零初值、仅截断负值、攻击顺序与界面显示；用 `09393e2^` 的 `ID.Morale` 交叉确认阈值。补给配置和反馈以当前 `ShipStateHandler`、`ReactionHandler` 为准。未改动旧树。
