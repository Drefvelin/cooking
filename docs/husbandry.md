# Cooking — Animal husbandry (locked design)

This file is the live system spec. It is not a build log or implementation checklist.

Livestock lives inside **Cooking**, not a second plugin. BreedingBuddies is a reference for genetics, ownership, and mounts only. Do not port friendship, stable chunks, bundles, IRL day-change, or async simulation.

This document is the source of truth. If code and this file disagree, change the code.

## Locked split (do not blur)

| Stat | What it is | What it is not |
|------|------------|----------------|
| **Genetics** | 0–1k, from breeding (wild roll up to `initial-genetic-max`). Mounts also derive it from health/speed/jump. | Not affected by care. Not a food-quality field. |
| **Care** | 0–200. Loaded happy time raises it. Neglect lowers it. | Does not change genetics. Does not pick star quality. |
| **Hungry / Dirty** | Negative states. Stop care-up. After grace, cause decay. Block breeding. | Not friendship. Not “slept outside.” |
| **Yield** | `care / care-max` (default care/200). Multiplies **amount** (roast cuts, wool count). | Does not change star quality. |
| **Stars** | Cooking `food_quality` 1–5 from **raw genetics** via a YAML table. | Not averaged with care. |

**Effective genetics (amounts only):** `genetics * (care / care-max)`. Care 0 → 0% amount (honour `min-roast-cuts`). Care 100 / 200 → 50%. Care 200 → 100%.

## What we keep from BreedingBuddies

- BB-style breeding roll (average + variance + slowdown at high parent genetics), scaled to **0–1k** in TFMC (`max-genetics: 1000`)
- Ownership + co-ownership (token item)
- Tame + name (anvil-named tame item)
- Mount stat ranges, inherit, nerf, owner-only ride
- Neutering (all species, not only horses)
- Inspect GUI (care + yield bars, no friendship hearts, no bundle timer)

## What we do not keep

- Friendship
- Stable chunks, water/space/sleep-in-plot
- Bundles, collector item, hours-between-rewards
- IRL UTC day-change scheduler
- Async world/entity work
- MMOItems item ids (use TLibs paths like the rest of Cooking)
- `Cleanser` that deletes records because `Bukkit.getEntity` is null

## Persistence

**SQLite is the source of truth.** Main thread only. WAL mode. Write on ownership change, tick flush, death, chunk unload, plugin disable.

Entity **PDC is persistent** in Paper (saved with the entity on unload). We still do **not** treat PDC as the database. Genetics, owners, care, states, and cooldowns live in SQLite.

Never delete an **owned** animal row because the entity is unloaded. Unloaded ≠ dead. Unowned listed types **are** deleted on chunk **load** (explicit despawn), including any orphan UNTAMED row.

Owned/managed entities must `setPersistent(true)` and `setRemoveWhenFarAway(false)`.

### SQLite schema

`animals`

- `uuid` PK
- `type`, `name`
- `genetics`, `care`
- `hungry_since`, `dirty_since` (epoch millis, null if clear)
- `last_processed_at`
- `unloaded_at` (set on unload / disable; null while loaded)
- `affliction_elapsed` (loaded hours toward next state)
- `affliction_at` (this cycle’s threshold, hours)
- `last_milk_at`, `wool_ready_at`, `shed_ready_at`, `egg_ready_at`
- `mature_at` (baby growth; null = adult)
- `neutered`

`owners`

- `animal_uuid`, `player_uuid`, `role` (`owner` / `coowner`)
- unique pair
- `COUNT(*)` per player vs `max-animals`

### Entity PDC flags

Stored on the entity, not in SQLite:

- `managed` — husbandry entity; used with chunk-load wipe logic
- `nerfed` — idempotent mount nerf marker (natural/spawn-egg horses)
- `tame_name`, `linked_animal` — tame and co-own item state

### Database migrations

Migrations run once when the plugin opens `husbandry.db` (`PRAGMA user_version`). They do **not** re-run on `/cooking reload` (the DB connection stays open).

| `user_version` | Change |
|----------------|--------|
| 0 → 2 | Existing `genetics` divided by 10, capped at 1000 |
| 2 → 3 | Add `mature_at` column (null on existing rows = adult) |
| 3 → 4 | Add `shed_ready_at` column |
| 4 → 5 | Add `egg_ready_at` column |

Bred-but-not-tamed animals get an UNTAMED row in the **same loaded visit** so tame uses parent genetics. They are **not** protected across chunk load: no owner → despawn.

Tame on a world / spawn-egg animal with **no row** inserts one (wild genetics `0 … initial-genetic-max`, care 0) then claims. Admin `/cooking husbandry spawn` is optional (staff genetics/care); it is not required to tame.

On death: delete animal + owner rows. Unowned listed types drop nothing.

The BB `???` bug (6-arg constructor forcing name `???` and ignoring owner) must not return. Persist `name` in SQLite and `setCustomName` on the entity. Never default to `???`.

## Lifecycle and timing

Care simulation runs on chunk load (`HusbandrySimulator.catchUp`) and on a **1-minute tick** while the entity chunk is loaded. Egg and shed harvest run on the **1-minute tick only** (not on chunk load). Maturity is applied on load and on tick.

On chunk unload (or plugin disable), `unloaded_at` is stamped and the row is upserted. Simulation does **not** advance into the future while unloaded.

```
elapsed = now - last_processed_at
unloaded = (unloaded_at != null) ? min(elapsed, now - unloaded_at) : 0
loaded_part = elapsed - unloaded
```

**Care simulation order** (same on load catch-up and tick):

1. **Decay first, full elapsed, no cap.** If Hungry and/or Dirty already existed: after `min(since) + decay-grace`, apply `floor(decay_seconds / care.down.interval) × care.down.amount` care loss. Keep original `*_since`.
2. **Unloaded care gain** only if they were happy at unload: cap elapsed by `offline-care`, then `floor(capped_seconds / care.up.interval) × care.up.amount`. If they already had a negative, **zero** unloaded gain.
3. **Long-unload force** if `unloaded > long-unload-force`: if still no negatives, apply Hungry or Dirty 50/50 with `since = now` (grace starts on return). If they already had one, add the **other**. Do not move the old `since`. Reset affliction cycle after a force.
4. **Loaded affliction.** Only `loaded_part` (and live ticks, after `min-loaded`) add to `affliction_elapsed`. When `>= affliction_at`, apply a missing state (50/50 if neither, else the missing one). If both set, skip. Reroll `affliction_at` in `[affliction.min, affliction.max]` (default 4h–8h, mean 6h). After both states are cleared by the player, roll a new cycle so they are not instantly re-dirtied.
5. **Loaded care gain** while happy: `floor(loaded_seconds / care.up.interval) × care.up.amount`, no cap.
6. Set `last_processed_at = now`. While loaded, `unloaded_at = null`. On unload/disable, set `unloaded_at = now`.

**Tick task** (every ~1 minute, loaded animals only): after `min-loaded` visit time, run care simulation; apply maturity; run shed and egg harvest if gates pass; sync state displays; upsert row.

Chunk loaders / another player at the pen = **loaded**. The 6h affliction loop is the anti-AFK.

## Ownership

- `max-animals` default **15**. Co-ownership **counts for every** owner.
- Reject tame / co-own if that player is at cap (`cooking.admin` bypasses cap only).
- Chickens and llamas count. Bees are **vanilla** (not in the 15, not wiped).
- Tameable vanilla types (horses, etc.) must be vanilla-tamed before plugin-own.
- Tame token (anvil-named) claims the animal. No SQLite row yet: insert then claim. Existing UNTAMED row: keep genetics, then claim. Token name is applied only after a successful claim. First tame plays spore blossom particles and the enchantment-table sound; renames are silent.
- Feed and glove require ownership (or staff).
- Items: TLibs paths in `husbandry.yml` (glove, universal feed, tame, co-own). Not MMOItems.

## Care and states

Happy = no Hungry and no Dirty.

Feed (universal feed) clears Hungry (consumes 1). Glove clears Dirty (not consumed). Clearing a state does **not** add care; it only allows care-up again once both are gone.

Entity name tag while afflicted: `{name} (Hungry)`, `(Dirty)`, or `(Hungry, Dirty)` — always visible. When happy, only the base name is set and the tag is hidden (hover only). Base name stays in SQLite; suffix is applied at sync time. Legacy TextDisplay passengers are removed on sync. Neuter (`v.shears`) costs 1 durability per use. Feed/glove play firework sparkle particles and a pickup sound.

### Two clocks

Owner online/offline does not matter. What matters is whether the **entity chunk is loaded**.

| | Chunk loaded | Chunk unloaded |
|---|---|---|
| Care up | +`care.up.amount` every `care.up.interval` (default +1 / 1h) while happy, **no cap** | At most `offline-care` (default 8h), then stop |
| New Hungry/Dirty | Mean every `affliction.mean` (default 6h) of **loaded** time | Frozen, except unload **> `long-unload-force`** (default 8h) forces a state on next load |
| Decay | After `decay-grace` (24h) with a negative, −`care.down.amount` every `care.down.interval` | **Same wall-clock.** Catch-up can dump care to 0 |

### Anti-game (locked)

- You cannot max care in spawn: unload > 8h → at most +8 care and you come back Hungry or Dirty.
- You cannot max care AFK at the pen: ~6h later a state appears, gain stops, then decay after 24h.
- You cannot freeze decay by unloading: decay uses wall-clock from original `*_since`.
- Pulse-loading (touch the chunk for one tick): `loaded_part` under `min-loaded` (default 60s) counts as **still unloaded**. Care-up and affliction elapsed only apply after the animal has been loaded for that long this visit.

## Breeding

- Genetics only (BB-style average + variance + high-parent slowdown). **No care influence** on the baby.
- Cancel if either parent is Hungry, Dirty, neutered, or **still growing up**.
- Offspring get an UNTAMED SQLite row with rolled genetics for **same-session** tame. If the chunk unloads/loads before anyone owns them, they despawn and the row is deleted.
- **Baby growth:** global `grow-up` (default `1h`); optional per-species override (e.g. `CHICKEN: 45m`). Stored as `mature_at` in SQLite; `NULL` = adult. Immature animals can be tamed but cannot breed, milk, shear, lay eggs, or drop Cooking roast on death. When maturity is reached, the entity is set adult if loaded.
- Neutering: all husbandry species, owner (or staff). Neutered animals cannot breed.

## Harvest

No custom slaughter tool. If an **owned** animal **dies**, it drops configured Cooking goods. Killer does not matter (subject to damage config). Listed types with **no owner** drop nothing (no vanilla loot, no XP, no Cooking roast).

| Action | Rule |
|--------|------|
| Death | Owned mature: Cooking roast (primary) + one weighted extra from `species.drops` tiers. Immature: no Cooking roast or extras. Unowned `remove-unowned` types: no drops. |
| Quality (stars) | From **raw genetics** via YAML table → 1–5. Apply with `ItemBuilder` / existing quality PDC. Stars gate drop tiers: common always, rare 3★+, epic 4★+, legendary 5★ only. |
| Amount | From **effective genetics** (care yield). Roast `carve_remaining` (existing carve sequences / roast models 1–8). Wool extra count. Honour `min-roast-cuts`. |
| Sheep | Vanilla wool **always** on shear. If `wool_ready_at <= now`, also give custom wool, then reset the wool timer. |
| Milk | Per-animal cooldown (`milk-cooldown`, default 20m). Mature only. Quality from the **animal**. If somehow unowned, fall back to player `OriginQualityResolver` pickup. |
| Eggs | Chickens with `egg` harvest: when loaded, mature, and happy, drop one egg after `egg-timer` (default 10m) on the 1-minute tick. Item from `species.egg` (`vanilla` → `Material.EGG`; `food(...)` or TLibs path otherwise). Vanilla egg drops from managed chickens are cancelled (`EntityDropItemEvent`). Bees: vanilla, not husbandry. |
| Shed | Chickens with `shed` harvest: when loaded, mature, and happy, roll `shed-chance` after `shed-timer` (default 8h) on the 1-minute tick. Success drops `species.shed` (e.g. `v.feather`) at the animal's feet and resets the timer. |

Genetics → stars **and** amount tables both live in YAML (`husbandry.yml`). Do not hardcode thresholds.

`MilkBucketConverter` must not ignore the cow when the fill is from a husbandry animal.

## World cleanup

On chunk **load**, for each entity type in `remove-unowned` (cows, pigs, sheep, chickens, goats, horses, camels, llamas, … — **not bees**): if there is **no owner** (no row, or a row with an empty owner list), `remove()` and delete any orphan SQLite row. Spawn-egg and natural animals can be tamed only while that chunk stays loaded.

Never delete a row on unload just because `Bukkit.getEntity` is null. If an **owned** row exists but the entity is missing, keep the row (admin / later reconcile). Unowned wipe on load is an explicit despawn, not that Cleanser path.

## Mounts

Mount types in code: all `AbstractHorse` (horse, donkey, mule, camel, llama). Attributes use Paper 1.21.8 names (`MAX_HEALTH`, `MOVEMENT_SPEED`, jump strength API).

**Configured in `husbandry.yml` `mounts:` today:** HORSE, DONKEY, MULE, CAMEL. **LLAMA has no stat block yet** — breed inherit, nerf clamping, and genetics-from-stats fall back to code defaults until `mounts.LLAMA` is added.

- Per-type min/max health, speed, jump in YAML
- Stats inherit on breed (applied one tick after birth); foal genetics overwritten from normalized stats when YAML stats exist
- Natural and spawn-egg spawns nerfed when `mounts.nerf` is true (`nerf-divisor`); idempotent `nerfed` PDC prevents double-nerf
- Owner/co-owner (or staff) only ride when the mount has owners; untamed mounts stay rideable
- Genetics from normalized health/speed/jump vs configured mins/maxes (when stats exist)

## Damage

```yaml
damage:
  other-players: true    # anyone can hurt/kill; death still drops goods
  owner: true
  mobs: true
  environment: true      # fall, fire, cactus, drowning, cramming, etc.
```

Applies to entities with a SQLite row. If `other-players` is false, cancel player melee/projectiles unless the attacker is an owner/co-owner (or staff).

## GUI

Sneak empty-hand (or configured `items.inspect` TLibs path; blank = sneak-only) on a managed animal. Mounts also open the same GUI when using `items.mount-stats`.

**54-slot** double-chest inventory for all animals (livestock and mounts).

Layout uses **gray/green concrete bars** (5 centered segments each). One empty row separates the two bars. **Every** bar segment (filled and empty) shows the same three-line text: title (`Care` / `Genetics`), current/max, then yield %.

- **Row 1 (header):** status, neutered, owners, name, remove-ownership (slot 8, owners only)
- **Row 2:** empty
- **Row 3:** **Care bar** (slots 20–24): fill = `care / care-max`; each segment shows title `Care`, lore `care/care-max`, then `Yield X%`
- **Row 4:** empty separator
- **Row 5:** **Genetics bar** (slots 38–42): fill = `genetics / max-genetics`; each segment shows title `Genetics`, lore `genetics/max-genetics`, then `Yield X%`
- **Row 6 (mounts only):** health, speed, jump (slots 46, 49, 52)

**Yield X%** is the same on both bars: `round(100 × effectiveGenetics / max-genetics)` where `effectiveGenetics = floor(genetics × care / care-max)`.

Also shows: status (Happy / Hungry+Dirty + decay line), neutered, owners, growing-up timer on babies, mount stats when relevant, remove-ownership for owners.

Stars are **not** shown in the GUI (visible on slaughter quality instead). No affliction/chore timer item.

## Commands

Requires `cooking.admin`:

- `/cooking husbandry spawn <type> [genetics] [care]` — spawn untamed animal with SQLite row
- `/cooking husbandry save` — upsert all currently loaded animals and checkpoint WAL

Reload config (including `husbandry.yml`) via `/cooking reload`. This reloads YAML only; it does **not** reopen the SQLite database.

No `/breedingbuddies`. Unowned admin-spawned animals despawn on chunk load until tamed.

## Architecture

Package: `net.tfminecraft.cooking.husbandry`. Config: `husbandry.yml`. Items via TLibs paths in that file.

No async entity or SQLite access. All husbandry logic runs on the main thread.

| Area | Primary classes |
|------|-----------------|
| DB | `HusbandryRepository`, `HusbandryAnimal` |
| Lifecycle | `HusbandryLifecycleListener`, `HusbandryTickTask` |
| Care | `HusbandrySimulator`, `HusbandryCareListener`, `HusbandryStateDisplay` (entity name + visibility sync) |
| Ownership | `HusbandryOwnershipService`, `HusbandryTamingListener` |
| Breed / growth | `HusbandryBreedListener`, `HusbandryNeuterListener`, `HusbandryGrowth`, `HusbandryGenetics` |
| Harvest | `HusbandryDeathListener`, `HusbandryHarvestListener`, `HusbandryHarvest`, `HusbandryShed`, `HusbandryEggs`, `HusbandryDropRoller` |
| Mounts / damage | `HusbandryMounts`, `HusbandryNerfListener`, `HusbandryMountListener`, `HusbandryDamageListener` |
| GUI | `HusbandryInspectGui`, `HusbandryInspectListener`, `HusbandryGuiBars` |

## Configuration

Global defaults in `husbandry.yml`:

```yaml
max-animals: 15
care-max: 200

min-loaded: 60s
decay-grace: 24h
offline-care: 8h
long-unload-force: 8h
milk-cooldown: 20m
wool-timer: 8h
shed-timer: 8h
shed-chance: 0.15
egg-timer: 10m

affliction:
  mean: 6h
  min: 4h
  max: 8h

care:
  up:
    interval: 1h
    amount: 1
  down:
    interval: 1h
    amount: 1

grow-up: 1h

items:
  tame: m.pets.taming_item
  co-own: m.pets.coownership_item
  feed: m.pets.universal_feed
  glove: m.pets.caring_glove
  neuter: v.shears
  inspect: ""
  mount-stats: m.pets.carrot_on_a_stick

initial-genetic-max: 20
max-genetics: 1000
min-roast-cuts: 1

# Per-species grow-up override example:
# species:
#   CHICKEN:
#     grow-up: 45m
```

Durations use TLibs strings (`8h`, `20m`, `60s`). Legacy `*-hours` / `*-minutes` / `care-*-per-hour` keys still load with a one-time warning.

### Per-species keys

Under `species.<TYPE>`:

| Key | Purpose |
|-----|---------|
| `harvest` | List of modes: `slaughter`, `milk`, `shear`, `egg`, `shed` |
| `slaughter` | `food(...)` roast string for death drop |
| `milk` | `food(...)` milk bucket string |
| `shear` | TLibs path for extra wool |
| `egg` | `vanilla`, `food(...)`, or TLibs path |
| `shed` | TLibs/vanilla path for feather shed |
| `drops` | Star-gated tier tables (`common`, `rare`, `epic`, `legendary`) |
| `grow-up` | Optional per-species baby duration override |

Also configured: `remove-unowned` (wipe list), `quality-from-genetics`, `amount-from-genetics`, `breeding.*`, `damage.*`, `mounts.*`.
