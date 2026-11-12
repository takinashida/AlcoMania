package net.takinashida.moonshine;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.takinashida.moonshine.item.AppleCider;
import net.takinashida.moonshine.item.BeerBottle;
import net.takinashida.moonshine.item.Honey;
import net.takinashida.moonshine.item.Liqueur;
import net.takinashida.moonshine.item.Rassol;
import net.takinashida.moonshine.item.Tequila;
import net.takinashida.moonshine.item.Vodka;
import net.takinashida.moonshine.item.Wine;
import net.takinashida.moonshine.item.Whisky;

/**
 * Регистрация всех предметов мода.
 *
 * DeferredRegister — это "отложенная" регистрация: мы создаём объекты здесь,
 * а Forge реально добавляет их в игру в нужный момент (иначе игра упадёт).
 */
public final class ModItems {

    /** Реестр предметов, привязанный к namespace "alcomania". */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MoonShine.MOD_ID);

    /** Яблочный сидр: +30 опьянения, Скорость II, 2 применения, кулдаун 30 сек. */
    public static final RegistryObject<Item> APPLE_CIDER =
            ITEMS.register("apple_cider", AppleCider::new);

    /** Пиво: +40 опьянения, лечение, 2 применения, без кулдауна. */
    public static final RegistryObject<Item> BEER =
            ITEMS.register("beer", BeerBottle::new);

    /** Водка: +100 опьянения, подсветка сущностей в радиусе 16 блоков, 4 применения. */
    public static final RegistryObject<Item> VODKA =
            ITEMS.register("vodka", Vodka::new);

    /** Вино: +75 опьянения, -1 к входящему урону, -10% скорости, 4 применения. */
    public static final RegistryObject<Item> WINE =
            ITEMS.register("wine", Wine::new);

    /** Виски: полная неуязвимость на 10 секунд, затем гарантированная смерть. */
    public static final RegistryObject<Item> WHISKY =
            ITEMS.register("whisky", Whisky::new);

    /** Медовуха: +30 опьянения, одноразовый щит от всего урона, 4 применения, кулдаун 10 сек. */
    public static final RegistryObject<Item> HONEY =
            ITEMS.register("honey", Honey::new);

    /** Текила: +100 опьянения, горение от выстрелов, 4 применения, без кулдауна. */
    public static final RegistryObject<Item> TEQUILA =
            ITEMS.register("tequila", Tequila::new);

    /** Ликёр: +50 опьянения, скорость добычи и двойной лут, 2 применения, кулдаун 10 мин. */
    public static final RegistryObject<Item> LIQUEUR =
            ITEMS.register("liqueur", Liqueur::new);

    /** Рассол: снимает все эффекты и обнуляет опьянение, 1 применение. */
    public static final RegistryObject<Item> RASSOL =
            ITEMS.register("rassol", Rassol::new);

    private ModItems() {
        // Утилитарный класс — создавать экземпляр не нужно.
    }

    /** Подписывает реестр на шину событий мода. Вызывается из MoonShine. */
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
