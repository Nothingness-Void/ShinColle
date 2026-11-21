# 舰娘核心行为一致性协议

- 目标：复核六条审计发现；仅恢复 1.12.2 行为，不回退其他批次。
- 旧版入口：`ShipStateHandler`、`BasicEntityShip`、`BuffHelper`、`Enums`、`ConfigHandler`；当前旧树半重构时用 `0aad501` 核实。
- 现代入口：`LegacyShipEntity`、`LegacyShipStatTables`、`LegacyShipBehaviorCatalog`、`ShinColleConfig`、舰娘界面/同步和现有 GameTest。
- 验收：四条士气档边界和脱战士气一致；航空消耗 6/8；新实体和缺字段存档为 0 怨念、0 弹药、60 士气；真实补给不封顶；怨念不足触发旧版断粮表情和 AI 生命周期；补给读旧版配置并恢复反馈；编译、全量 GameTest 验证。
- 非目标：不改旧树、注册舰种、投射物或无关子系统。
