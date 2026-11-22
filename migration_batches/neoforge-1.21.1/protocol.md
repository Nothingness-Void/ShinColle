# NeoForge 1.21.1 version upgrade

## Approved rendering follow-up

Merge Forge rendering backport 1547691 without rewriting history. Only API,
identical-renderer and documentation merge differences may be resolved; stop
on substantive behavior conflicts. Add the approved Hime land-leg policy and
exact source-audit exceptions; keep saved bit 4 unchanged, explicit legs always
visible, default hover in water, other models unchanged. Validate build, full
GameTests and 1536 controlled cases; capture live land/water, other standing/
sitting ships, desk/crown placed and held. Do not broaden unrelated gameplay.

- Base: origin/devin/parity-fix-integration (5b95cfe).
- Scope: build files, src/modern/java, src/modern/resources and migration documentation.
- Reference: fixed modern implementation on the base branch; src/main remains read-only.
- Goal: compileJava on JDK 21 with NeoForge 21.1.267 and ModDevGradle 2.0.148; adapt registry, events, network, components, capabilities, renderer and data APIs without changing gameplay or constants.
- Checks: compileJava, processResources, verifyItemModelCoverage and structural resource validation. Runtime and visual validation must be reported separately from compilation.
- Non-goals: new mechanics, parity backlog, legacy source changes, pushes.

## Authorized client rendering follow-up

### Residual Destroyer Hime placement audit

Identify the FR egg 29 model/state path from the reported screenshot; compare all registered model classes, variants, standing and sitting against legacy defaults and transforms. Entry points: `ModelDestroyerHime`, `ShipModelBaseAdv`, legacy `EntityDestroyerHime`, model-state catalogs and compatibility entity/emotion bridge. Programmatically measure rendered geometry relative to entity origin where feasible; preserve intentional legacy poses instead of globally snapping feet. Verify any correction with compileJava, full GameTests and the specific client scene. No block/item changes or legacy source edits.

Outcome: [decision and audit](decision.md). Legacy no-leg hovering reproduced;
forcing that variant to ground is held for a parity decision. No production
offset/default change is included in this audit-only follow-up.

- Restore ship model placement from `RenderLivingBase`/`RenderBasic` and the legacy ship models; compare the fixed 1.20.1 wrapper and NeoForge 21.1.267 `LivingEntityRenderer`. Keep dimension, eye-height and rider-position numbers unchanged.
- Restore crown/crane and desk rendering, and audit waypoint, both shipyards and volcano core against their legacy block JSON, TESRs and `RenderTileEntityItem`. Remove case-colliding modern texture files without changing the lowercase runtime assets.
- Modern entry points: `LegacyShipPoseModel`, the three block entity renderers, `ModBlocks`, item renderer registration and the corresponding block/item model JSON. `src/main/**` remains read-only.
- Acceptance: grounded BB/FR models, legacy sitting/riding transform path, runtime hitboxes equal to the legacy catalog; lit, correctly oriented desk and 3D inventory/held block models; no case-folding collisions in modern resources. Run JDK 21 `compileJava`, full `runGameTestServer`, targeted client screenshots and resource assertions. Commit the ship and block fixes separately, then push the existing branch as requested.
- Non-goals: new models, mechanics or gameplay values, unrelated visual backlog, or claiming complete client parity from these targeted checks.
