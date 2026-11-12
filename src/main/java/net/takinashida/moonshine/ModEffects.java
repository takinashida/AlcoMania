package net.takinashida.moonshine;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.takinashida.moonshine.effect.*;

/**
 * Регистрация кастомных эффектов (MobEffect).
 *
 * Здесь одиннадцать эффектов (включая кастомное ночное зрение):
 *  - alcomania:sway  — «Шатание»: маркер опьянения, на клиенте шатает камеру;
 *  - alcomania:cider — «Сидр»: положительный эффект от яблочного сидра (Скорость II);
 *  - alcomania:vodka — «Водка»: подсветка сущностей в радиусе только для выпившего;
 *  - alcomania:wine  — «Вино»: -1 к входящему урону и -10% к скорости;
 *  - alcomania:whisky — «Виски»: полная неуязвимость, после снятия — смерть;
 *  - alcomania:honey — «Медовый щит»: одноразовая блокировка всего урона;
 *  - alcomania:spectral — «Спектральный»: подсветка игрока без «рентгена»;
 *  - alcomania:tequila — «Текила»: бафф, включающий горение от выстрелов;
 *  - alcomania:tequila_burn — «Горение»: урон во времени, пламя и -50% исцеления;
 *  - alcomania:night_vision — «Ночное зрение», которое не блокируется Hardcore True Darkness;
 *  - alcomania:liqueur — «Ликёр»: скорость добычи и шанс двойного лута.
 *
 * Боевые эффекты вина и медовухи считаются в CombatEvents, горение
 * текилы — в TequilaEvents, а подсветка водки — на клиенте. Сами эффекты
 * служат маркерами с иконкой и таймером.
 */
public final class ModEffects {

    /** Реестр эффектов. */
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MoonShine.MOD_ID);

    /**
     * Эффект «Шатание».
     * Логика отрисовки (камера) находится на клиенте в SwayClientHandler.
     */
    public static final RegistryObject<MobEffect> SWAY =
            EFFECTS.register("sway", SwayEffect::new);

    /**
     * Эффект «Сидр».
     * Выдаётся предметом AppleCider и даёт Скорость II на 30 секунд.
     */
    public static final RegistryObject<MobEffect> CIDER =
            EFFECTS.register("cider", CiderEffect::new);

    /**
     * Эффект «Водка».
     * Выдаётся предметом Vodka: подсвечивает сущности в радиусе и позволяет
     * выпившему видеть их сквозь стены. Другие игроки подсветку не видят.
     */
    public static final RegistryObject<MobEffect> VODKA =
            EFFECTS.register("vodka", VodkaEffect::new);

    /**
     * Эффект «Вино».
     * Выдаётся предметом Wine: снижает входящий урон и скорость передвижения.
     */
    public static final RegistryObject<MobEffect> WINE =
            EFFECTS.register("wine", WineEffect::new);

    /**
     * Эффект «Виски».
     * Выдаётся предметом Whisky: полная неуязвимость на время действия.
     * После окончания или снятия эффекта игрок умирает (см. ModEvents).
     */
    public static final RegistryObject<MobEffect> WHISKY =
            EFFECTS.register("whisky", WhiskyEffect::new);

    /**
     * Эффект «Медовый щит».
     * Выдаётся предметом Honey: блокирует весь входящий урон и снимается
     * после первого поглощённого удара (см. CombatEvents).
     */
    public static final RegistryObject<MobEffect> HONEY =
            EFFECTS.register("honey", HoneyEffect::new);

    /**
     * Эффект «Спектральный».
     * Новый эффект на основе Glowing: игрок подсвечивается, но не виден
     * сквозь стены (см. SpectralEffectRenderHandler на клиенте).
     */
    public static final RegistryObject<MobEffect> SPECTRAL =
            EFFECTS.register("spectral", SpectralEffect::new);

    /** Бафф текилы: попадания снарядов накладывают горение. */
    public static final RegistryObject<MobEffect> TEQUILA =
            EFFECTS.register("tequila", TequilaEffect::new);

    /** Горение от текилы: 1 урон раз в 2 секунды, пламя и -50% исцеления. */
    public static final RegistryObject<MobEffect> TEQUILA_BURN =
            EFFECTS.register("tequila_burn", TequilaBurnEffect::new);

    /** «Ночное зрение»: локально добавляет ванильный Night Vision, обходя блокировки. */
    public static final RegistryObject<MobEffect> NIGHT_VISION =
            EFFECTS.register("night_vision", NightVisionEffect::new);

    /** «Ликёр»: +50% скорости добычи и 20% шанс двойного лута. */
    public static final RegistryObject<MobEffect> LIQUEUR =
            EFFECTS.register("liqueur", LiqueurEffect::new);

    private ModEffects() {
        // Утилитарный класс — создавать экземпляр не нужно.
    }

    /** Подписывает реестр на шину событий мода. Вызывается из MoonShine. */
    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
