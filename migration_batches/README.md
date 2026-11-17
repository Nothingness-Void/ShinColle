# Migration Batches

这里是迁移的 Question Plane。每个目录只回答一个可验收问题，避免把几十个舰种、多个行为族和整套测试揉成一篇长日志。

## 固定结构

```text
migration_batches/<batch-id>/
  README.md       # 问题、范围、入口和导航
  protocol.md     # 开工前冻结的旧版对照和验收条件
  STATUS.md       # 当前过程状态，不写最终历史结论
  decision.md     # 完成后的结论和未做事项
  results/        # 精确命令、测试计数、资源/客户端证据
```

从 [_TEMPLATE](_TEMPLATE/README.md) 复制结构，目录名使用 [NAMING.md](../NAMING.md) 规定的日期和主题格式。

## 阅读规则

- 接手工作先读批次 `README.md` 和 `STATUS.md`。
- 只有需要验证语义时才读 `protocol.md` 指定的 legacy/modern 文件。
- 已完成批次只读 `decision.md` 和 `results/`，不要重新扫描代码；代码发生影响时才重新打开批次。
- `decision.md` 的结论必须链接回根部矩阵，但根部矩阵不复制整段结论。
