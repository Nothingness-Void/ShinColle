# Broad wall, recovery, normal audio and queued PR11 review

RingFly decision: retain legacy state. Forge push hold remains. Current source
baselines Forge3730e83 and Neo77bd8ba. Read only existing mount/path protocol and
F3 recovery/audio protocol dependencies, plus direct paths discovered therein.

Trace actual private client mount navigation target/path/index/repath at broad
wall; compare finder parameters and step-height with BasicEntityMount constructor,
BasicEntitySummon and ShipPathNavigate/ShipPathFinder. Probe candidate versus an
unchanged geometry/control before attributing cause. Check floating-effect
inheritance (not merely canFly body) before choosing modern effect adapter.
Add broad-wall coverage without moving/weakening existing finite-wall assertions.

Recovery: modern commands/ShipCommands, ShipCacheSavedData/ShipWorldCacheEntry,
TeitokuHelper and existing killed-ship persistence tests; legacy ShipCmdGetShip,
CommandHelper/EntityHelper and ServerProxy duplicate guard. Reproduce actual
dimension transfer into non-ticking chunk then recovery; distinguish cache window
from duplicate suppression and compare UID/UUID/entity id/dimension/tick age.

Audio: private native client six real idle ships with timekeeping enabled, normal
daylight ticking for >=300 seconds, no frozen exact-hour. Record start/end wall
time, world time, alive keeper count, pool/handle warnings and OpenAL backend.

After these, PR11 A separately changes ModNetwork protocol only; PR11 B separately
changes PointerClientEvents/PointerCommandEvents/GameplayCommandHandler and focused
MorphAndTeamParityGameTests, comparing legacy PointerItem159-340. Preserve repeated
block hold. Register protocol rejection for mixed versions. Both branches gated
after final source; commit locally Forge, normal merge and one Neo push at end.
