# 测试夹具调整与归因边界

## passiveAiStopsAutomaticRangeTargeting

- 前期300项运行出现 `re-enabling automatic targeting should restore the legacy range target goal`。
- 在独立干净 worktree `/home/ubuntu/repos/ShinColle-baseline`，基于 `origin/codex/mc-1.20.1`（8f1d2fd）全量298项通过。因此**不能记为预存失败**。
- 原夹具使用固定世界坐标；改为 `helper.absolutePos` 的结构相对坐标，让目标与本测试舰娘一起隔离。未修改自动目标选择业务及断言。
- 隔离后后续全量通过。未逐实体追踪确切竞争对象，不把“共享世界干扰风险”升级为已确认的具体因果链。
- 原失败日志仍在 `/home/ubuntu/shincolle-ring-check.log`；基线日志在 verification.json。

## Raiden 配对/燃料测试

- 新增区块加载测试后，既有配对测试出现非预期配对；候选查询取首项的路径可受共享世界其它实体影响。
- 将该测试舰娘/玩家抬高至隔离位置（含远离其它夹具的位置），并在结束时清理配对舰娘。配对/解体/燃料边界断言保留，未改配对业务逻辑。
- 后续304及最终全量通过；确切竞争实体没有逐一溯源，原失败运行已被最终日志覆盖，不能称为独立基线已证明的预存失败。

## autoCombatRationUsesLegacyTickAndFoodRules

- 曾出现 `auto combat ration should wait for the legacy 128-tick boundary`。原夹具处于固定世界坐标且开启无关AI。
- 改为结构相对高空位置、关闭无关AI，补结束清理和失败诊断信息（tick/物品/饱食度/士气）。仍保留127不消费、128消费、256衰减及阈值检查。
- `LegacyShipEntity.tick` 的自动补给/口粮逻辑不依赖 Mob AI 开关，测试并未绕过被测逻辑；未改口粮业务代码或放宽断言。
- 隔离后后续全量通过；没有为这一失败另做干净基线归因，因此不声明其为预存失败，也不声称已确定具体干扰实体。

## 其它夹具/测试纠正

- B2普通坐骑与 Round2编队夹具在走严格旧owner路径之前设置有效玩家UID；不放宽生产所有者校验。
- Morph获取夹具明确设有效变体；随后增加真正 Destroyer I classId=0 的获取/存档回归，不以绕过零号问题替代修复。
- 新测试初次的 LazyOptional API、私有查询可见性和 GameTest模板名称错误已修正；模板采用项目既有 `gameplayparitygametests.empty`。这些是本轮测试实现错误，不列为预存失败。
- 早期 chunk-loader 测试连续写配置引发 watcher 解析错误，移除测试中的连续配置写入；最终通过结果不包含此错误。
- 最终生产编辑后的编译与全量服务端测试见 verification.json；没有客户端视觉或真实重启验收。
