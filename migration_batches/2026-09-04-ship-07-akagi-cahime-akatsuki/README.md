# Batch: SHIP-07 Akagi/CA Hime/Akatsuki/Hibiki/Ikazuchi

## Question

这个批次只回答：class `48`、`49`、`51`、`52`、`53` 的单人主线行为是否已经按 1.12 旧版语义接通到现代路径，重点覆盖航母恢复、CA Hime 夜战、三艘驱逐舰的骑乘/燃料/战斗 cadence、hostile 边界、runtime save 和已锁定模型状态。

## Scope

- 舰种/class ID：`48`、`49`、`51`、`52`、`53`
- 行为族/模块：`carrier`、`cruiser`、`destroyer`；M2、M4、M5、M6、M7、M8
- 旧版入口：见 [protocol.md](protocol.md) 的冻结清单
- modern 入口：见 [protocol.md](protocol.md) 的冻结清单

## Out of scope

- 不重新审计 SHIP-01 至 SHIP-06 已锁定行为，也不把共享实现的覆盖推断为其他 `SLICE` 行已完成。
- 不恢复完整 1.12 parity、多人 ally UI、完整旧版 packet-heavy GUI、全部 inter-mod、历史 worldgen 矩阵或非主线边角语音/动画。
- 不改变敌对 Boss 过远追击或敌方起飞/命中提示策略，除非冻结的五个旧版入口证明存在直接回归。
- 不扩展新的网络协议；只使用现有 command/gameplay state bus 表达单人必要状态。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
