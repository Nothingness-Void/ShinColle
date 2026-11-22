# Rendering backport decisions

## Scope and API equivalents

Backport the rendering work from cae1ae9, 3fa3514 and a1050d1, not the
NeoForge version migration. The follow-up user-approved visual deviation shows
Destroyer Hime's legs on land while preserving the original toggle in water.
The saved model state, geometry, offsets and gameplay numbers are unchanged.

- Restore the outer legacy pose pass and preserve Midway's independent misc
  matrix. Forge 1.20.1 copies Matrix4f/Matrix3f explicitly because Pose.copy()
  belongs to the later API; the existing float RGBA signature stays intact.
- Forge custom block items initialize IClientItemExtensions through
  Item.initializeClient, rather than NeoForge's registration event. Only desk
  and small shipyard use this item renderer; the same BERs draw them.
- Restore Z-axis 180-degree block transforms, non-occluding desk properties,
  crane/waypoint inventory parents, and the exact same lowercase-only resource
  set. Do not edit src/main or change processResources' lowercase copy.
- Carry over the two block rendering tests, runtime dimensions/eye-height test
  and Destroyer Hime model-state test without reducing assertions. Forge
  EntityDimensions uses public width/height fields rather than accessors.
- CPU audit supports both VertexConsumer naming conventions, a Forge client
  classpath or NeoForge client artifacts, and JAVA_HOME. It retains all model
  source comparisons with exactly the two approved Destroyer Hime policy calls
  excepted. It measures 64 variants / 1536 controlled land/water pose cases.

## Approved Destroyer Hime visual deviation

Only ModelDestroyerHime replaces its two bit-4 checks (visibility and normal
pose) with DestroyerHimePosePolicy.showLegs(state, inWater). The signal is the
live vanilla Entity.isInWater() flag exposed through the compatibility entity,
not a new gameplay flag, cached depth approximation or persisted bit rewrite.
Effective legs = explicit bit 4 OR not in water. Other state bits do not enable
legs in water. Existing sitting, no-fuel and swimming animation branches remain.
The user's explicit leg enable therefore wins in both environments, while the
default legs-hidden state retains its hovering pose only in water. This is an
approved visual departure from 1.12.2, not a purported minimum API adaptation.
Both versions use the same pure policy and regression assertions.

## Earlier review fixes

**f6db0ec: not applicable.** RecoveredShipItemEntity extends ItemEntity on Forge
as well, but 1.20.1 ItemStack still directly serializes/deserializes `tag`
CompoundTag data. There is no component conversion or LegacyEntityDataMixin
gate to skip. Forge 47.4.18 sources preserve that NBT constructor/codec and
capability import path. Do not introduce a NeoForge migration hook into Forge.

**3a06c51: not applicable to the shipped Forge tag set.** Its trigger was
NeoForge's actual c:enchantables common group. Forge 47.4.18 Tags.java has no
enchantables group; the 1.20.1 resources have no equivalent c:/forge group.
The 301 vanilla/Forge/mod item-tag definitions were expanded recursively:
no c:/forge enchantables group and no iron-helmet/diamond-sword shared tag.
The generic matcher could still share tags added by third parties; no unrelated
material-tag policy change is inferred from that possibility. No matcher edit.

## Validation boundaries

The CPU audit uses a controlled BasicEntityShip surrogate, not every live
wrapper/animation/water/riding/scale combination. Source bodies are checked
against the read-only reference. A targeted client smoke test is separate
from broad rendering parity. Actual old-save upgrade and multiplayer remain
outside this rendering backport.

The Forge wrapper has no executable bit; use `bash ./gradlew` as its exact
documented equivalent. Maven Central returned HTTP 429 during initial plugin
dependency resolution. A machine-local ~/.gradle/init.d mirror selects the
public Google Maven Central mirror; no repository build configuration changed.
