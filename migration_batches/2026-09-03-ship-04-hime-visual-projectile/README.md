# Batch: 2026-09-03-ship-04-hime-visual-projectile

## Status

`COMPLETE`

## Question

Airfield Hime、Battleship Hime、Destroyer Hime、Harbour Hime 与 Isolated Hime 的 1.12 模型源、投射物族、渲染入口和单人战斗反馈，是否已经由现代实现按旧版语义接通，并且不会静默 fallback？

## Scope

- class ID：`21`、`26`、`27`、`28`、`29`
- 行为族：`hime visual`、`heavy projectile`、`aircraft`、`static effect`
- 模块：M5、M6、M7、M8
- 目标：确认五行的旧版模型源可 bake、攻击投射物族不串线、命中/未命中反馈隔离正确，并只修复被对照和测试证明的现代侧缺口。

## Result

- 五行均已接通旧版模型源解析/bake、模型状态位、投射物族和现有反馈总线；现代侧只保留必要的模型状态、起飞高度和 railgun 粒子适配。
- 批次静态检查、Java 编译、资源清理重建和 GameTest 通过；客户端完成启动与资源 reload 烟测，无 ShinColle 模型/贴图/fallback 错误。
- 原生 Minecraft 窗口未暴露给当前自动化环境，因此进世界后的人工操作未虚报为通过；该项保留到 V0 手工验收。

精确命令和结果见 [STATUS.md](STATUS.md) 与 [results/ship-04.json](results/ship-04.json)。

## Out of scope

- GUI、网络协议、路线命令、存档字段和敌对 Boss 过远追击。
- 未列入本批的舰种行为，不从共享 renderer 或 projectile 推断为已完成。
- 完整 1.12 parity、全部 inter-mod、历史资源路径兼容和非主线边角视觉差异。

## Navigation

- Protocol: [protocol.md](protocol.md)
- Status: [STATUS.md](STATUS.md)
- Decision: [decision.md](decision.md)
- Results: [results/](results/)
