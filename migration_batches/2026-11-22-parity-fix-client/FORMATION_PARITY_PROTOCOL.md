# Formation parity scope

Reference: 0aad501 GuiFormation, GuiHelper GUI-4 hitboxes,
FormationHelper.getFormationBuffValue, BuffHelper.calcAttrsWithoutBuff and
EntityAbyssMissile constructor. src/main and the reference checkout are read-only.

Modern targets: FormationScreen, FormationMenu, LegacyShipStats presentation
accessors, GameplayParityGameTests, formation client-state tests, language and
canonical lowercase GUI textures. No missile algorithm changes.

Restore direct six-formation/nine-team selection, original hitboxes, repeated-row
inventory opening without a time window, 60/40 wait counters, spots, bars, morale,
names and attribute tooltips. Synchronize original server attributes rather than
inventing client formulas. Remove only obsolete formation-screen mechanisms and
their exclusive keys. CLEAR_CURRENT_TEAM is also used by pointer sprint+shift
click, so its command and handler must remain.

Checks: compileJava, runGameTestServer with counts, and one client smoke check.
Only src/modern changes are submitted, per the explicit handoff boundary.
