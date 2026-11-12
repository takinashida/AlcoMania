package net.takinashida.moonshine.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.takinashida.moonshine.MoonShine;

/**
 * Регистрация клиентских элементов интерфейса.
 * Работает только на клиенте (value = Dist.CLIENT).
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    /**
     * Регистрируем фабрику экрана конфига.
     *
     * После этого у MoonShine в списке модов появляется кнопка «Настройка»,
     * которая открывает {@link MyConfigScreen}.
     */
    @SubscribeEvent
    public static void registerConfigScreen(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new MyConfigScreen(parent))));
    }

    /**
     * registerAboveAll — рисуем индикатор поверх всех ванильных HUD-элементов.
     * partialTick передаём в HUD: благодаря нему волновая анимация жидкости
     * плавная и не зависит от частоты тиков.
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("drunkenness", (gui, graphics, partialTick, width, height) ->
                DrunkennessHUD.render(graphics, partialTick));

        event.registerAboveAll("drunkenness_bar", (gui, graphics, partialTick, width, height) ->
                DrunkennessBarHUD.render(graphics));
    }
}
