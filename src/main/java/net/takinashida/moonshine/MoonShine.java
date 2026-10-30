package net.takinashida.moonshine;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.AddMavenMod;
import net.minecraftforge.fml.javafmlmod.ModInitializer;

@Mod(MoonShine.MOD_ID)
public class MoonShine implements ModInitializer {
    public static final String MOD_ID = "moonshine";

    public MoonShine() {
        // The IEventBus is passed during mod initialization.
        // But we can register the DeferredRegister here or through a method.
    }

    @Override
    public void init() {
        // Register items
        ModItems.ITEMS.register(IEventBus.INSTANCE);
    }
}
