# Shimakaze bored minecart pose review

Question: red boots clip through minecart walls in bored pose but not sitting.
Compare complete historical 0aad501 ModelShimakaze and its base pose methods,
renderer transforms and BasicEntityShip riding/getYOffset hooks against modern
legacy-model source/wrapper, LegacyShipRenderer, render catalog, entity riding
attachments/offset helpers and relevant riding tests. Verify the actual bored
and minecart gates, and try to disprove a suspected offset regression. Fix only
verified migration differences, Forge first then merge to NeoForge. Preserve
identical legacy poses. No changes to src/main or invented cosmetic offsets.
Record source evidence, verified/assumed reachability and any unverified runtime
scope. If code changes, include existing build and GameTest gates on both
branches after the final changes. This is separate from waypoint behavior.
