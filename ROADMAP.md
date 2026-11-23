# 后续队列

当前事实：[CURRENT_STATE](CURRENT_STATE.md)；
[整合与 release 记录](migration_batches/2026-11-23-ship-smoke/integration-release.md)。

## NEXT：0.2.2-alpha smoke + Pointer 整合交还

1. 在两个现有 smoke branches 正常 merge 指定 fetched bases，保留双方修改；
   冲突只按实际五个 docs 处理，不 rebase/force push。
2. 两端 mod_version=0.2.2-alpha 后，JDK17/21 完整 build+GameTests。
3. 普通 commit/push feature branches，使用同一通过门禁的 main release jars，
   复制到用户指定两个 release 路径并核验元数据/哈希。
   无源码/资源/版本变化，不重复已通过构建。
4. 报告 pushed SHAs、真实 gate 输出、冲突文件与解决方法及 jar 路径，然后停止。
   不扩展飞机生命周期、mount/LivingEntity、LEVITATION 或其它历史队列。
