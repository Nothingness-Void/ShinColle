# 批次：方块与物品旧版行为纠偏

## 问题

现代造船厂、起重机与物品实现是否只做版本升级，并严格保留 1.12.2 的内容和实际行为，而不是用现代自创机制替代？

## 范围

- 批次日期：2026-11-22；分支：`devin/parity-fix-items-blocks`。
- 四轮定向复核：造船厂与受保护方块、起重机与路线能量、变身及舰船补给物品、船蛋与最后一批工具。
- [逐条结论](decision.md) 共 27 条。计数说明：原第二轮 A 中的工作循环和无 IC2 时的 EU 语义分别编号；其余保持用户原问题分组，对应 `6 + 3 + 11 + 7`，没有增加一个新的审计问题。
- 旧版：`src/main/java`、`src/main/resources` 只读；现代实现仅修改 `src/modern/**`。
- 实现提交：`cfddb11`（第一、二轮）、`cb6af2f`（第三轮）、`79b4527`（第四轮）。此前的“全量收口”记录不能替代本次纠偏结论。

## 非目标与边界

- 不修改 `codex/mc-1.20.1`，不创建合并请求；仅推送用户指定的功能分支。
- 不修改 `/home/ubuntu/baseline`。已知基线失败不在本批修复范围。
- 不宣称完整视觉等价或变身航空战斗库存生命周期已完成；见 [未验证项](decision.md#未验证项与后续边界)。

## 导航

- [协议与入口](protocol.md)
- [状态](STATUS.md)
- [27 条结论与历史参考](decision.md)
- [改动文件](results/changed-files.md)
- [构建与回归证据](results/README.md)

`src/modern/item-final-protocol.md` 与 `src/modern/item-final-results.md` 是第四轮执行时的历史记录；本目录是本批次统一结论与证据入口。
