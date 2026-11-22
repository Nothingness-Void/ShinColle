# F round2

Curios 9.6.0 getCuriosInventory returns java.util.Optional, unlike Forge5.14.1
LazyOptional. The bridge unconditionally reflected resolve(), caught the missing
method, and returned empty: both the 16-tick fire-immunity capability scan and
the worn-ring tick dispatch use that same lookup. Handle both return types. Add
curios:ring item tag (items on Forge, item on NeoForge); standard tag validation
previously rejected the ring. Real JAR signatures inspected with javap; no new
mandatory dependency. Optional installed-mod GameTest configures a ring fixture
through Curios's real handler and covers ring scan, breathing, flight, fire,
empty slot and removal. No-slots API legitimately yields no inventory; final
integration run must configure slots as the report did, rather than count skip
as exercised integration.

The bfb98e5/9bfb9fa report predates the owned-flight correction. Modern ring now
never grants mayfly; both inventory and Curios removal clear flying via the
same scan. Tests begin survival with mayfly=false and assert it remains false.
Do not revoke unrelated mayfly ownership or guess ownership of old leaked bits.
Four-abyssium wrench already restored; full recipe diff remains in decision.md.

ConfigHandler defaults at113–119 and395–400 restored as Attrs_Scale and four
Attrs_Hostile_* six-value numeric lists. Friendly base/buff/potion/movement
reads and hostile band selection now read config rather than immutable tables;
unchanged default constants retained as reference. Malformed list length or
element uses established numeric-list fallback. Regression tests exercise final
friendly/hostile HP computation and all four default bands.

Morph: unlocking a profile does not select or activate it (TeitokuData.unlockMorph).
MorphHelper.openMorphScreen sends SelectedClassId=-1; MorphInventoryScreen's
containerTick closes when no profile. The key callback and screen registration
are present on both versions, and there is no bridge-available check in the
key dispatch. Legacy C2SInputPackets.MorphGUI and GuiHandler only opened for
capa.morphEntity instanceof BasicEntityShip; unlock alone was insufficient.
External Metamorph activation remains unported and the reported bridge was
id=none. Do not invent activation or change the default key just for a conflict;
rebinding cannot supply a missing selected/active external morph. Actual G/H
client dispatch counter-check pending; no production change claimed.

OpenAL: supplied full log path is absent here. The single Stop diagnostic can
come from vanilla Channel.stop or Channel.destroy; both call alSourceStop and
checkALError("Stop"). No mod custom sound stop/loop instance implementation was
found. Reachability of a mod-triggered invalid source is unverified; leave OPEN,
not legacy-identical or confirmed upstream. Need complete surrounding log and
a repeatable stop/close sequence to distinguish engine cleanup versus a source
invalidation originating in mod playback. No speculative audio changes.
