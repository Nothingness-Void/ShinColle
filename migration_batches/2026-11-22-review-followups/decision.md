# Decisions

Preserve material-specific ore equivalence, not generic item classifications;
[reason and fixtures](ore-matching.md).

Import only the legacy ShinColle drop marker on Neo and preserve native values.
Forge already loads ForgeData and receives tests only;
[platform source audit](forge-data.md).

Keep unrelated legacy/gameplay gaps and original worktrees unchanged. The
explicit unified version request authorizes gradle.properties outside modern/.
