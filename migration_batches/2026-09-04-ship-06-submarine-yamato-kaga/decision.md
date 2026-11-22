# Batch Decision

- **已撤销**：下方对敌舰规模默认推导、通用 Boss Runtime 以及特殊攻击独有冷却的等价认可。见 [D-014](../../MIGRATION-DECISIONS.md#d-014舰种个体行为的现代偏离已撤销)。

- **已撤销**：下表与实现记录中将“非伤害 miss”及投射物 miss 伤害边界视为旧版一致的部分；其他弹道、坐标和目标排除结论不变。更正见 [D-006](../../MIGRATION-DECISIONS.md#d-006投射物-miss-语义不可丢失)。

- 结论：`COMPLETE (SP submarine/Yamato/Kaga boundary)`
- 影响矩阵：class `38`、`39`、`44`、`46`、`47`
- 关联模块：M2、M4、M5、M6、M7、M8

## Evidence rule

记录每个行为域的实际结果；`MATCHED` 表示旧版语义已由现代路径保留，`CHANGED` 表示仅做了被对照证明的现代侧最小修正。

## Per-ship record

| ID | Ship | Runtime/save | Attack/target/cadence | Render/feedback boundary | Result |
| ---: | --- | --- | --- | --- | --- |
| 38 | `EntitySubmU511` | `MATCHED`: 潜艇路径、速度、隐身和 hostile scale/runtime 默认值沿用既有 catalog/state 管线 | `MATCHED`: light projectile、深度移动选择、目标有效性和资源路径保持旧版边界 | `CHANGED`: 旧版 U-511 scale-level 分支接入运行时模型变换；source/texture 解析保持旧路径 | `CHANGED` |
| 39 | `EntitySubmRo500` | `MATCHED`: U-511 派生 runtime、Garuru/辅助状态、隐身和 hostile 默认值保持 | `MATCHED`: Ro-500 light projectile、深度选择、资源/cooldown 和目标边界保持 | `CHANGED`: 旧版 Ro-500 scale-level 分支接入运行时模型变换；source/texture 解析保持旧路径 | `CHANGED` |
| 44 | `EntitySubmHime` | `MATCHED`: hime submarine path、mount 和 boss runtime 状态沿用现有实体管线 | `CHANGED`: 双导弹侧向坐标改为旧版 `CalcHelper.rotateXZByAxis` 等价结果；miss 散射改为 `nextFloat()`，保留目标/host 排除和非伤害 miss | `CHANGED`: `ModelSubmHime` source、showEquip bits 和无 fallback bake 断言补齐 | `CHANGED` |
| 46 | `EntityBattleshipYMT` | `MATCHED`: engine/runtime 默认值和 hostile scale 沿用既有状态路径 | `MATCHED`: beam phases、hostile cannon、目标有效性、cooldown/resource 及 host/friendly-fire 边界保持旧版选择 | `CHANGED`: Yamato source、showEquip bits 和 scale-level 运行时变换补齐；beam host 排除边界补回 | `CHANGED` |
| 47 | `EntityCarrierKaga` | `MATCHED`: Zero/T inventory、capacity/recovery 和 hostile owner 边界保持既有 CV 管线 | `MATCHED`: aircraft selection、hostile scale 和 resource/cooldown 入口保持 | `CHANGED`: Kaga source、showEquip bits 和 scale-level 运行时变换补齐 | `CHANGED` |

## Code changes


> **已撤销（见 D-016）**：本条旧解析模型/目录作为完整动画、死亡、情绪和发光等价实现的口径已撤销；原数值/部件审计仅留作历史证据。替代实现与边界见 [客户端还原决定](../2026-11-22-parity-fix-client/decision.md)。

- 新增 `LegacyShipModelTransformCatalog`，只投影冻结旧版模型中已确认的 scale/offset 分支，不替换模型架构。
- `LegacyModelSourceParser` 支持旧版 `scale * number` 参数形式；`LegacyShipModel` 将已确认的 scale-level/offset 应用于 bake 后渲染。
- `LegacyShipModelStateCatalog` 补齐 U-511、Ro-500、Submarine Hime、SSNH、Yamato、Kaga 的旧版 `showEquip` 位规则。
- `LegacyShipBehaviorCatalog` 补齐潜艇姬侧向导弹坐标和旧版 miss 随机数类型；beam/area projectile 使用旧版 host、敌对同队和 miss 伤害边界。

## No-change audits

- U-511/Ro-500 的潜艇深度移动选择、基础 projectile profile、拾取/隐身和 save defaults 已匹配，无需另造 per-ship 分派。
- Yamato 的 beam 阶段、Kaga 的 Zero/T 选择与飞机回收入口已匹配，本批只补直接证明的模型和边界缺口。
- 敌对 Boss 过远追击及敌方起飞/命中提示策略不在冻结范围内，本批未改动。
- `EntitySubmHimeNew`/SSNH 仅作为冻结模型与既有双导弹回归的直接依赖；没有把 class 72 的整行状态提前宣称完成。

## Follow-up

- 矩阵已将本批五行锁定；下一批提升 class `48`、`49`、`51`、`52`、`53` 为 `NEXT`，入口见 [ROADMAP.md](../../ROADMAP.md)。
- V0 仍需人工进世界检查选定友方/敌方/Boss、投射物、粒子、GUI 和反馈；完整 1.12 parity 继续留在 backlog。
