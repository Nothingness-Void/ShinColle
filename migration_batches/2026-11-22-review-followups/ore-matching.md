# A — ore equivalence, not container/material classification

## Evidence and decision
- Read-only InventoryHelper.matchTargetItem (779–833) intersects
  OreDictionary.getOreIDs only for different item identities when checkOredict
  is enabled. Same-item metadata/NBT behavior is independent.
- Historical 0aad501 TileEntityCrane.matchTempItem (1285 onward) does the same
  ore-ID intersection after identical-item checks; applyItemTransfer calls it.
  ModOres registers named materials (ingotAbyssium, ingotPolymetal, etc.), not
  a generic bucket or generic ingot category.
- Forge 1.12.x OreDictionary.initVanillaEntries registers ingotIron/ingotGold
  separately and no vanilla bucket entries:
  https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/oredict/OreDictionary.java
- NeoForge 21.1.267 sources contain c:buckets with both water/lava sub-tags.
  ItemStack.getTags -> matchesTargetItem accepted that common parent, and
  CraneBlockEntity.matches -> applyItemTransfer uses the result directly.
- Disconfirming check: Forge 47.4.18 universal jar has NO built-in bucket tags.
  Thus the default bucket bug is reachable on Neo, not default bare Forge.
  Forge nevertheless accepted arbitrary shared tags and is vulnerable when a
  pack/mod adds forge:buckets (or c:buckets); cover that supported tag setup.

Exclude only unqualified c/forge bucket/material-category roots (buckets,
ingots, nuggets, ores, dusts, gems, storage_blocks), retaining specific material
sub-tags and all existing custom equivalences. This also prevents iron/gold
cross-matching through the generic ingots parent and Minecraft's shared
beacon_payment_items / trim_materials classifications (discovered by the ingot
negative test). Static resolution of platform + mod resource tag trees found
exactly three common iron/gold tags on both platforms: the generic ingots
parent, beacon_payment_items and trim_materials. The same resolution found
only c:buckets for Neo water/lava and no default Forge common bucket tag.
No item blacklist, fluid API
change, namespace-wide rejection, or crane behavior change is needed.

## Coverage
LegacyItemMatchGameTests checks actual crane load/unload filter predicates,
water/lava rejection in both directions, identical water and empty bucket,
specific shared ingots/iron equivalence, disabled-ore rejection, generic
iron/gold rejection, and empty inputs. It temporarily supplies holder tags in
try/finally to model a second mod's equivalent ingot and Forge pack bucket tags;
no fixture tags ship in production resources. Existing real-tag plank/grudge,
NBT and crane-transfer tests remain unchanged. Full gates recorded in result.md.
