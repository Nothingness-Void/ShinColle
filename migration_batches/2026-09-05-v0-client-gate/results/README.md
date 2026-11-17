# V0 Client Gate Results

## Environment

- Java: `C:\Program Files\Java\jdk-17.0.3.1`
- Command: `.\gradlew.bat runClient`
- Date: 2026-09-05 (Asia/Tokyo)

## Observed result

- `runClient` started successfully.
- Native process title: `Minecraft* Forge 1.20.1`.
- Startup log reached OpenGL 4.6, Forge registration, ShinColle initialization, resource reload, OpenAL and texture atlas creation.
- No ShinColle crash, missing model, missing texture or fallback signal was found in the captured startup log.
- CUA exposed no native app (`apps: []`) on 2026-09-05, so automated in-world interaction was not performed.
- Client was terminated after the startup check; no Java client process remained.
- Static completeness recheck found 45 matrix rows, 45 unique primary modern specs, zero matrix-to-spec gaps and zero duplicate primary specs; this did not replace V0 in-world acceptance.
- 2026-09-16 update: User manually verified in-world V0 acceptance and confirmed SP mainline pass (`STATUS.md` and `decision.md` closed as `DONE`).

Machine-readable detail: [v0-client-gate.json](v0-client-gate.json)
