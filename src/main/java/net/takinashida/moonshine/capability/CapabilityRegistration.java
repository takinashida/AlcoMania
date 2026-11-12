package net.takinashida.moonshine.capability;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.MoonShine;

/**
 * Сообщает Forge, какой класс реализует capability "Опьянение".
 *
 * Без этой регистрации CapabilityManager не будет знать, как создавать
 * DrunkennessInstance, и игра упадёт при первом обращении к capability.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CapabilityRegistration {

    private CapabilityRegistration() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(DrunkennessInstance.class);
    }
}
