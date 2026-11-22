# Waypoint world visibility regression

Round2 harvest scope: historical BlockWaypoint.removedByPlayer/harvestBlock/
quantityDropped and modern WaypointBlock.onDestroyedByPlayer/playerDestroy,
WaypointBlockEntity.canDestroy and block loot data. Preserve owner restriction,
server-only inventory return (including legacy creative), full-inventory ground
fallback and no duplicate vanilla loot. Add directed harvesting tests.

Scope: compare historical 0aad501 BlockWaypoint, TileEntityWaypoint, client
renderer registration/TESR and modern WaypointBlock, WaypointBlockEntity,
waypoint marker particles and block/item model resources. Inspect relevant
render-resource guard and block GameTests. Preserve held-item icon rendering.
No src/main edits, unrelated particle migration or gameplay changes.

Assertions: ordinary held blocks must not expose waypoint world geometry;
legacy-approved waypoint/wrench/matching pointer must expose only the legacy
marker effect. Check negative, empty and positive held-item conditions. First
implement on Forge, build and full GameTests with JDK17; merge into NeoForge,
adapt only required APIs and build/full GameTests with JDK21. Capture VM stone
versus wrench/waypoint screenshots, commit and push both without force. Record
evidence and limitations in this directory. Do not start unrelated NEXT work.
