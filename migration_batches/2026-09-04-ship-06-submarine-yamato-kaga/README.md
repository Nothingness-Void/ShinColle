# Batch: 2026-09-04-ship-06-submarine-yamato-kaga

## Question

这个批次只回答：class 38、39、44、46、47 的敌对战斗/存档、Boss 运行时、目标与阶段节奏、航母回收和视觉边界，是否已经按 1.12 旧版语义接通到现代单人路径。

## Scope

- 舰种/class ID：`38`、`39`、`44`、`46`、`47`
- 行为族/模块：`submarine`、`princess`、`battleship`、`carrier`；M2、M4、M5、M6、M7、M8
- 旧版入口：见 [protocol.md](protocol.md) 的冻结清单
- modern 入口：见 [protocol.md](protocol.md) 的冻结清单

## Out of scope

- 不重新审计 SHIP-01 至 SHIP-05 已锁定行为，也不从共享 helper 推断其他 `SLICE` 行已完成。
- 不改变敌对 Boss 过远追击或敌方起飞/命中提示策略；只有冻结范围内的直接旧版回归才允许修正。
- 不恢复完整 1.12 parity、多人 ally UI、完整旧版 packet-heavy GUI、全部 inter-mod、历史 worldgen 矩阵或非主线边角语音/动画。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
