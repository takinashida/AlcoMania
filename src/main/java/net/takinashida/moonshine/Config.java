package net.takinashida.moonshine;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Mod config (config/alcomania-common.toml in the game directory).
 *
 * All gameplay numbers live here:
 *  - drunkenness decay speed;
 *  - effect, wither and death thresholds;
 *  - drink stats (drunkenness, uses, cooldown, bonuses);
 *  - camera sway strength.
 *
 * Values are read in {@link #onLoad(ModConfigEvent)} and cached in static
 * fields, so gameplay code does not query the config every tick/frame.
 *
 * IMPORTANT: items are created before the config is loaded, so the number
 * of uses (durability) is read dynamically in DrinkableItem#getMaxDamage
 * instead of the constructor. Config edits are therefore applied without
 * restarting the game.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {

    /** Config spec builder. */
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // =========================================================================
    //  DRUNKENNESS
    // =========================================================================
    private static final ForgeConfigSpec.IntValue DECAY_INTERVAL_TICKS = BUILDER
            .comment("How often (in ticks) drunkenness decreases. 20 ticks = 1 second.")
            .defineInRange("drunkenness.decayIntervalTicks", 20, 1, 200);

    private static final ForgeConfigSpec.IntValue DECAY_AMOUNT = BUILDER
            .comment("How many drunkenness points are removed per decay step.")
            .defineInRange("drunkenness.decayAmount", 1, 1, 100);

    private static final ForgeConfigSpec.IntValue SWAY_THRESHOLD = BUILDER
            .comment("Drunkenness at which camera sway starts and the Sway effect is applied.",
                    "This is also the start of the first (weak) sway tier.")
            .defineInRange("drunkenness.swayThreshold", 25, 1, 10000);

    private static final ForgeConfigSpec.IntValue SWAY_TIER2_THRESHOLD = BUILDER
            .comment("Drunkenness at which sway switches to the middle tier.",
                    "Strength is constant within a tier and does not depend on every point.",
                    "Transitions are smoothed, so the camera does not jitter.")
            .defineInRange("drunkenness.swayTier2Threshold", 50, 1, 10000);

    private static final ForgeConfigSpec.IntValue SWAY_TIER3_THRESHOLD = BUILDER
            .comment("Drunkenness at which sway becomes maximum.",
                    "Strength does not increase further up to deathThreshold.")
            .defineInRange("drunkenness.swayTier3Threshold", 75, 1, 10000);

    private static final ForgeConfigSpec.IntValue DEATH_THRESHOLD = BUILDER
            .comment("Drunkenness at which the player instantly have wither effect(value >= this number).",
                    "The same value is used as the HUD bar maximum and camera sway maximum.")
            .defineInRange("drunkenness.deathThreshold", 100, 1, 10000);

    private static final ForgeConfigSpec.IntValue WITHER_THRESHOLD = BUILDER
            .comment("If drunkenness is STRICTLY GREATER than this value, Wither is applied.",
                    "By default it equals deathThreshold (100), so Wither is applied",
                    "on the same tick the player dies. Raise deathThreshold above this",
                    "value (for example to 120) if you want the player to survive and",
                    "actually suffer from Wither.")
            .defineInRange("drunkenness.witherThreshold", 100, 0, 10000);

    private static final ForgeConfigSpec.IntValue WITHER_AMPLIFIER = BUILDER
            .comment("Wither strength: 0 = Wither I, 1 = II, 2 = III, 3 = IV (default).")
            .defineInRange("drunkenness.witherAmplifier", 3, 0, 9);

    private static final ForgeConfigSpec.IntValue EFFECT_DURATION_TICKS = BUILDER
            .comment("Duration (in ticks) of threshold effects. 200 ticks = 10 seconds.",
                    "Effects are refreshed only when less than half their duration remains,",
                    "so packets are not sent every tick.")
            .defineInRange("drunkenness.effectDurationTicks", 200, 20, 12000);

    // =========================================================================
    //  VISUAL: CAMERA SWAY
    // =========================================================================
    private static final ForgeConfigSpec.DoubleValue SWAY_YAW_DEGREES = BUILDER
            .comment("Maximum left-right camera sway amplitude (in degrees).")
            .defineInRange("visual.swayYawDegrees", 14.0D, 0.0D, 45.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_PITCH_DEGREES = BUILDER
            .comment("Maximum up-down camera sway amplitude (in degrees).")
            .defineInRange("visual.swayPitchDegrees", 8.0D, 0.0D, 45.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_ROLL_DEGREES = BUILDER
            .comment("Maximum horizon roll (tilt) in degrees.")
            .defineInRange("visual.swayRollDegrees", 6.0D, 0.0D, 45.0D);

    // ---- Sway strength tiers ----
    private static final ForgeConfigSpec.DoubleValue SWAY_TIER1_STRENGTH = BUILDER
            .comment("Sway strength on the first tier (swayThreshold..swayTier2Threshold).",
                    "Fraction of maximum amplitude: 0.35 = 35% of swayYawDegrees/swayPitchDegrees/swayRollDegrees.")
            .defineInRange("visual.swayTier1Strength", 0.35D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_TIER2_STRENGTH = BUILDER
            .comment("Sway strength on the second tier (swayTier2Threshold..swayTier3Threshold).")
            .defineInRange("visual.swayTier2Strength", 0.7D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_TIER3_STRENGTH = BUILDER
            .comment("Sway strength on the third tier (swayTier3Threshold and above).",
                    "1.0 = full amplitude from swayYawDegrees/swayPitchDegrees/swayRollDegrees.")
            .defineInRange("visual.swayTier3Strength", 1.0D, 0.0D, 1.0D);

    // =========================================================================
    //  VISUAL: HUD WAVE ANIMATION
    // =========================================================================
    private static final ForgeConfigSpec.DoubleValue HUD_WAVE_AMPLITUDE_PIXELS = BUILDER
            .comment("Liquid wave amplitude in the HUD bottle, in pixels.",
                    "0 = flat surface, 2 = noticeable rippling.")
            .defineInRange("visual.hudWaveAmplitudePixels", 2.0D, 0.0D, 8.0D);

    private static final ForgeConfigSpec.DoubleValue HUD_WAVE_LENGTH_PIXELS = BUILDER
            .comment("Length of one wave across the bottle width, in pixels.",
                    "Smaller value = more waves on the surface.")
            .defineInRange("visual.hudWaveLengthPixels", 18.0D, 4.0D, 128.0D);

    private static final ForgeConfigSpec.DoubleValue HUD_WAVE_SPEED = BUILDER
            .comment("Wave movement speed in radians per tick.",
                    "0.1 = roughly one full period every 3 seconds.")
            .defineInRange("visual.hudWaveSpeed", 0.1D, 0.0D, 2.0D);

    // HUD visibility is handled by the client config (see ClientConfig).

    // =========================================================================
    //  DRINKS
    // =========================================================================

    // ---- Apple cider ----
    private static final ForgeConfigSpec.IntValue CIDER_DRUNKENNESS = BUILDER
            .comment("Apple cider: drunkenness per drink.")
            .defineInRange("items.cider.drunkenness", 30, 0, 1000);

    private static final ForgeConfigSpec.IntValue CIDER_USES = BUILDER
            .comment("Apple cider: number of uses (item durability).")
            .defineInRange("items.cider.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue CIDER_COOLDOWN_SECONDS = BUILDER
            .comment("Apple cider: cooldown between sips in seconds.")
            .defineInRange("items.cider.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue CIDER_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Apple cider: Cider effect duration in seconds.")
            .defineInRange("items.cider.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue CIDER_SPEED_AMPLIFIER = BUILDER
            .comment("Apple cider: effect amplifier. 0 = Speed I, 1 = Speed II (default).")
            .defineInRange("items.cider.speedAmplifier", 0, 0, 9);

    // ---- Beer ----
    private static final ForgeConfigSpec.IntValue BEER_DRUNKENNESS = BUILDER
            .comment("Beer: drunkenness per drink.")
            .defineInRange("items.beer.drunkenness", 40, 0, 1000);

    private static final ForgeConfigSpec.IntValue BEER_USES = BUILDER
            .comment("Beer: number of uses (item durability).")
            .defineInRange("items.beer.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue BEER_COOLDOWN_SECONDS = BUILDER
            .comment("Beer: cooldown between sips in seconds. 0 = no cooldown (default).")
            .defineInRange("items.beer.cooldownSeconds", 0, 0, 3600);

    private static final ForgeConfigSpec.IntValue BEER_HEAL_AMOUNT = BUILDER
            .comment("Beer: HP restored per drink (2 HP = 1 heart).")
            .defineInRange("items.beer.healAmount", 12, 0, 100);

    // ---- Vodka ----
    private static final ForgeConfigSpec.IntValue VODKA_DRUNKENNESS = BUILDER
            .comment("Vodka: drunkenness per drink.")
            .defineInRange("items.vodka.drunkenness", 75, 0, 1000);

    private static final ForgeConfigSpec.IntValue VODKA_USES = BUILDER
            .comment("Vodka: number of uses (item durability).")
            .defineInRange("items.vodka.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue VODKA_COOLDOWN_SECONDS = BUILDER
            .comment("Vodka: cooldown between sips in seconds.")
            .defineInRange("items.vodka.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue VODKA_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Vodka: Vodka effect duration in seconds.")
            .defineInRange("items.vodka.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue VODKA_GLOW_RADIUS = BUILDER
            .comment("Vodka: radius (in blocks) in which entities are highlighted for the drinker.")
            .defineInRange("items.vodka.glowRadius", 40, 1, 64);

    // ---- Wine ----
    private static final ForgeConfigSpec.IntValue WINE_DRUNKENNESS = BUILDER
            .comment("Wine: drunkenness per drink.")
            .defineInRange("items.wine.drunkenness", 75, 0, 1000);

    private static final ForgeConfigSpec.IntValue WINE_USES = BUILDER
            .comment("Wine: number of uses (item durability).")
            .defineInRange("items.wine.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue WINE_COOLDOWN_SECONDS = BUILDER
            .comment("Wine: cooldown between sips in seconds.")
            .defineInRange("items.wine.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue WINE_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Wine: Wine effect duration in seconds.")
            .defineInRange("items.wine.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue WINE_DAMAGE_REDUCTION = BUILDER
            .comment("Wine: damage subtracted from every incoming hit.",
                    "Does not affect hunger or drowning damage.")
            .defineInRange("items.wine.damageReduction", 1, 0, 100);

    private static final ForgeConfigSpec.IntValue WINE_SPEED_PENALTY_PERCENT = BUILDER
            .comment("Wine: movement speed penalty in percent (10 = -10%).")
            .defineInRange("items.wine.speedPenaltyPercent", 10, 0, 100);

    // ---- Rassol ----
    private static final ForgeConfigSpec.IntValue RASSOL_USES = BUILDER
            .comment("Rassol: number of uses (item durability).")
            .defineInRange("items.rassol.uses", 1, 1, 64);

    private static final ForgeConfigSpec.IntValue RASSOL_COOLDOWN_SECONDS = BUILDER
            .comment("Rassol: cooldown between sips in seconds.")
            .defineInRange("items.rassol.cooldownSeconds", 30, 0, 3600);

    // ---- Whisky ----
    private static final ForgeConfigSpec.IntValue WHISKY_DRUNKENNESS = BUILDER
            .comment("Whisky: drunkenness per drink.")
            .defineInRange("items.whisky.drunkenness", 75, 0, 1000);

    private static final ForgeConfigSpec.IntValue WHISKY_USES = BUILDER
            .comment("Whisky: number of uses (item durability).")
            .defineInRange("items.whisky.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue WHISKY_COOLDOWN_SECONDS = BUILDER
            .comment("Whisky: cooldown between sips in seconds.")
            .defineInRange("items.whisky.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue WHISKY_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Whisky: Whisky effect duration in seconds.")
            .defineInRange("items.whisky.effectDurationSeconds", 10, 1, 3600);

    // ---- Mead ----
    private static final ForgeConfigSpec.IntValue HONEY_DRUNKENNESS = BUILDER
            .comment("Mead: drunkenness per drink.")
            .defineInRange("items.honey.drunkenness", 30, 0, 1000);

    private static final ForgeConfigSpec.IntValue HONEY_USES = BUILDER
            .comment("Mead: number of uses (item durability).")
            .defineInRange("items.honey.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue HONEY_COOLDOWN_SECONDS = BUILDER
            .comment("Mead: cooldown between sips in seconds.")
            .defineInRange("items.honey.cooldownSeconds", 0, 0, 3600);

    private static final ForgeConfigSpec.IntValue HONEY_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Mead: Mead Shield effect duration in seconds.")
            .defineInRange("items.honey.effectDurationSeconds", 30, 1, 64);

    // ---- Tequila ----
    private static final ForgeConfigSpec.IntValue TEQUILA_DRUNKENNESS = BUILDER
            .comment("Tequila: drunkenness per drink.")
            .defineInRange("items.tequila.drunkenness", 75, 0, 1000);

    private static final ForgeConfigSpec.IntValue TEQUILA_USES = BUILDER
            .comment("Tequila: number of uses (item durability).")
            .defineInRange("items.tequila.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue TEQUILA_COOLDOWN_SECONDS = BUILDER
            .comment("Tequila: cooldown between sips in seconds. 0 = no cooldown.")
            .defineInRange("items.tequila.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Tequila: Tequila effect duration in seconds.")
            .defineInRange("items.tequila.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_BURN_DURATION_SECONDS = BUILDER
            .comment("Tequila: shot-induced burning duration in seconds.")
            .defineInRange("items.tequila.burnDurationSeconds", 8, 1, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_BURN_INTERVAL_SECONDS = BUILDER
            .comment("Tequila: interval between burning damage ticks in seconds.")
            .defineInRange("items.tequila.burnIntervalSeconds", 2, 1, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_BURN_DAMAGE = BUILDER
            .comment("Tequila: damage dealt by one burning tick.")
            .defineInRange("items.tequila.burnDamage", 1, 0, 1000);

    private static final ForgeConfigSpec.IntValue TEQUILA_HEALING_REDUCTION_PERCENT = BUILDER
            .comment("Tequila: incoming healing reduction for burning targets in percent (50 = -50%).")
            .defineInRange("items.tequila.healingReductionPercent", 50, 0, 100);

    // ---- Liqueur ----
    private static final ForgeConfigSpec.IntValue LIQUEUR_DRUNKENNESS = BUILDER
            .comment("Liqueur: drunkenness per drink.")
            .defineInRange("items.liqueur.drunkenness", 50, 0, 1000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_USES = BUILDER
            .comment("Liqueur: number of uses (item durability).")
            .defineInRange("items.liqueur.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue LIQUEUR_COOLDOWN_SECONDS = BUILDER
            .comment("Liqueur: cooldown between sips in seconds (10 minutes = 600).")
            .defineInRange("items.liqueur.cooldownSeconds", 600, 0, 36000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Liqueur: Liqueur effect duration in seconds (6 minutes = 360).")
            .defineInRange("items.liqueur.effectDurationSeconds", 360, 1, 36000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_MINING_SPEED_BONUS_PERCENT = BUILDER
            .comment("Liqueur: pickaxe/axe mining speed bonus in percent (50 = +50%).")
            .defineInRange("items.liqueur.miningSpeedBonusPercent", 50, 0, 1000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_DOUBLE_LOOT_CHANCE_PERCENT = BUILDER
            .comment("Liqueur: double loot chance for iron/coal ore and jeg:brimstone_ore.")
            .defineInRange("items.liqueur.doubleLootChancePercent", 20, 0, 100);

    /** Final config spec registered by MoonShine. */
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // =========================================================================
    //  CACHED VALUES (defaults used before the config is loaded)
    // =========================================================================
    public static int decayIntervalTicks = 20;
    public static int decayAmount = 1;
    public static int swayThreshold = 25;
    public static int swayTier2Threshold = 50;
    public static int swayTier3Threshold = 75;
    public static int deathThreshold = 100;
    public static int witherThreshold = 100;
    public static int witherAmplifier = 3;
    public static int effectDurationTicks = 200;

    public static double swayYawDegrees = 14.0D;
    public static double swayPitchDegrees = 8.0D;
    public static double swayRollDegrees = 6.0D;
    public static double swayTier1Strength = 0.35D;
    public static double swayTier2Strength = 0.7D;
    public static double swayTier3Strength = 1.0D;

    public static double hudWaveAmplitudePixels = 2.0D;
    public static double hudWaveLengthPixels = 18.0D;
    public static double hudWaveSpeed = 0.1D;

    public static int ciderDrunkenness = 30;
    public static int ciderUses = 2;
    public static int ciderCooldownSeconds = 30;
    public static int ciderEffectDurationSeconds = 30;
    public static int ciderSpeedAmplifier = 1;

    public static int beerDrunkenness = 40;
    public static int beerUses = 2;
    public static int beerCooldownSeconds = 0;
    public static int beerHealAmount = 12;

    public static int vodkaDrunkenness = 75;
    public static int vodkaUses = 4;
    public static int vodkaCooldownSeconds = 30;
    public static int vodkaEffectDurationSeconds = 30;
    public static int vodkaGlowRadius = 40;

    public static int wineDrunkenness = 75;
    public static int wineUses = 4;
    public static int wineCooldownSeconds = 30;
    public static int wineEffectDurationSeconds = 30;
    public static int wineDamageReduction = 1;
    public static int wineSpeedPenaltyPercent = 10;

    public static int rassolUses = 1;
    public static int rassolCooldownSeconds = 30;

    public static int whiskyDrunkenness = 75;
    public static int whiskyUses = 2;
    public static int whiskyCooldownSeconds = 600;
    public static int whiskyEffectDurationSeconds = 10;

    public static int honeyDrunkenness = 30;
    public static int honeyUses = 4;
    public static int honeyCooldownSeconds = 10;
    public static int honeyEffectDurationSeconds = 30;

    public static int tequilaDrunkenness = 75;
    public static int tequilaUses = 4;
    public static int tequilaCooldownSeconds = 30;
    public static int tequilaEffectDurationSeconds = 30;
    public static int tequilaBurnDurationSeconds = 8;
    public static int tequilaBurnIntervalSeconds = 2;
    public static int tequilaBurnDamage = 1;
    public static int tequilaHealingReductionPercent = 50;

    public static int liqueurDrunkenness = 50;
    public static int liqueurUses = 2;
    public static int liqueurCooldownSeconds = 600;
    public static int liqueurEffectDurationSeconds = 360;
    public static int liqueurMiningSpeedBonusPercent = 50;
    public static int liqueurDoubleLootChancePercent = 20;

    private Config() {
    }

    /** Called by Forge when the config is loaded or reloaded. */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // The MOD bus also receives client config events; react only to our spec.
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        decayIntervalTicks = DECAY_INTERVAL_TICKS.get();
        decayAmount = DECAY_AMOUNT.get();
        swayThreshold = SWAY_THRESHOLD.get();
        swayTier2Threshold = SWAY_TIER2_THRESHOLD.get();
        swayTier3Threshold = SWAY_TIER3_THRESHOLD.get();
        deathThreshold = DEATH_THRESHOLD.get();
        witherThreshold = WITHER_THRESHOLD.get();
        witherAmplifier = WITHER_AMPLIFIER.get();
        effectDurationTicks = EFFECT_DURATION_TICKS.get();

        swayYawDegrees = SWAY_YAW_DEGREES.get();
        swayPitchDegrees = SWAY_PITCH_DEGREES.get();
        swayRollDegrees = SWAY_ROLL_DEGREES.get();
        swayTier1Strength = SWAY_TIER1_STRENGTH.get();
        swayTier2Strength = SWAY_TIER2_STRENGTH.get();
        swayTier3Strength = SWAY_TIER3_STRENGTH.get();

        hudWaveAmplitudePixels = HUD_WAVE_AMPLITUDE_PIXELS.get();
        hudWaveLengthPixels = HUD_WAVE_LENGTH_PIXELS.get();
        hudWaveSpeed = HUD_WAVE_SPEED.get();

        ciderDrunkenness = CIDER_DRUNKENNESS.get();
        ciderUses = CIDER_USES.get();
        ciderCooldownSeconds = CIDER_COOLDOWN_SECONDS.get();
        ciderEffectDurationSeconds = CIDER_EFFECT_DURATION_SECONDS.get();
        ciderSpeedAmplifier = CIDER_SPEED_AMPLIFIER.get();

        beerDrunkenness = BEER_DRUNKENNESS.get();
        beerUses = BEER_USES.get();
        beerCooldownSeconds = BEER_COOLDOWN_SECONDS.get();
        beerHealAmount = BEER_HEAL_AMOUNT.get();

        vodkaDrunkenness = VODKA_DRUNKENNESS.get();
        vodkaUses = VODKA_USES.get();
        vodkaCooldownSeconds = VODKA_COOLDOWN_SECONDS.get();
        vodkaEffectDurationSeconds = VODKA_EFFECT_DURATION_SECONDS.get();
        vodkaGlowRadius = VODKA_GLOW_RADIUS.get();

        wineDrunkenness = WINE_DRUNKENNESS.get();
        wineUses = WINE_USES.get();
        wineCooldownSeconds = WINE_COOLDOWN_SECONDS.get();
        wineEffectDurationSeconds = WINE_EFFECT_DURATION_SECONDS.get();
        wineDamageReduction = WINE_DAMAGE_REDUCTION.get();
        wineSpeedPenaltyPercent = WINE_SPEED_PENALTY_PERCENT.get();

        rassolUses = RASSOL_USES.get();
        rassolCooldownSeconds = RASSOL_COOLDOWN_SECONDS.get();

        whiskyDrunkenness = WHISKY_DRUNKENNESS.get();
        whiskyUses = WHISKY_USES.get();
        whiskyCooldownSeconds = WHISKY_COOLDOWN_SECONDS.get();
        whiskyEffectDurationSeconds = WHISKY_EFFECT_DURATION_SECONDS.get();

        honeyDrunkenness = HONEY_DRUNKENNESS.get();
        honeyUses = HONEY_USES.get();
        honeyCooldownSeconds = HONEY_COOLDOWN_SECONDS.get();
        honeyEffectDurationSeconds = HONEY_EFFECT_DURATION_SECONDS.get();

        tequilaDrunkenness = TEQUILA_DRUNKENNESS.get();
        tequilaUses = TEQUILA_USES.get();
        tequilaCooldownSeconds = TEQUILA_COOLDOWN_SECONDS.get();
        tequilaEffectDurationSeconds = TEQUILA_EFFECT_DURATION_SECONDS.get();
        tequilaBurnDurationSeconds = TEQUILA_BURN_DURATION_SECONDS.get();
        tequilaBurnIntervalSeconds = TEQUILA_BURN_INTERVAL_SECONDS.get();
        tequilaBurnDamage = TEQUILA_BURN_DAMAGE.get();
        tequilaHealingReductionPercent = TEQUILA_HEALING_REDUCTION_PERCENT.get();

        liqueurDrunkenness = LIQUEUR_DRUNKENNESS.get();
        liqueurUses = LIQUEUR_USES.get();
        liqueurCooldownSeconds = LIQUEUR_COOLDOWN_SECONDS.get();
        liqueurEffectDurationSeconds = LIQUEUR_EFFECT_DURATION_SECONDS.get();
        liqueurMiningSpeedBonusPercent = LIQUEUR_MINING_SPEED_BONUS_PERCENT.get();
        liqueurDoubleLootChancePercent = LIQUEUR_DOUBLE_LOOT_CHANCE_PERCENT.get();
    }
}
