# Cooking — Crop quality and growth (locked design)

This file is the live system spec. It is not a build log or implementation checklist.

Crop stars and **growth cancel** live inside **Cooking**. CustomCrops is the plant/harvest engine for custom crops. SimpleFactions owns **province fertility** (0–100 lookup only). Seeds stay dumb IA/vanilla items: no `food_quality`, no planted_crops SQL, no plant→harvest lineage.

This document is the source of truth. If code and this file disagree, change the code.

[`crops.yml`](../src/main/resources/crops.yml) is loaded (`CropsLoader`). Harvest math is `CropHarvestQuality`. Growth math is `CropGrowthChance`. Vanilla hoe harvest, vanilla `BlockGrowEvent`, and CustomCrops harvest rewrite are live. Fertility uses SimpleFactions province 0–100 (`CropFertility`); missing plugin, disabled map, or unmapped land is **0** for harvest. Growth **does not cancel** when the map is off or `growth-gate.enabled` is false. CustomCrops loot tables drop ItemsAdder `tfmc_cooking` produce (and IA seeds). Cooking converts produce via [`conversions.yml`](../src/main/resources/conversions.yml) at harvest quality H. Configured seed drops are left unchanged. Dowsing farm tables drop plain IA seeds (`ia.playbox_custom_crops:…_seeds`).

## Locked split (do not blur)

| Piece | What it is | What it is not |
|------|------------|----------------|
| **Seeds** | IA/vanilla seed stacks from CustomCrops yield and Dowsing. | Not Cooking food. Not `food_quality`. Not `c.seed`. |
| **Harvest food** | Conversion of the dropped IA/vanilla produce to TLibs/Cooking food at quality **H**. | Not a second `food:` line on the crop. Not MMOItems. Not IA star variants. |
| **Harvest roll** | Weighted 1–5 from fertility × crop affection, then pickup permissions. | Not planted-seed lineage. Never zero weight on 5. |
| **Growth gate** | `(fertility/100)^affection` per grow tick. Fertility 0 never grows; 100 always grows. | Not harvest stars. Not bone meal (TFMCCore). |
| **Fertility** | SimpleFactions province **0–100** at the block. Low fertility hampers harvest and slows growth. | Not a second weight file in SimpleFactions. |
| **Permissions** | `tfmc.cooking.better_crops` (and other pickup effects) apply **after** the harvest roll. | Does not affect grow ticks. |

Pickup conversion of farm **produce** is still quality **1** via `CropsConfig.isFarmFood` (farm conversion inputs only; not a 5★ backdoor). Potato/carrot are produce, not seeds, for pickup.

## Harvest pipeline

```text
Province fertility 0–100  ×  crops.yml affection
  → stress = (1 - fertility/100) * affection
  → lerp rich → poor star weights
  → sample H in 1–5 (every star weight > 0)
  → OriginQualityResolver.applyPickupPermissions
  → (hoe) HoeQualityBonus
  → convert produce via conversions.yml at H; leave seed drops unchanged
```

## Growth pipeline

```text
growth-gate.enabled and SimpleFactions map active
  → affection from crops.yml
  → growChance = (fertility/100) ^ affection
  → vanilla: cancel BlockGrowEvent on fail
  → CustomCrops: Cooking wraps loaded grow-conditions (no fertility keys in CC YAML)
```

Unlisted blocks/ids are not gated. `growth-gate.enabled: false` or map off: all ticks allowed.

## Roller (`CropHarvestQuality`)

Stress is **0** at fertility 100. At fertility 0, stress equals affection. Affection is clamped to `(0, 1]`; missing or invalid uses **0.5**.

Star weights interpolate from `harvest-quality.rich` to `harvest-quality.poor` by stress. Defaults:

- rich: `8, 16, 28, 28, 20` for stars 1–5
- poor: `40, 28, 18, 10, 4`

After lerp, each weight is clamped **> 0**, then normalized, then sampled. Fertility never forbids 5★.

`roll(cropId, Location, Player)` reads fertility via `CropFertility.at`, then permissions. Hoe harvest applies `HoeQualityBonus` after that.

## `crops.yml`

Keep `seed:` + `source` + `block` so harvest can skip seeds and growth can look up affection. There is no `food:` on crops. Produce identity lives in CustomCrops loot + `conversions.yml`.

Per crop `affection:` is both harvest hamper and grow-tick weight.

## TLibs paths

Cooking registers prefix `c` on enable (`CookingPathHandler`). Match is food only: `c.grain`, `c.grain(type=wheat)`, `c.vegetable(type=vegetable_1)`. Extra fields (`quality`, `origin`, `tags`) are ignored for matching.

**Create:** `c.` + FoodParser string:

```text
c.grain(type=wheat;quality=2)
c.grain(type=wheat;quality=1-3;origin=Wheat)
c.vegetable(type=vegetable_1;origin=Tomato;quality=2)
```

`quality=2` is exact. `quality=1-3` rolls uniformly in that inclusive range. Farm harvest always passes explicit quality H.

`/cooking builditem` accepts food strings with or without the `c.` prefix.

There is no `c.seed` path and no `/cooking crop` / `/cooking cropseed` admin commands.

## CustomCrops vs vanilla

| | Vanilla (hoe list) | CustomCrops |
|--|---------------------|-------------|
| Crop id | lowercase block, e.g. `wheat` | CC id, e.g. `tomato`, `nutmeg` |
| Seed path | `v.wheat_seeds`, `v.potato`, … | `ia.playbox_custom_crops:tomato_seeds` |
| Nutmeg seed | — | **`ia.playbox_custom_crops:nut_seeds`** (not `nutmeg_seeds`) |
| Produce drop | Vanilla item (`wheat`, potato, …) | `tfmc_cooking:<id>` (e.g. `tfmc_cooking:tomato`) |
| Harvest | Hoe harvest: roll H, convert produce, leave seeds | `CropBreakEvent` / mature interact: pending rewrite at H |
| Growth | `CropGrowthListener` on `BlockGrowEvent` | Cooking wraps CC grow-conditions after load; `crops.yml` affection |

CustomCrops one-seed drops stay yield/balance. No plant upsert / harvest SQL.

Cooking injects a fertility check into loaded CustomCrops grow-conditions on enable and `/customcrops reload`. CustomCrops YAML must not mention fertility. Affection comes only from `crops.yml`.

## Hoe crops in this spec

Vanilla harvest (from `farming.yml`): wheat, potatoes, carrots, beetroots, nether wart. Melon/pumpkin **fruit** harvest is out of scope; **stems** are growth-gated.

CustomCrops: every id listed under `crops:` with `source: customcrops`.

## Out of scope

- Seed quality / planted_crops SQLite
- Soil tending / quality changing while growing
