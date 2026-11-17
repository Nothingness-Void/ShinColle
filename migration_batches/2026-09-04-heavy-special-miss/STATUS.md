# Heavy Special Miss Status

| Field | Value |
| --- | --- |
| Status | `AUDITED; NO CODE CHANGE REQUIRED` |
| Legacy tree | Read-only |
| Scope | Heavy special attack miss semantics |
| Decision | [decision.md](decision.md) |

## Work log

- Batch created from the migration batch template.
- Protocol frozen before reading implementation code.
- Legacy and modern heavy-launch paths were compared. The reported direct-damage miss path is not present in the current worktree.
- Existing deterministic GameTests already cover both generic and catalog heavy misses.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/heavy-special-miss.json](results/heavy-special-miss.json)
- [results/](results/)
