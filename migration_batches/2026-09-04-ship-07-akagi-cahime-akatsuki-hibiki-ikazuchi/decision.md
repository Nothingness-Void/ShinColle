# SHIP-07 Decision

状态：`COMPLETE (SP mainline boundary)`

本文件记录五个选定舰种与 1.12 语义的逐域结论。`MATCHED` 表示已有现代路径与旧版一致，`CHANGED` 表示仅做了本批对照证明的现代侧最小适配。

| ID | 舰种 | runtime/save | attack/cadence/resource | target/rider boundary | render/feedback |
| ---: | --- | --- | --- | --- | --- |
| 48 | Akagi | `MATCHED`: carrier runtime、owner relation、aircraft inventory/recovery and transient state use the existing catalog/entity path | `MATCHED`: Zero/T selection, air attack channels, cadence and resource gates match the selected carrier/airplane sources | `CHANGED`: exact Akagi `showEquip` bits, source bake and texture lookup are now covered; no fallback path added | `CHANGED` |
| 49 | CA Hime | `MATCHED`: small-ship dimensions, food/runtime defaults and night refresh remain in the shared legacy state path | `MATCHED`: night critical/triple bonuses, range attack, push target filtering and impact cadence match the old entity | `CHANGED`: exact TailBase and hat/hair/horn/ear branches are now represented and baked from the old source | `CHANGED` |
| 51 | Akatsuki | `MATCHED`: owner, rider mask and transient gattai state remain catalog-owned; no new save schema was introduced | `MATCHED`: range attack cadence and attack/resource eligibility use the old shared AI contract | `CHANGED`: exact equipment bits and rider-side no-fuel dismount now match the old override | `CHANGED` |
| 52 | Hibiki | `MATCHED`: fixed eye height, dimensions, hostile scale and rider state use existing spec/entity state | `MATCHED`: range attack cadence and target/resource gates remain the old shared path | `CHANGED`: exact hat-state visibility and no-fuel Akatsuki dismount are now covered | `CHANGED` |
| 53 | Ikazuchi | `MATCHED`: fixed eye height, dimensions, hostile scale and friendly/hostile Raiden state use the catalog | `MATCHED`: range attack cadence and target/resource gates remain the old shared path | `CHANGED`: exact equipment bits and no-fuel Akatsuki/Raiden cleanup now match the old override | `CHANGED` |

## No-change audit

- Akagi aircraft selection, Zero/T inherited initialization, launch sound and trail suppression were already equivalent; the batch only added missing model-state coverage.
- CA Hime has no custom attack override in the selected old class; its range attack and push behavior are inherited and remain catalog-owned.
- Akatsuki/Hibiki/Ikazuchi rider order, masks, passenger positions, health/fuel gates, damage dismount and hostile Raiden boundaries were already present; the missing rider-side no-fuel callbacks were added without changing the gattai architecture.
- The hostile Boss far-chase and player-notification policy was outside the frozen scope and was not changed.

## Follow-up

- The matrix locks only the covered domains for IDs `48`, `49`, `51`, `52`, and `53`.
- The next queue entry is `SHIP-08`, covering the matrix `NEXT` rows for Inazuma, Tenryuu, Tatsuta, Atago and Takao; its protocol is not frozen yet.
- V0 in-world interaction and full 1.12 parity remain outside this batch.
