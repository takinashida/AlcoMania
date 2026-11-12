package net.takinashida.moonshine;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Конфиг мода (файл config/alcomania-common.toml в папке игры).
 *
 * Здесь вынесены ВСЕ игровые числа:
 *  - скорость трезвения;
 *  - пороги эффектов, иссушения и смерти;
 *  - характеристики напитков (опьянение, применения, перезарядка, бонусы);
 *  - сила покачивания камеры.
 *
 * Значения читаются в {@link #onLoad(ModConfigEvent)} и складываются в обычные
 * static-поля, чтобы игровой код не дёргал конфиг каждый тик/кадр.
 *
 * ВАЖНО: предметы создаются ДО загрузки конфига, поэтому количество применений
 * (прочность) читается не в конструкторе, а динамически — в
 * DrinkableItem#getMaxDamage. Благодаря этому правки конфига подхватываются
 * без перезапуска игры.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {

    /** Построитель описания конфига. */
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // =========================================================================
    //  ОПЬЯНЕНИЕ
    // =========================================================================
    private static final ForgeConfigSpec.IntValue DECAY_INTERVAL_TICKS = BUILDER
            .comment("Как часто (в тиках) уменьшается опьянение. 20 тиков = 1 секунда.")
            .defineInRange("drunkenness.decayIntervalTicks", 20, 1, 200);

    private static final ForgeConfigSpec.IntValue DECAY_AMOUNT = BUILDER
            .comment("Сколько единиц опьянения снимается за один шаг трезвения.")
            .defineInRange("drunkenness.decayAmount", 1, 1, 100);

    private static final ForgeConfigSpec.IntValue SWAY_THRESHOLD = BUILDER
            .comment("Опьянение, с которого начинает шатать камеру и вешается эффект Sway.",
                    "Это же значение — начало первого (слабого) сегмента шатания.")
            .defineInRange("drunkenness.swayThreshold", 25, 1, 10000);

    private static final ForgeConfigSpec.IntValue SWAY_TIER2_THRESHOLD = BUILDER
            .comment("Опьянение, с которого шатание переходит на средний сегмент.",
                    "Сила внутри сегмента постоянна и не зависит от каждой единицы.",
                    "Переход сглаживается, поэтому камера не дёргается.")
            .defineInRange("drunkenness.swayTier2Threshold", 50, 1, 10000);

    private static final ForgeConfigSpec.IntValue SWAY_TIER3_THRESHOLD = BUILDER
            .comment("Опьянение, с которого шатание становится максимальным.",
                    "Дальше сила не растёт — вплоть до deathThreshold.")
            .defineInRange("drunkenness.swayTier3Threshold", 75, 1, 10000);

    private static final ForgeConfigSpec.IntValue SLOWNESS_THRESHOLD = BUILDER
            .comment("Опьянение, с которого накладывается замедление.")
            .defineInRange("drunkenness.slownessThreshold", 50, 1, 10000);

    private static final ForgeConfigSpec.IntValue WEAKNESS_THRESHOLD = BUILDER
            .comment("Опьянение, с которого накладывается слабость.")
            .defineInRange("drunkenness.weaknessThreshold", 75, 1, 10000);

    private static final ForgeConfigSpec.IntValue BLINDNESS_THRESHOLD = BUILDER
            .comment("Опьянение, с которого накладывается слепота.")
            .defineInRange("drunkenness.blindnessThreshold", 100, 1, 10000);

    private static final ForgeConfigSpec.IntValue DEATH_THRESHOLD = BUILDER
            .comment("Опьянение, при котором игрок мгновенно умирает (значение >= этого числа).",
                    "Это же значение используется как максимум шкалы HUD и силы шатания камеры.")
            .defineInRange("drunkenness.deathThreshold", 100, 1, 10000);

    private static final ForgeConfigSpec.IntValue WITHER_THRESHOLD = BUILDER
            .comment("Если опьянение СТРОГО БОЛЬШЕ этого значения, накладывается иссушение.",
                    "По умолчанию равно deathThreshold (100), поэтому иссушение вешается",
                    "в тот же тик, когда игрок умирает. Поднимите deathThreshold выше этого",
                    "значения (например, до 120), если хотите, чтобы игрок выживал и реально",
                    "страдал от иссушения.")
            .defineInRange("drunkenness.witherThreshold", 100, 0, 10000);

    private static final ForgeConfigSpec.IntValue WITHER_AMPLIFIER = BUILDER
            .comment("Сила иссушения: 0 = Иссушение I, 1 = II, 2 = III, 3 = IV (по умолчанию).")
            .defineInRange("drunkenness.witherAmplifier", 3, 0, 9);

    private static final ForgeConfigSpec.IntValue EFFECT_DURATION_TICKS = BUILDER
            .comment("Длительность (в тиках) пороговых эффектов. 200 тиков = 10 секунд.",
                    "Эффект продлевается только когда от него осталось меньше половины,",
                    "поэтому пакеты в сеть каждый тик не летят.")
            .defineInRange("drunkenness.effectDurationTicks", 200, 20, 12000);

    // =========================================================================
    //  ВИЗУАЛ: ПОКАЧИВАНИЕ КАМЕРЫ
    // =========================================================================
    private static final ForgeConfigSpec.DoubleValue SWAY_YAW_DEGREES = BUILDER
            .comment("Максимальная амплитуда покачивания камеры влево-вправо (в градусах).")
            .defineInRange("visual.swayYawDegrees", 14.0D, 0.0D, 45.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_PITCH_DEGREES = BUILDER
            .comment("Максимальная амплитуда покачивания камеры вверх-вниз (в градусах).")
            .defineInRange("visual.swayPitchDegrees", 8.0D, 0.0D, 45.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_ROLL_DEGREES = BUILDER
            .comment("Максимальный наклон горизонта (крен) в градусах.")
            .defineInRange("visual.swayRollDegrees", 6.0D, 0.0D, 45.0D);

    // ---- Сегменты силы шатания ----
    private static final ForgeConfigSpec.DoubleValue SWAY_TIER1_STRENGTH = BUILDER
            .comment("Сила шатания на первом сегменте (swayThreshold..swayTier2Threshold).",
                    "Доля от максимальной амплитуды: 0.35 = 35% от swayYawDegrees/swayPitchDegrees/swayRollDegrees.")
            .defineInRange("visual.swayTier1Strength", 0.35D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_TIER2_STRENGTH = BUILDER
            .comment("Сила шатания на втором сегменте (swayTier2Threshold..swayTier3Threshold).")
            .defineInRange("visual.swayTier2Strength", 0.7D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.DoubleValue SWAY_TIER3_STRENGTH = BUILDER
            .comment("Сила шатания на третьем сегменте (swayTier3Threshold и выше).",
                    "1.0 = полная амплитуда из swayYawDegrees/swayPitchDegrees/swayRollDegrees.")
            .defineInRange("visual.swayTier3Strength", 1.0D, 0.0D, 1.0D);

    // =========================================================================
    //  ВИЗУАЛ: ВОЛНОВАЯ АНИМАЦИЯ HUD
    // =========================================================================
    private static final ForgeConfigSpec.DoubleValue HUD_WAVE_AMPLITUDE_PIXELS = BUILDER
            .comment("Амплитуда волны жидкости в HUD-бутылке, в пикселях.",
                    "0 = поверхность ровная, 2 = заметное колыхание.")
            .defineInRange("visual.hudWaveAmplitudePixels", 2.0D, 0.0D, 8.0D);

    private static final ForgeConfigSpec.DoubleValue HUD_WAVE_LENGTH_PIXELS = BUILDER
            .comment("Длина одной волны по ширине бутылки, в пикселях.",
                    "Меньше значение = больше волн на поверхности.")
            .defineInRange("visual.hudWaveLengthPixels", 18.0D, 4.0D, 128.0D);

    private static final ForgeConfigSpec.DoubleValue HUD_WAVE_SPEED = BUILDER
            .comment("Скорость бега волны в радианах за тик.",
                    "0.1 — примерно один полный период за 3 секунды.")
            .defineInRange("visual.hudWaveSpeed", 0.1D, 0.0D, 2.0D);

    // Видимость шкал вынесена в клиентский конфиг: см. ClientConfig.

    // =========================================================================
    //  НАПИТКИ
    // =========================================================================

    // ---- Яблочный сидр ----
    private static final ForgeConfigSpec.IntValue CIDER_DRUNKENNESS = BUILDER
            .comment("Яблочный сидр: сколько опьянения даёт одно питьё.")
            .defineInRange("items.cider.drunkenness", 30, 0, 1000);

    private static final ForgeConfigSpec.IntValue CIDER_USES = BUILDER
            .comment("Яблочный сидр: количество применений (прочность предмета).")
            .defineInRange("items.cider.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue CIDER_COOLDOWN_SECONDS = BUILDER
            .comment("Яблочный сидр: перезарядка между глотками в секундах.")
            .defineInRange("items.cider.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue CIDER_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Яблочный сидр: длительность эффекта «Сидр» в секундах.")
            .defineInRange("items.cider.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue CIDER_SPEED_AMPLIFIER = BUILDER
            .comment("Яблочный сидр: сила эффекта. 0 = Скорость I, 1 = Скорость II (по умолчанию).")
            .defineInRange("items.cider.speedAmplifier", 0, 0, 9);

    // ---- Пиво ----
    private static final ForgeConfigSpec.IntValue BEER_DRUNKENNESS = BUILDER
            .comment("Пиво: сколько опьянения даёт одно питьё.")
            .defineInRange("items.beer.drunkenness", 40, 0, 1000);

    private static final ForgeConfigSpec.IntValue BEER_USES = BUILDER
            .comment("Пиво: количество применений (прочность предмета).")
            .defineInRange("items.beer.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue BEER_COOLDOWN_SECONDS = BUILDER
            .comment("Пиво: перезарядка между глотками в секундах. 0 = без перезарядки (по умолчанию).")
            .defineInRange("items.beer.cooldownSeconds", 0, 0, 3600);

    private static final ForgeConfigSpec.IntValue BEER_HEAL_AMOUNT = BUILDER
            .comment("Пиво: сколько HP восстанавливает одно питьё (2 HP = 1 сердце).")
            .defineInRange("items.beer.healAmount", 16, 0, 100);

    // ---- Водка ----
    private static final ForgeConfigSpec.IntValue VODKA_DRUNKENNESS = BUILDER
            .comment("Водка: сколько опьянения даёт одно питьё.")
            .defineInRange("items.vodka.drunkenness", 100, 0, 1000);

    private static final ForgeConfigSpec.IntValue VODKA_USES = BUILDER
            .comment("Водка: количество применений (прочность предмета).")
            .defineInRange("items.vodka.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue VODKA_COOLDOWN_SECONDS = BUILDER
            .comment("Водка: перезарядка между глотками в секундах.")
            .defineInRange("items.vodka.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue VODKA_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Водка: длительность эффекта «Водка» в секундах.")
            .defineInRange("items.vodka.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue VODKA_GLOW_RADIUS = BUILDER
            .comment("Водка: радиус (в блоках), в котором сущности подсвечиваются только для выпившего.")
            .defineInRange("items.vodka.glowRadius", 16, 1, 64);

    // ---- Вино ----
    private static final ForgeConfigSpec.IntValue WINE_DRUNKENNESS = BUILDER
            .comment("Вино: сколько опьянения даёт одно питьё.")
            .defineInRange("items.wine.drunkenness", 75, 0, 1000);

    private static final ForgeConfigSpec.IntValue WINE_USES = BUILDER
            .comment("Вино: количество применений (прочность предмета).")
            .defineInRange("items.wine.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue WINE_COOLDOWN_SECONDS = BUILDER
            .comment("Вино: перезарядка между глотками в секундах.")
            .defineInRange("items.wine.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue WINE_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Вино: длительность эффекта «Вино» в секундах.")
            .defineInRange("items.wine.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue WINE_DAMAGE_REDUCTION = BUILDER
            .comment("Вино: сколько единиц урона вычитается из каждого входящего удара.",
                    "Не влияет на урон от голода и утопления.")
            .defineInRange("items.wine.damageReduction", 1, 0, 100);

    private static final ForgeConfigSpec.IntValue WINE_SPEED_PENALTY_PERCENT = BUILDER
            .comment("Вино: штраф к скорости передвижения в процентах (10 = -10%).")
            .defineInRange("items.wine.speedPenaltyPercent", 10, 0, 100);

    // ---- Рассол ----
    private static final ForgeConfigSpec.IntValue RASSOL_USES = BUILDER
            .comment("Рассол: количество применений (прочность предмета).")
            .defineInRange("items.rassol.uses", 1, 1, 64);

    private static final ForgeConfigSpec.IntValue RASSOL_COOLDOWN_SECONDS = BUILDER
            .comment("Рассол: перезарядка между глотками в секундах.")
            .defineInRange("items.rassol.cooldownSeconds", 30, 0, 3600);

    // ---- Виски ----
    private static final ForgeConfigSpec.IntValue WHISKY_DRUNKENNESS = BUILDER
            .comment("Виски: сколько опьянения даёт одно питьё.")
            .defineInRange("items.whisky.drunkenness", 100, 0, 1000);

    private static final ForgeConfigSpec.IntValue WHISKY_USES = BUILDER
            .comment("Виски: количество применений (прочность предмета).")
            .defineInRange("items.whisky.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue WHISKY_COOLDOWN_SECONDS = BUILDER
            .comment("Виски: перезарядка между глотками в секундах.")
            .defineInRange("items.whisky.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue WHISKY_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Виски: длительность эффекта «Виски» в секундах.")
            .defineInRange("items.whisky.effectDurationSeconds", 10, 1, 3600);

    // ---- Медовуха ----
    private static final ForgeConfigSpec.IntValue HONEY_DRUNKENNESS = BUILDER
            .comment("Медовуха: сколько опьянения даёт одно питьё.")
            .defineInRange("items.honey.drunkenness", 30, 0, 1000);

    private static final ForgeConfigSpec.IntValue HONEY_USES = BUILDER
            .comment("Медовуха: количество применений (прочность предмета).")
            .defineInRange("items.honey.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue HONEY_COOLDOWN_SECONDS = BUILDER
            .comment("Медовуха: перезарядка между глотками в секундах.")
            .defineInRange("items.honey.cooldownSeconds", 10, 0, 3600);

    private static final ForgeConfigSpec.IntValue HONEY_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Медовуха: длительность эффекта «Медовуха» в секундах..")
            .defineInRange("items.honey.effectDurationSeconds", 20, 1, 64);

    // ---- Текила ----
    private static final ForgeConfigSpec.IntValue TEQUILA_DRUNKENNESS = BUILDER
            .comment("Текила: сколько опьянения даёт одно питьё.")
            .defineInRange("items.tequila.drunkenness", 100, 0, 1000);

    private static final ForgeConfigSpec.IntValue TEQUILA_USES = BUILDER
            .comment("Текила: количество применений (прочность предмета).")
            .defineInRange("items.tequila.uses", 4, 1, 64);

    private static final ForgeConfigSpec.IntValue TEQUILA_COOLDOWN_SECONDS = BUILDER
            .comment("Текила: перезарядка между глотками в секундах. 0 = без перезарядки.")
            .defineInRange("items.tequila.cooldownSeconds", 30, 0, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Текила: длительность эффекта «Текила» в секундах.")
            .defineInRange("items.tequila.effectDurationSeconds", 30, 1, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_BURN_DURATION_SECONDS = BUILDER
            .comment("Текила: длительность горения от выстрелов в секундах.")
            .defineInRange("items.tequila.burnDurationSeconds", 8, 1, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_BURN_INTERVAL_SECONDS = BUILDER
            .comment("Текила: интервал между ударами горения в секундах.")
            .defineInRange("items.tequila.burnIntervalSeconds", 2, 1, 3600);

    private static final ForgeConfigSpec.IntValue TEQUILA_BURN_DAMAGE = BUILDER
            .comment("Текила: сколько урона наносит один удар горения.")
            .defineInRange("items.tequila.burnDamage", 1, 0, 1000);

    private static final ForgeConfigSpec.IntValue TEQUILA_HEALING_REDUCTION_PERCENT = BUILDER
            .comment("Текила: снижение входящего исцеления у цели с горением в процентах (50 = -50%).")
            .defineInRange("items.tequila.healingReductionPercent", 50, 0, 100);

    // ---- Ликёр ----
    private static final ForgeConfigSpec.IntValue LIQUEUR_DRUNKENNESS = BUILDER
            .comment("Ликёр: сколько опьянения даёт одно питьё.")
            .defineInRange("items.liqueur.drunkenness", 50, 0, 1000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_USES = BUILDER
            .comment("Ликёр: количество применений (прочность предмета).")
            .defineInRange("items.liqueur.uses", 2, 1, 64);

    private static final ForgeConfigSpec.IntValue LIQUEUR_COOLDOWN_SECONDS = BUILDER
            .comment("Ликёр: перезарядка между глотками в секундах (10 минут = 600).")
            .defineInRange("items.liqueur.cooldownSeconds", 600, 0, 36000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_EFFECT_DURATION_SECONDS = BUILDER
            .comment("Ликёр: длительность эффекта «Ликёр» в секундах (6 минут = 360).")
            .defineInRange("items.liqueur.effectDurationSeconds", 360, 1, 36000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_MINING_SPEED_BONUS_PERCENT = BUILDER
            .comment("Ликёр: бонус к скорости добычи киркой/топором в процентах (50 = +50%).")
            .defineInRange("items.liqueur.miningSpeedBonusPercent", 50, 0, 1000);

    private static final ForgeConfigSpec.IntValue LIQUEUR_DOUBLE_LOOT_CHANCE_PERCENT = BUILDER
            .comment("Ликёр: шанс двойного лута при добыче железной/угольной руды и jeg:brimstone_ore.")
            .defineInRange("items.liqueur.doubleLootChancePercent", 20, 0, 100);

    /** Итоговое описание конфига, которое регистрирует MoonShine. */
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // =========================================================================
    //  КЭШ ЗНАЧЕНИЙ (значения по умолчанию на случай, если конфиг ещё не загружен)
    // =========================================================================
    public static int decayIntervalTicks = 20;
    public static int decayAmount = 1;
    public static int swayThreshold = 25;
    public static int swayTier2Threshold = 50;
    public static int swayTier3Threshold = 75;
    public static int slownessThreshold = 50;
    public static int weaknessThreshold = 75;
    public static int blindnessThreshold = 100;
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
    public static int beerHealAmount = 16;

    public static int vodkaDrunkenness = 100;
    public static int vodkaUses = 4;
    public static int vodkaCooldownSeconds = 30;
    public static int vodkaEffectDurationSeconds = 30;
    public static int vodkaGlowRadius = 16;
    public static int wineSpeedPenaltyPercent = 10;
    public static int rassolUses = 1;
    public static int wineDrunkenness = 75;
    public static int wineUses = 4;
    public static int wineCooldownSeconds = 30;
    public static int wineEffectDurationSeconds = 30;
    public static int wineDamageReduction = 1;
    public static int whiskyDrunkenness = 100;
    public static int whiskyUses = 2;
    public static int whiskyCooldownSeconds = 30;
    public static int whiskyEffectDurationSeconds = 10;

    public static int honeyDrunkenness = 30;
    public static int honeyUses = 4;
    public static int honeyCooldownSeconds = 10;
    public static int honeyEffectDurationSeconds = 20;

    public static int tequilaDrunkenness = 100;
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

    public static int rassolCooldownSeconds = 30;

    private Config() {
    }

    /** Вызывается Forge при загрузке/перезагрузке конфига. */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // На MOD-шине приходят события и от клиентского конфига — реагируем только на свой.
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        decayIntervalTicks = DECAY_INTERVAL_TICKS.get();
        decayAmount = DECAY_AMOUNT.get();
        swayThreshold = SWAY_THRESHOLD.get();
        swayTier2Threshold = SWAY_TIER2_THRESHOLD.get();
        swayTier3Threshold = SWAY_TIER3_THRESHOLD.get();
        slownessThreshold = SLOWNESS_THRESHOLD.get();
        weaknessThreshold = WEAKNESS_THRESHOLD.get();
        blindnessThreshold = BLINDNESS_THRESHOLD.get();
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
