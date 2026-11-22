# Waypoint visibility

Confirmed regression: modern WaypointBlock returned MODEL unconditionally,
unlike 0aad501 BlockWaypoint.getRenderType's INVISIBLE. Historical registration
binds TESRs only for desk, heavy grudge and small shipyard, not waypoint. Its
red world marker comes from TileEntityWaypoint.update every eight client ticks:
main-hand ItemBlockWaypoint, TargetWrench or PointerItem metadata <3 only.
ParticleHelper type25 dispatches ParticleTeam block type9: scale0.2, red circle,
no arrow, 31-tick lifetime, age-dependent rise/fade, double-sided horizontal UVs.

Modern waypoint had no client ticker/marker implementation, so changing only
the model shape would hide it even with the right tool. Restored INVISIBLE,
a client-only ticker and the specific type9 marker. Left item/block JSON intact
to preserve existing 3D held/inventory icon. Existing shape/pathing unchanged.
Normalized the reused ParticleTeam texture's filename and references to lowercase
(no pixel change); uppercase ResourceLocation paths cannot be loaded reliably.
No other team-particle logic was changed.

New GameTest covers empty/stone, waypoint/wrench and all six pointer modes,
invisible world shape, retained item-parent path and marker texture availability.
Render-resource guard passed; Forge build passed and 346/346 tests passed before
later queued fixes. Final combined gates and VM evidence will be in STATUS.md.
This focused fix does not port the previously missing link sprays/chest labels
from the rest of TileEntityWaypoint; those remain outside this marker finding.
