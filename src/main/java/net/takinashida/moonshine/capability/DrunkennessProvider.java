package net.takinashida.moonshine.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.Capability;
import net.minecraftforge.event.entity.EntityJoinEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.AddMavenMod;
import net.minecraftforge.fml.javafmlmod.ModInitializer;
import net.minecraftforge.providers.CapabilityProvider;

public class DrunkennessProvider implements CapabilityProvider<Entity, DrunkennessInstance> {
    
    @Override
    public CapabilityProvider.Provider<Entity, DrunkennessInstance> getProvider() {
        return new CapabilityProvider<Entity, DrunkennessInstance>() {
            @Override
            public Capability<DrunkennessInstance> getCapability(Entity entity) {
                return DrunkennessCapability.DRUNKENNESS_CAPABILITY;
            }

            @Override
            public CapabilityProvider.Provider<Entity, DrunkennessInstance> getProvider() {
                return this;
            }
        };
    }

    @Mod.EventBusSubscriber
    public static class CapabilityRegistration {
        @SubscribeEvent
        public static void onEntityJoin(EntityJoinEvent event) {
            if (event.getEntity() instanceof ServerPlayer) {
                // Logic to attach would go here via CapabilityProvider helper
            }
        }
    }
}
