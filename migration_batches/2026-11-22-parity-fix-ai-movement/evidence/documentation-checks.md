# 第6步文档检查

检查范围：本次修改的根文档、已标记撤销的历史decision，以及统一批次全部Markdown；递归解析本地链接目标和Markdown/显式HTML锚点，并扫描旧批次路径残留。

```text
Missing links/anchors: 0
Stale old-batch references: 0
Non-document tracked changes: []
```

迁入的step4/results/checks.json为历史验证记录，只移动、不改内容。src/main与src/modern均无差异；所有新增文件均在文档批次目录内。git diff --check及暂存后git diff --cached --check须无输出。

本步没有代码或资源改动，按用户要求未重新执行compileJava/GameTest；319/319是第5步已有结果，不是本次文档检查输出。
