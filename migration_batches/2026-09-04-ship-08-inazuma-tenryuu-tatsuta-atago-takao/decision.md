# SHIP-08 Decision

状态：`COMPLETE (SP mainline boundary)`

本文件记录五个选定舰种与 1.12 语义的逐域结论。只有在定向对照、实现和批末验证完成后填写；未覆盖的行为不得写成已完成。

| ID | 舰种 | runtime/save | attack/cadence/resource | target/rider boundary | render/feedback |
| ---: | --- | --- | --- | --- | --- |
| 54 | Inazuma | MATCHED existing owner/runtime and Inazuma/Raiden state | MATCHED inherited range cadence and resource path | MATCHED Akatsuki/Raiden pairing, hostile profile, and rider cleanup | CHANGED exact legacy `EquipBase` bit and model source/bake path |
| 56 | Tenryuu | MATCHED skill phase, night effect, and owner effect | MATCHED heavy resource gate, dash timers, and damage path | MATCHED target fallback, phase boundary, and mounted path | CHANGED exact five-part legacy model state |
| 57 | Tatsuta | MATCHED skill phase, night effect, and owner effect | MATCHED heavy resource gate, control wave, and Gae Bolg path | MATCHED target fallback, phase boundary, and mounted path | CHANGED exact three-part legacy model state |
| 58 | Atago | MATCHED common runtime, food state, and Atago Emotion4 reset | MATCHED inherited range/heavy path and type-five missile gate; old class has no entity-specific attack override | MATCHED mount, hostile scale, and reaction path | CHANGED exact four-part legacy model state |
| 59 | Takao | MATCHED common runtime and inherited emotion path | MATCHED inherited range/heavy path and type-five missile gate; old class has no entity-specific attack override | MATCHED mount, hostile scale, and reaction path | CHANGED exact four-part legacy model state |

## No-change audit

- 定向旧版/现代读取确认：五行的尺寸、骑乘偏移、巡洋舰技能阶段、攻击资源边界、反应效果和 Atago/Takao type-five 重炮条件已有现代 catalog 对应实现。

> **已撤销（见 D-016）**：本条旧解析模型/目录作为完整动画、死亡、情绪和发光等价实现的口径已撤销；原数值/部件审计仅留作历史证据。替代实现与边界见 [客户端还原决定](../2026-11-22-parity-fix-client/decision.md)。

- 本批唯一需要补齐的是五个旧模型的 `showEquip` 位映射；现代 `LegacyShipModel` 已负责资源解析和 bake，本批只接入精确状态分支。
- 旧版 Atago/Takao 没有普通实体级攻击 override，因此沿用共享攻击路径，不新增分派逻辑。
- morph-only hooks 不属于本批选定实体行；敌对 Boss 远距离追击和玩家提示策略也不在本批范围内，保持不变。

## Follow-up

- 本批实际覆盖域已锁定；下一批为 SHIP-09，读取范围以矩阵中的 IDs `60`、`61`、`62`、`63`、`72` 为准。
- V0 进世界人工交互与完整 1.12 parity 仍是后续边界，不由本批的自动化和客户端启动烟测替代。
