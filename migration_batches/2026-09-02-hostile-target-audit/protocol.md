# Frozen Protocol

## Legacy references

- 敌对 Boss target range 和目标过滤条件。
- 创造模式、无敌状态和视线过滤。
- hostile aircraft/Rensouhou 的起飞、命中和 actionbar/message 分支。

## Modern references

- hostile target acquisition helper。
- combat/reaction/particle packet bus。
- hostile aircraft/Rensouhou 客户端提示入口。

## Acceptance assertions

- [x] 目标过滤与旧版条件逐项对照。
- [x] 敌方起飞和命中不向玩家显示不应显示的提示。
- [x] 未确认的行为不因截图审计直接修改。

## Frozen non-goals

- 不在审计批次中扩大寻敌范围或重写攻击系统。
