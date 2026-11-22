# GUI and audio regression protocol

## Questions and scope

Compare Admiral Desk radar names, large shipyard text/layout and reported
sound-source exhaustion against read-only 1.12.2. Fix only migration regressions,
on Forge first, then merge into NeoForge without rewriting history. Retain any
identical legacy behavior and report it. No src/main, gameplay, CI or build edits.

## Directed source entries

- Legacy GuiDesk, GuiLargeShipyard; modern DeskScreen/DeskScreenSupport,
  LargeShipyardScreen and corresponding menu/state helpers.
- Legacy SoundHelper, SoundHandler, ISoundEntity, BasicEntityShip and sound call
  sites; modern ShinColleSoundHelper, ShipSoundType, entity/client sound call
  sites, sound registration/JSON and engine sound lifecycle as needed.
- Relevant Desk/client/UI GameTests and new focused source/pure-policy tests.
- Historical 0aad501 only if a needed legacy entry is absent from src/main.
- Report screenshots ss_0bfde3da.png and ss_e40be118.png; preserve the supplied
  NeoForge latest.log as evidence before launching another client.

## Assertions and gates

Prove each leading sound cause is reachable on this client path; try a
counter-check that could disprove it. Separate verified evidence from inference.
Retain legacy name sizing/layout and sound timing, not invented convenience UI.
Test normal, empty and boundary/error inputs where new behavior is introduced.
After the final source edit run build and all GameTests once per branch, JDK17
with bash ./gradlew on Forge and JDK21 with ./gradlew on NeoForge. Capture final
NeoForge GUI screenshots at 2560x1920 and GUI scale 8. Record exact tails,
counts, decisions, screenshots and unverified scope. Commit/push both branches.
