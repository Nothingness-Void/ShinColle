# ParticleHelper parity audit against 0aad501

The reference is `src/main/java/com/lulan/shincolle/utility/ParticleHelper.java`
from 0aad501, not the partially refactored main source tree. Numeric types are
local to each overload; type 28 at a position is dripping water, **not** emotion
28 inside the type-36 emotion family.

## Completed in this change

- `ParticleEmotion` -> `LegacyEmotionParticle`: all constructor cases (0/default,
  1–34), frame counts, loops, frame size, fade and stay state machine, host
  tracking, yaw-relative position and boss offsets. Modern CUSTOM particles must
  explicitly begin/end their buffers; the API adapter uses the original quad
  coordinates, UVs and full brightness. Zero-size custom particles opt out of
  modern frustum culling.
- `ParticleTexts` -> `LegacyTextParticle`: all five rows, fixed 0.8/0.2 quad,
  max age 25 with the original post-increment expiry, initial upward speed 0.1
  and 0.9 damping. Entity text starts at host height * 1.3. TEXT_* owns these
  visuals; combat-react packets no longer add a second smoke/vanilla approximation.
- Northern Hime's existing mouth effect now uses actual dripping water with
  the legacy +/-0.35 horizontal random offset, not an emotion-sheet sprite.
- HUD uses the old HOTBAR Pre phase, 18px texture slots at 21px spacing,
  `Position_HUD=[0.5,0.6]`, cooldown UV mask/text, aircraft counts, key highlight
  and crosshair. Aircraft counts come from existing synchronized ship fields.
  Modern MorphProfile has no aircraft-stock counters: without a ship entity
  those two values remain zero, as in the draw array's initialization. Restoring
  actual stock management for profile-only carrier morphs is **not completed**.
- Canonical lowercase HUD/emotion/text textures are byte-identical copies of
  the original resources. The removed HUD labels were literals, not language
  keys; shared morph inventory and combat actionbar translations are retained.

## Remaining custom classes / branches (not claimed as ported)

Overload labels:

- A: `spawnAttackParticleAt(x,y,z,lookX,lookY,lookZ,type)`
- B: `spawnAttackParticleAtEntity(entity,par1,par2,par3,type)`
- C: `spawnAttackParticleAtEntity(entity,type,double[])`
- D: `spawnAttackParticleAtEntity(host,target,par1,par2,par3,type,setAtkTime)`
- E: `spawnAttackParticleAt(text,x,y,z,type,int...)`

| Original class | Overload/types | Current state / required remaining work |
|---|---|---|
| ParticleLaser | A:14,21 | Not ported; requires original textured beam planes/animated UV, not a vanilla beam trail |
| ParticleLaserNoTexture | B:8; D:0–6 | Not ported; LEGACY_RE_LASER still emits DRAGON_BREATH along its ray, and BEAM_* still approximate with END_ROD/ELECTRIC_SPARK/explosion |
| ParticleSpray | A:15–18,21–23,26,27,29,32,33,37–39,41,47; C:1,2 | Not ported; cloud/bubble/splash/missile approximations do not implement these per-type colors, sizes, speed limits and lifetimes |
| ParticleLightning | B:3 | No corresponding custom renderer; electric-spark approximations are not equivalent |
| ParticleStickyLightning | B:4,6,7,9 | No host-bound multi-segment lightning renderer |
| ParticleCube | B:7; D:1,2 | No corresponding cube renderer or vibrate/target-linked lifecycle |
| ParticleLine | A:44–46 | No corresponding multi-line high-speed aura renderer |
| ParticleSphereLight | B:11,24 | No corresponding inward-beam/sphere renderer |
| ParticleGradient | B:12,14 | No original outward gradient/double gradient renderer |
| ParticleCraning | A:40 | No original block/model craning particle renderer |
| ParticleSparkle | B:13,17,21–23 | Not ported; HEALING_SPARKLE uses HAPPY_VILLAGER, fill/craning/eye/goddess variants are not implemented |
| ParticleSweep | B:15,16 | No original vertical/horizontal colored sword-sweep renderer |
| Particle91Type | A:21 | No original AP-fist phase-4 composite particle |
| ParticleChi | B:1 | No corresponding energy-ball renderer |
| ParticleDebugPlane | B:18–20 | No original body/target/debug geometry renderer |
| ParticleSmoke | A:43 | Original custom-scale large-smoke subclass not ported |
| ParticleTextsCustom | E:0,1 | No original free-text/entity-text renderer |
| ParticleTeam | A:25; B:2 | Existing LegacyTeamMarkerParticle retained; full mechanical parity of all original modes not verified here |

These are 17 not-yet-ported classes (~4,243 legacy lines), plus the existing team
particle (~314 lines) requiring a separate parity check. They include distinct
immediate-mode geometry, matrices, host/target tracking and block/font rendering;
they were not replaced with newly invented algorithms to call this task complete.
The new emotion/text quad adapter alone is insufficient to port them faithfully.

Other vanilla-particle branches and their burst counts/motion were not rewritten
or certified as exact. In particular LAUNCH_SMOKE, HIT_EXPLOSION, AIR/MISSILE/
AIRCRAFT/BOMB/TORPEDO effects, FLARE_BURST, SEARCHLIGHT_MARK, BLACK_HOLE and the
non-text combat-react effects retain pre-existing implementations/approximations.

## Checks

`python3 src/modern/scripts/check_particle_parity.py LEGACY_ROOT OUTPUT_DIRECTORY`
checks mechanical constructor/tick/position parity and original texture bytes,
then compiles the actual ported state methods against headless API stubs. It
exercises normal and invalid types, no host/moving host, all entity modes and
small/boss heights, sprite bounds, expiry and text damping. This is not a
Minecraft/client render test. Actual HUD/particle rendering remains visually
unverified.
