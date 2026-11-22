# Preliminary checked chain

The complete modern ModelDestroyerShimakaze body matches historical 0aad501
after the existing three API aliases (checked with model_body from the pose
audit). getIsRiding forwards ship.isPassenger, emotion lookup forwards entity
emotion, BORED=4. Its sitting/riding BORED branch translates Y by0.575, rotates
body1.4835, leaves left leg0/right leg-0.2618 and splays Z0.1745/-0.35. Ordinary
sitting instead translates0.45 and bends both legs-2.2689. These are legacy poses,
not evidence of a per-model migration difference. The common wrapper restores
outer setRotationAngles before model.render's own scaled pose; no catalog shift.

Counter-check found a separate reachable 1.21 attachment difference requiring
completion: legacy EntityMinecart.getMountedYOffset returns0.0 (vanilla1.12.2
source mirror checked at https://raw.githubusercontent.com/KealJones/mc-1.12.2-source_files/master/src/minecraft/net/minecraft/entity/item/EntityMinecart.java).
Forge1.20.1 AbstractMinecart.getPassengersRidingOffset bytecode also returns0.0
(javap on cached mapped Forge47.4.18 jar). NeoForge1.21.1 EntityType.MINECART sets
passengerAttachments(0.1875F); AbstractMinecart only special-cases villagers,
otherwise Entity.positionRider adds this anchor then subtracts the passenger's
getVehicleAttachmentPoint. Modern LegacyShipEntity currently returns only
(0,-getMyRidingOffset(),0), so a Shimakaze passenger (egg38, riding offset0)
is raised0.1875 versus Forge/legacy, independent of emotion. Need finish runtime
regression test and minimal NeoForge cart-anchor compensation; Forge may need
only invariant coverage, not an artificial offset. Exact user boot clipping
has not been runtime reproduced; do not yet call its visual cause confirmed.
