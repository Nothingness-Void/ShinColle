# Frozen Protocol

## Legacy references

- 潜艇的投射物速度、弧线和攻击触发。
- 隐身/不可见状态的旧版条件。
- SSNH、U-511、Ro-500、Yo、Hime 的单人可达路径。

## Modern references

- 潜艇实体和行为 catalog 的对应入口。
- 鱼雷/投射物 helper。
- 隐身与客户端可见性路径。

## Acceptance assertions

- [x] 单人必要潜艇路径与旧版规则逐项对照。
- [x] 未发现需要新增 modern 实现的阻塞差异。
- [x] 审计结论和未覆盖范围有独立记录。

## Frozen non-goals

- 不为了制造代码变更而改写已经匹配的逻辑。
