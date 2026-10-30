package net.takinashida.moonshine.event;

import net.takinashida.moonshine.capability.DrunkennessCapability;
import net.takinashida.moonshine.capability.DrunkennessInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingTickEvent.EntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player && !player.level().isClientSide) {
            DrunkennessInstance instance = DrunkennessCapability.DRUNKENNESS_CAPABILITY.get(player);
            if (instance != null) {
                // Reduce drunkenness by 1 every 20 ticks (1 second)
                if (player.tickCount % 20 == 0) {
                    int currentValue = instance.getValue();
                    if (currentValue > 0) {
                        instance.setValue(currentValue - 1);
                    }
                }

                // Handle effects based on drunkenness value
                int val = instance.getValue();
                if (val >= 100) {
                    player.die();
                    return;
                }

                // Effect Logic (Placeholder)
                // 25 units: Effect 1
                if (val >= 25 && val < 50) {
                    player.addEffect(new MobEffectInstance(net.minecraft.MobEffects.CONFUSION, 20, 0));
                }
                // 50 units: Effect 1 + Effect 2
                if (val >= 50 && val < 75) {
                    player.addEffect(new MobEffectInstance(net.minecraft.MobEffects.SLOWNESS, 20, 0));
                }
                // 75 units: Effect 1 + Effect 2 + Effect 3
                if (val >= 75 && val < 100) {
                    player.addEffect(new MobEffectInstance(net.minecraft.MobEffects.WEAKNESS, 20, 0));
                }
                // 100 units: Effect 1 + Effect 2 + Effect 3 + Effect 4
                if (val >= 100) {
                    player.addEffect(new MobEffectInstance(net.minecraft.MobEffects.BLINDNESS, 20, 0));
                }
            }
        }
    }
}
