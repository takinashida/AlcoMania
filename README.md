<div>

[English](README.md) | [Русский](README.ru.md)

</div>

# AlcoMania

A small mod for **Minecraft Forge 1.20.1** that adds a drunkenness (intoxication) system.

- Minecraft **1.20.1**
- Forge **47.4.28** or newer (47.x)
- Java **17**
- Mod ID: `alcomania`
- Current version: **1.0.2**

## What the mod does

| Drunkenness | What happens |
|-------------|--------------|
| 0 | Everything is normal. |
| 25+ | The camera sways slightly and the **Drunkenness** effect (`alcomania:sway`) appears. |
| 50+ | The sway becomes stronger (second tier). |
| 75+ | The sway is at its maximum (third tier). |
| > 100 | **Wither IV** is applied on top. |

Drunkenness decreases by **1 point per second** by default (`config/alcomania-common.toml`).

The sway strength is tiered, not continuous: it stays constant inside a tier and
increases when the next threshold is crossed. Transitions are smoothed, so the
camera does not jitter.

> **About `deathThreshold`:** in the current code the direct kill at
> `deathThreshold` is commented out in `ModEvents`. The value is still used as
> the maximum of the HUD gauges; the third sway tier stays at maximum above 75.
> The actual damage comes from Wither, which is applied when drunkenness is
> **strictly greater** than `witherThreshold` (default `100`). With the defaults
> both values are `100`, so Wither only appears after the gauge is completely full.

## Drinks

| Item | Drunkenness | Bonus | Uses | Cooldown |
|------|-------------|-------|------|----------|
| Apple cider (`alcomania:apple_cider`) | +30 | **Cider** effect: +30% movement speed for 30 s (each +1 to `speedAmplifier` adds another +30%) | 2 | 30 s |
| Beer (`alcomania:beer`) | +40 | Restores 12 HP (6 hearts) | 2 | — |
| Ale (`alcomania:ale`) | +40 | **Ale** effect for 8 s; when it expires naturally, restores 12 HP | 2 | 10 s |
| Vodka (`alcomania:vodka`) | +75 | **Vodka** effect for 30 s: every 5 s a sonar ping sounds, then for 1 s all entities within 40 blocks glow through walls (only the drinker sees them) | 4 | 30 s |
| Wine (`alcomania:wine`) | +75 | **Wine** effect for 30 s: −1 point of incoming damage (except starvation and drowning) and −10% movement speed | 4 | 30 s |
| Whisky (`alcomania:whisky`) | +75 | Full invulnerability for 10 s, then guaranteed death | 2 | 30 s |
| Mead (`alcomania:honey`) | +30 | One-time **Mead Shield**: blocks 100% of damage from any source until the first absorbed hit (or 30 s) | 4 | — |
| Tequila (`alcomania:tequila`) | +75 | **Tequila** effect for 30 s: your hits apply **Burning** — 1 damage every 2 s for 8 s, flame particles and −50% incoming healing. A new hit refreshes it. Fire Resistance does not help and water does not stop it | 4 | 30 s |
| Herbal liqueur (`alcomania:herbal_liqueur`) | +50 | **Herbal Liqueur** effect for 6 min: +50% pickaxe/axe mining speed and a 20% chance of one extra drop from iron/coal ore and `jeg:brimstone_ore` | 2 | 10 min |
| Anti-hangover / brine (`alcomania:rassol`) | 0 | Removes **all** effects and resets drunkenness to 0 | 1 | 30 s |

> **Ale** heals only when its effect expires naturally. Removing the effect with
> milk or `/effect` cancels the heal.

> **Tequila's Burning** is applied by any damage dealt by a player under the
> Tequila effect, not only by projectiles. While the target burns, any new hit
> refreshes the duration. The damage is magic damage, so Fire Resistance does
> not reduce it.

The number of uses is stored **in the item's durability**, not in NBT, so the
usual durability bar in the inventory shows how many sips are left. The tooltip
also shows the remaining uses, the drunkenness value and the cooldown.

Drinking takes **32 ticks (1.6 seconds)** and uses the `UseAnim.DRINK`
animation, like potions.

Drink cooldowns **do not reset on death**: the remaining cooldown is copied to
the respawned player by `CooldownEvents`, so you cannot drink the same bottle
immediately after respawning.

> **Survivor's Arsenal compatibility:** if
> `survivorsarsenal-1.1.7_test_version.jar` is installed, the last sip of any
> alcoholic drink drops `survivorsarsenal:empty_glass_bottle`.
> Anti-hangover does not leave an empty bottle.

All numbers from the table (drunkenness, uses, effect durations, bonuses,
cooldowns) are configurable.

> **Whisky:** the effect lasts 10 seconds and grants full invulnerability. When
> it ends, or is removed by milk/a command, the player dies. Only
> `genericKill` damage (for example `/kill` or the mod's own kill) bypasses the
> invulnerability. A llama spit sound plays once when the bottle is drunk, and
> during the effect the player hears a heartbeat — only they can hear it.
>
> **Mead:** when the Mead Shield expires or is removed, the shield-breaking
> sound plays.

### Hardcore True Darkness compatibility

The **`alcomania:night_vision`** effect ("Night Vision") is a separate effect
that keeps vanilla Night Vision active on the server with a short duration and
a hidden icon. If vanilla Night Vision is removed, it is added again on the
next tick. Hardcore True Darkness sees the vanilla Night Vision and disables
its darkness, so the effect works together with that mod. `/effect clear`
correctly removes both the custom effect and the supported Night Vision.

You can apply the effect manually:

```mcfunction
/effect give @s alcomania:night_vision 60 0
```

### Spectral effect

The **`alcomania:spectral`** effect ("Spectral") is based on vanilla Glowing:

- a player with this effect is highlighted with a turquoise tint;
- the highlight does not show the player through walls — they are only visible
  in line of sight;
- vanilla Glowing and spectral arrows are **not modified** and work as before.

Apply it with:

```mcfunction
/effect give @s alcomania:spectral 30 0
```

## Code structure

```
src/main/java/net/takinashida/moonshine/
├── MoonShine.java                       — mod entry point
├── Config.java                          — all balance and visual settings
├── ModItems.java                        — item registration
├── ModEffects.java                      — custom effect registration
├── ModSounds.java                       — mod sound registration
├── ModCreativeTabs.java                 — items in the creative Food & Drinks tab
├── capability/
│   ├── DrunkennessCapability.java       — capability key
│   ├── DrunkennessInstance.java         — the drunkenness value itself
│   ├── DrunkennessProvider.java         — storage and NBT persistence
│   └── CapabilityRegistration.java      — capability registration
├── event/
│   ├── CapabilityAttachHandler.java     — attaches the capability and syncs it
│   ├── ModEvents.java                   — decay, threshold effects, wither, whisky/mead/night vision hooks
│   ├── CombatEvents.java                — wine damage reduction and mead shield
│   ├── TequilaEvents.java               — burning application and healing reduction
│   ├── AleEvents.java                   — healing when the Ale effect expires
│   ├── HerbalLiqueurEvents.java         — mining speed and double loot
│   └── CooldownEvents.java              — keeps drink cooldowns after death
├── item/
│   ├── DrinkableItem.java               — shared drinking, durability and cooldown logic
│   ├── AppleCider.java                  — apple cider
│   ├── BeerBottle.java                  — beer (id: alcomania:beer)
│   ├── Ale.java                         — ale
│   ├── Vodka.java                       — vodka
│   ├── Wine.java                        — wine
│   ├── Whisky.java                      — whisky (invulnerability + death after the effect)
│   ├── Honey.java                       — mead (one-time shield)
│   ├── Tequila.java                     — tequila (burning from hits)
│   ├── HerbalLiqueur.java               — herbal liqueur (mining speed and double loot)
│   └── Rassol.java                      — anti-hangover / brine
├── effect/
│   ├── SwayEffect.java                  — "Drunkenness" marker effect
│   ├── CiderEffect.java                 — "Cider" effect (+30% speed per level)
│   ├── AleEffect.java                   — "Ale" timer effect
│   ├── VodkaEffect.java                 — "Vodka" effect (highlight pulse)
│   ├── WineEffect.java                  — "Wine" effect (−1 damage, −10% speed)
│   ├── WhiskyEffect.java                — "Whisky" effect (invulnerability, death on removal)
│   ├── HoneyEffect.java                 — "Mead Shield" effect (one-time damage block)
│   ├── SpectralEffect.java              — "Spectral" effect (highlight without X-ray)
│   ├── TequilaEffect.java               — tequila buff marker
│   ├── TequilaBurnEffect.java           — burning (damage over time, flames, −50% healing)
│   ├── HerbalLiqueurEffect.java         — herbal liqueur buff marker
│   └── NightVisionEffect.java           — "Night Vision" that Darkness mods do not block
├── network/
│   ├── Networking.java                  — network channel
│   └── SyncDrunkennessPacket.java       — "server → client" packet
└── client/
    ├── ClientModEvents.java             — registers the HUD and the config screen
    ├── ClientConfig.java                — client config (HUD visibility and color)
    ├── MyConfigScreen.java              — mod settings screen
    ├── ClientData.java                  — client-side value cache
    ├── DrunkennessHUD.java              — bottle-shaped drunkenness gauge
    ├── DrunkennessBarHUD.java           — horizontal drunkenness bar (4 segments)
    ├── SpectralEffectRenderHandler.java — Spectral highlight without X-ray
    ├── VodkaSightClientHandler.java     — client-side entity highlight from vodka
    ├── WhiskySoundClientHandler.java    — whisky heartbeat, only for the player
    └── SwayClientHandler.java           — camera sway
```

## Blur effect (removed)

Camera sway works, but the screen blur is disabled: the code that enabled it is
commented out in `SwayClientHandler.java` and marked `БЛЮР ОТКЛЮЧЁН`. The old
shader files under `assets/alcomania/shaders/` were removed in 1.0.2.

To bring blur back, recreate the post-processing shader files and re-enable the
commented blocks. The comments in `SwayClientHandler` still refer to the old
`assets/moonshine/shaders/...` paths, so those paths must be updated to
`assets/alcomania/shaders/...` as well.

## Building

```bash
gradlew build
```

On Windows you can also use `gradlew.bat build`. The built mod is placed at
`build/libs/alcomania-1.0.2.jar`.

## Configuration

Common gameplay values live in `config/alcomania-common.toml`. HUD visibility
and color live in the client file `config/alcomania-client.toml`:

```toml
[hud]
# Show the bottle-shaped gauge in the bottom-left corner.
bottleVisible = true
# Show the horizontal bar above the hotbar.
barVisible = true
# Bar fill color in RGB format (0xRRGGBB).
barColor = 0xD08A2C
```

Both gauges and the bar color can also be changed with the **Config** button
for Alcomania in the mod list, which opens `MyConfigScreen`. The "Bar color"
button cycles through preset colors; an exact RGB value can be entered manually
in `alcomania-client.toml`.

The common config is grouped into sections. Defaults:

```toml
[drunkenness]
# How often drunkenness decreases, in ticks. 20 ticks = 1 second.
decayIntervalTicks = 20
# How many points are removed per step.
decayAmount = 1
# Sway starts here (and the Drunkenness effect is applied).
swayThreshold = 25
# Second and third sway tiers.
swayTier2Threshold = 50
swayTier3Threshold = 75
# Maximum of the HUD gauges; the third sway tier stays max above 75.
deathThreshold = 100
# Wither is applied when drunkenness is STRICTLY greater than this.
witherThreshold = 100
# 0 = Wither I, 1 = II, 2 = III, 3 = IV.
witherAmplifier = 3
# Duration of threshold effects, in ticks.
effectDurationTicks = 200

[visual]
# Maximum camera sway amplitudes, in degrees.
swayYawDegrees = 9.0
swayPitchDegrees = 6.0
swayRollDegrees = 3.0
# Sway strength per tier (fraction of the maximum amplitude).
swayTier1Strength = 0.35
swayTier2Strength = 0.70
swayTier3Strength = 1.00
# Liquid wave animation in the bottle gauge.
hudWaveAmplitudePixels = 2.0
hudWaveLengthPixels = 18.0
hudWaveSpeed = 0.1

[items.cider]
drunkenness = 30
uses = 2
cooldownSeconds = 30
effectDurationSeconds = 30
speedAmplifier = 0

[items.beer]
drunkenness = 40
uses = 2
cooldownSeconds = 0
healAmount = 12

[items.ale]
drunkenness = 40
uses = 2
cooldownSeconds = 10
effectDurationSeconds = 8
healAmount = 12

[items.vodka]
drunkenness = 75
uses = 4
cooldownSeconds = 30
effectDurationSeconds = 30
glowRadius = 40

[items.wine]
drunkenness = 75
uses = 4
cooldownSeconds = 30
effectDurationSeconds = 30
damageReduction = 1
speedPenaltyPercent = 10

[items.rassol]
uses = 1
cooldownSeconds = 30

[items.whisky]
drunkenness = 75
uses = 2
cooldownSeconds = 30
effectDurationSeconds = 10

[items.honey]
drunkenness = 30
uses = 4
cooldownSeconds = 0
effectDurationSeconds = 30

[items.tequila]
drunkenness = 75
uses = 4
cooldownSeconds = 30
effectDurationSeconds = 30
burnDurationSeconds = 8
burnIntervalSeconds = 2
burnDamage = 1
healingReductionPercent = 50

[items.herbal_liqueur]
drunkenness = 50
uses = 2
cooldownSeconds = 600
effectDurationSeconds = 360
miningSpeedBonusPercent = 50
doubleLootChancePercent = 20
```

## License

MIT.
