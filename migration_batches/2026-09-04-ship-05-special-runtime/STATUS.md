# SHIP-05 Status

| Field | Value |
| --- | --- |
| Status | `COMPLETE` |
| Protocol | Frozen before implementation read |
| Scope | IDs `30`, `31`, `33`, `36`, `37`; special runtime, servants, carrier recovery and cadence |
| Legacy tree | Read-only |
| Verification | `git diff --check`, `compileJava`, `cleanProcessResources processResources`, `runGameTestServer` pass; 258 required GameTests pass |
| Client smoke | Java 17 startup, OpenGL initialization, resource reload, integrated single-player connection pass; manual in-world inspection remains V0 |
| Decision | `COMPLETE (SP special runtime boundary)` |

## Work log

- Batch directory created from the batch template.
- Protocol frozen from matrix rows `30`, `31`, `33`, `36`, and `37`.
- Initial legacy and modern file lists are frozen; direct model/projectile/servant dependencies will be appended here before any additional legacy read.
- File-name audit identified the direct hostile variants `EntityDestroyerShimakazeMob` and `EntityBattleshipNGTMob`, plus Shimakaze summon entities and Northern Hime's `EntityFloatingFort`; these were added to `protocol.md` before reading.
- `EntityRensouhouMob.java` was added to the frozen legacy list before reading because the hostile Shimakaze source directly constructs it; its modern counterpart must preserve hostile servant lifecycle and feedback.
- Method-level comparison completed for all five selected rows; no legacy source or resource file was modified.
- Added exact `showEquip` state rules for Midway Hime, Northern Hime, Carrier WD, Shimakaze, and Nagato to the modern model-state catalog.
- Corrected Northern Hime aircraft launch height to the legacy `launchHeight = height` rule for both air attack channels.
- Kept hostile Rensouhou's legacy `OnSightChase=false` boundary explicit and mapped its hostile light shot to modern projectile damage; friendly Rensouhou retains mob damage.
- Existing Northern FloatingFort, Shimakaze five-shot/servant pool, Carrier WD aircraft role, Midway recovery aura, and Nagato AP phase behavior matched the frozen legacy entry points and were not rewritten.
- Added focused GameTests for the five model-state catalogs, special attack-channel boundaries, launch heights, hooks, boss colors, resource limits, and hostile chase boundary.
- Ran one Java 17 dev client smoke after the batch. The client created an OpenGL window, loaded the mod and resources, and entered an integrated single-player connection without a ShinColle model/texture/fallback error. The native window was not manually inspected through the available automation surface.

## Evidence

- [protocol.md](protocol.md)
- [decision.md](decision.md)
- [results/](results/)
