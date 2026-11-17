# Batch: 2026-09-02-hostile-target-audit

## Question

敌对 Boss 的寻敌距离、创造模式/无敌目标筛选、视线条件以及敌方飞机/Rensouhou 的起飞和命中提示是否保持旧版行为？

## Scope

- 行为族：`abyssal` / `princess` / hostile aircraft
- 旧版入口：敌对目标筛选、攻击提示和 actionbar/message 分支
- modern 入口：hostile target helper、combat reaction bus、客户端提示抑制路径

## Out of scope

- 不凭截图臆造规则；不修改尚未确认的寻敌半径。
- 不扩大到友方提示或其他 GUI。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/acceptance.json](results/acceptance.json)
