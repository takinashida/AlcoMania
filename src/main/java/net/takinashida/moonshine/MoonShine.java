package net.takinashida.moonshine;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.takinashida.moonshine.client.ClientConfig;
import net.takinashida.moonshine.network.Networking;
import org.slf4j.Logger;

/**
 * Главный класс мода MoonShine.
 *
 * Именно этот класс Forge находит по аннотации {@link Mod} и запускает первым.
 * Здесь мы:
 *  1. Регистрируем предметы (ModItems).
 *  2. Регистрируем эффекты (ModEffects).
 *  3. Регистрируем сетевой канал (Networking).
 *  4. Подключаем конфиг.
 */
@Mod(MoonShine.MOD_ID)
public class MoonShine {

    /** Уникальный id мода. Должен совпадать с mod_id в gradle.properties и mods.toml. */
    public static final String MOD_ID = "alcomania";

    /** Логгер мода — удобно писать в консоль через MoonShine.LOGGER.info(...). */
    public static final Logger LOGGER = LogUtils.getLogger();

    public MoonShine() {
        // Шина событий этапа загрузки мода (registry, setup и т.п.).
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрируем предметы, эффекты и звуки. Внутри DeferredRegister сам подпишется на нужные события.
        ModItems.register(modBus);
        ModEffects.register(modBus);
        ModSounds.register(modBus);

        // FMLCommonSetupEvent — безопасное место для регистрации сетевых пакетов.
        modBus.addListener(this::commonSetup);

        // Подключаем общий конфиг (config/alcomania-common.toml) и клиентский
        // (config/alcomania-client.toml — видимость шкал HUD).
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // Шина игровых событий (тики, способности, атака и т.п.).
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Вызывается один раз после загрузки всех модов.
     * enqueueWork нужен, чтобы регистрация пакетов произошла в главном потоке.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Networking::register);
    }
}
