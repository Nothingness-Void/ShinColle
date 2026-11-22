# Per-item verdicts

## 1. Radar names: legacy behavior, unchanged

Legacy GuiDesk.RadarEntity uses the full custom or translated name;
drawRadarText draws s.name at (147,texty), without clipping/truncation/scaling.
Only the following level/HP/coordinates use scale 0.8. This is also true in
0aad501. The modern method body is identical after mechanical API/color aliases,
checked by src/modern/scripts/check_gui_audio_parity.py. A long English name
such as Isolated Island Princess therefore overflows the 113-pixel right panel
in legacy as well. No name-rendering changes are authorized by this report.

## 2. Large shipyard: regression fixed

Legacy GuiLargeShipyard.drawGuiContainerForegroundLayer has material values,
time, and only two errors at center (105,99): missing material when powerGoal
<= 0 and buildType != 0, otherwise missing fuel when !hasPowerRemained(). It
has no structure/core banners; its title drawing is commented out. Historical
0aad501 agrees. Modern LargeShipyardScreen had invented banners at (103,17)
and (103,28), over the first material count and mode buttons, plus an enabled
title. Removed these without moving existing geometry, numbers or hot areas.

LargeShipyardMenu.getFailureMessage is display-only (the screen is its sole
production caller). Removed invented structure/select-mode/output-lock errors
and restored the two legacy conditions and priority. Construction/permissions,
inventory handling and network/container data are unchanged. A GameTest covers
72 combinations of mode, goal, fuel boundaries, output and structure state.

## 3. Sound exhaustion: legacy frozen-hour replay; no production change

Preserved reported log: /home/ubuntu/shincolle-gui-audio-reported-client.log,
510 occurrences each of pool-limit and handle-allocation warnings. Environment
is loaded (JDK21, initialized OpenAL Null Output); this is pool capacity, not a
missing setup/dependency. Saved New World level.dat has DayTime=6000 and
doDaylightCycle=false. All 14 saved legacy_ship entities have ShipTimeKeeper=1.

Reachable chain: LegacyShipEntity.tick's server branch calls tickTimeKeeper
every tick. Alive/enabled/config-enabled gates then accept dayTime%1000 == 0.
playShipCombatSound broadcasts a normal sound; ClientLevel creates a one-shot
SimpleSoundInstance, not a mod loop. SoundEngine/Library allocate static
channels and report exhaustion at their pool limit. No mod tickable/looping
SoundInstance implementation exists. The hourly sound at 6000 is ship-time6,
whose time06 resource/event is also present in legacy sounds.json.

The partial src/main refactor calls missing getWorldHourTime/playTimeSound
methods under its eight-tick gate, so complete historical 0aad501 was needed:
BasicEntityShip.getWorldHourTime (287) accepts time%1000 == 0 with no debounce;
server onLivingUpdate dispatch (2278) is outside the eight-tick gate and plays
on every server tick, exactly as modern code does. Its playTimeSound (484)
plays ordinary sounds, not retained loop instances. Suppressing this replay or
adding a cooldown would change this legacy behavior, so no such fix was made.

Final client A/B at frozen 6001 versus 6000 will discriminate this path from a
retained-loop leak; record measurements in STATUS.md before calling it verified.
