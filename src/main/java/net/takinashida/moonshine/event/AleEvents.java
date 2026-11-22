package net.takinashida.moonshine.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.MoonShine;

/**
 * Механика эля.
 *
 * Эффект «Эль» сам по себе ничего не делает — он работает как таймер.
 * Когда эффект заканчивается естественным образом, игрок исцеляется на
 * items.ale.healAmount HP (по умолчанию 12 HP).
 *
 * Снятие эффекта молоком или командой /effect не лечит: это принудительное
 * удаление, а не окончание эффекта.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID)
public final class AleEvents {

    private AleEvents() {
    }

    @SubscribeEvent
    public static void onAleExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (event.getEffectInstance().getEffect() == ModEffects.ALE.get() && player.isAlive()) {
            player.heal(Config.aleHealAmount);
        }
    }
}
