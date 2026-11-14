<div>

[English](README.ru.md) | [Русский](README.ru.md)

</div>

# AlcoMania

A small mod for **Minecraft Forge 1.20.1**, which adds an intoxication system.

## What does the mod do

| Intoxication | What's going on |
|-----------|----------------|
| 0 | Everything is fine. |
| 25+ | The camera sways slightly, and the **Sway** effect appears.|
| 50+ | The wobble is getting worse|
| 75+ | Maximum Wobble|
| > 100 | Additionally superimposed ** desiccation IV**. |

Intoxication itself decreases by **1 unit per second** (configured in `config/alcomania-common.toml`).

> Desiccation is set strictly above the `witherThreshold' (default is 100).
> Since death occurs already at `deathThreshold' (also 100), by default
> desiccation only has time to overlap at the moment of death. Lift it up
> `deathThreshold', for example up to 120, if you want the player to live and suffer from it.

## Drinks

| Item | Intoxication | Bonus | Uses | Recharge |
|---------|-----------|-------|------------|-------------|
| Apple cider ('alcomania:apple_cider') | +30 | Custom effect "Cider" (Speed II) for 30 seconds | 2 | 30 sec |
| Beer (`alcomania:beer`) | +40 | Restores 16 HP (8 hearts) | 2 | — |
| Vodka (`alcomania:vodka`) | +100 | Vodka effect: every 5 seconds for 1 second highlights entities within a radius of 16 blocks (through walls, only to the drinker) and plays a sonar sound, 30 seconds | 4 | 30 seconds |
| Wine (`alcomania:wine') | +75 | Custom effect "Wine": -1 unit to incoming damage (except starvation and drowning) and -10% to speed for 30 seconds | 4 | 30 sec |
| Whiskey (`alcomania:whisky') | +80 | Full invulnerability for 10 seconds, then guaranteed death | 3 | 30 seconds |
| Mead (`alcomania:honey`) | +30 | One-time "Honey Shield": blocks 100% damage from any source and disappears after the first absorption | 4 | 10 sec |
| Tequila (`alcomania:tequila`) | +100 | Tequila effect for 30 seconds: Projectile hits cause gorenje — 1 damage every 2 seconds. for 8 seconds, flame particles around the target and -50% incoming healing; a new hit updates the effect. Fire resistance does not protect, water does not stop the effect | 4 | — |
| Liquor (`alcomania:liqueur') | +50 | Liquor effect for 6 minutes: +50% pickaxe/axe extraction speed and 20% chance of double loot on iron/coal ore and `jeg:brimstone_ore` | 2 | 10 min |
| Brine (`alcomania:rassol`) | 0 | Removes **all** effects and resets the intoxication scale | 1 | 30 sec |

> **Survivor's Arsenal Compatibility:** if installed
> `survivorsarsenal-1.1.7_test_version.jar`, then after the last application
> any alcohol emits `survivorsarsenal:empty_glass_bottle'.
> Brine does not leave an empty bottle.

The number of uses is stored ** in the strength of the item**, not in NBT.
Therefore, the usual strength bar is visible in the inventory: it shows how many sips are left.
Drinking takes **32 ticks (1.6 seconds)** and uses the animation `UseAnim.DRINK', like potions.

Recharge of drinks **does not reset after death**: The remainder of the cooldown is transferred
to the revived player, so you cannot drink the same drink immediately after respawn.

All the numbers from the table (intoxication, applications, duration of effects, bonuses,
recharge) are displayed in the config.

> **Whiskey:** The effect lasts for 10 seconds and gives full invulnerability. When is the effect
> ends or is removed by milk/command, the player is guaranteed to die.
> At the moment of consumption, the llama spit sounds once, and during the action
> the player hears the heartbeat — only he, the other players do not hear it.
>
> **Mead:** when removing or ending the "Honey Shield", a sound plays
> shield destruction.

### Compatible with Hardcore True Darkness

Effect **`alcomania:night_vision`** ("Night Vision") is a separate effect
that supports vanilla Night Vision on the server with a short
duration and hidden icon. If Night Vision is removed, it will be added
again in the next tick.

Hardcore True Darkness sees vanilla Night Vision and turns off its darkness,
so the effect works with this mod as well. '/effect clear` correctly
removes both the custom effect and the supported Night Vision.

You can set the effect manually:

```mcfunction
/effect give @s alcomania:night_vision 60 0
```

### Spectral effect

Added a separate effect **`alcomania:spectral`** ("Spectral"),
based on vanilla Glowing:

- A player with this effect is highlighted with a turquoise hue.;
- the backlight does not show the player through the walls — he is only visible in line of sight;
- Vanilla Glowing and spectral arrows **are not changed** and work as before.

You can apply the effect with the command:

```mcfunction
/effect give @s alcomania:spectral 30 0
```

## Code structure

```
src/main/java/net/takinashida/moonshine/
├── MoonShine.java is the mod's entry point
├── Config.java — all balance and visual settings
├── ModItems.java — registration of items
├── ModEffects.java — registration of custom effects
├── ModSounds.java — registration of mod sounds
├── ModCreativeTabs.java — adding items to the creative
├── capability/
│   ├── DrunkennessCapability.java capability key
│   ├── DrunkennessInstance.java — the very meaning of intoxication
│   ├── DrunkennessProvider.java storage and preservation in NBT
│   └── CapabilityRegistration.java registration capability
├── event/
│   ├── CapabilityAttachHandler.java — issuing capability to the player and synchronization
│   ├── ModEvents.java — Sobriety, Effects, death, desiccation
│   ├── CombatEvents.Java — wine and a shield of mead
│   ├── TequilaEvents.Java gorenje and the reduction of healing from tequila
│   └── LiqueurEvents.java — mining speed and double loot from liquor
├── item/
│ ├── DrinkableItem.java — the general logic of drinking, strength, and cooldown
│   ├── AppleCider.java — apple cider
│   ├── BeerBottle.java beer (id: alcomania:beer)
│   ├── Vodka.java vodka
│   ├── Wine.java wine
│   ├── Whisky.java whiskey (invulnerability + death after effect)
│ ├── Honey.Java mead (disposable shield)
│   ├── Tequila.java — tequila (burning from shots)
│ ├── Liqueur. GorenjeJava liquor (mining speed and double loot)
│   └── Rassol.java pickle
├── effect/
│   ├── SwayEffect.java effect marker "Wobble"
│ ├── CiderEffect.java — custom effect "Cider" (Speed II)
│ ├── VodkaEffect.java — custom effect "Vodka" (illumination in a radius)
│   ├── WineEffect.java — custom effect "Wine" (-1 damage, -10% speed)
│ ├── WhiskyEffect.java Whiskey effect (invulnerability, death when removed)
│   ├── HoneyEffect.Java effect "Honey Shield" (one—time damage blocking)
│ ├── SpectralEffect.Java effect is "Spectral" (illumination without "X—ray").
TequilaEffect.java tequila buff
│   ├── TequilaBurnEffect.java — gorenje (time damage, flame + -50% healing)
│ ├── LiqueurEffect.java liquor buff
│   └── NightVisionEffect.java — "Night vision", not blocked by Darkness mods
├── network/
│   ├── Networking.java network channel
│   └── SyncDrunkennessPacket.java package "server → client"
└── client/
    ├── ClientModEvents.java registration of the HUD and configuration screen
    ├── ClientConfig.java client configuration (scale visibility)
├── MyConfigScreen.java mod settings screen
    ├── ClientData.java — client value cache
    ├── DrunkennessHUD.java scale of intoxication in the form of a bottle
    ├── DrunkennessBarHUD.java — horizontal scale of intoxication (4 segments)
├── Spectraleffecterderhandler.java illumination Spectral without "X—ray"
├── VodkaSightClientHandler.java — client highlighting of entities from vodka
    ├── WhiskySoundClientHandler.Java — Whiskey Heartbeat for player only
    └── SwayClientHandler.java — Camera wobble and blurring
```

## Blur shader (disabled)

> **Attention:** blurring is disabled now — all the code that enables it,
> commented out in `SwayClientHandler.java` and marked with `BLUER DISABLED'.
> Camera wobble continues to work.

The files are located in `src/main/resources/assets/alcomania/shaders/`:

```
post/blur.json is a chain of two passes (horizontal + vertical)
program/blur.json description of the program
program/blur.vsh — vertex shader
program/blur.fsh — fragment shader
```

**Important:** in the `post/blur.json` passages are called `alcomania:blur`, and in `program/blur.The json
`vertex` and `fragment` fields also have the namespace `alcomania`.:blur`. If you remove the namespace,
Minecraft will search for files in `minecraft:shaders/program/`, it will not find them and blur
it won't load. The amount of blur is set by the `Radius` field (currently `2.0` is a light blur).

Comments in `.vsh`/`.fsh` are specifically written in ASCII characters: not all drivers
accept non-ASCII bytes even inside GLSL comments.

## Assembly

```bash
gradlew build
```

Ready-made mod: `build/libs/alcomania-1.0.0.jar `.

## Setting up

The common game values are the file `config/alcomania-common.toml`.
HUD scale visibility — client file `config/alcomania-client.toml`:

```toml
[hud]
# Show the bottle scale in the lower left corner.
bottleVisible = true
# Show the horizontal scale above the hotbar.
barVisible = true
# Scale fill color in RGB format (0xRRGGBB).
barColor = 0xD08A2C
```

Both scales and the color of the horizontal scale can also be changed via
the "Customize" button on alcomania in the list of mods — it opens the "MyConfigScreen" screen.
The "Scale color" button switches the finished palette, and the exact RGB value
can be entered manually in "alcomania-client.toml".

The values of the general configuration are grouped into sections:

```toml
[drunkenness]
# How often to reduce intoxication (in ticks). 20 ticks = 1 second.
decayIntervalTicks = 20
# How many intoxication units to remove in one step.
decayAmount = 1
# Thresholds of effects and death.
