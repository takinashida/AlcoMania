package net.takinashida.moonshine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.ModSounds;
import net.takinashida.moonshine.MoonShine;

/**
 * Звуковое сопровождение эффекта «Виски».
 *
 * Пока эффект активен, у игрока циклично играет heart_beat.ogg.
 * Звук создаётся как локальный (relative, без позиционного затухания)
 * и проигрывается через клиентский SoundManager — его слышит только
 * сам игрок, другие игроки ничего не слышат.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, value = Dist.CLIENT)
public final class WhiskySoundClientHandler {

    private static final float VOLUME = 0.6F;
    private static final float PITCH = 1.0F;

    /** Текущий зацикленный звук сердцебиения, если он играет. */
    private static SoundInstance heartbeat;

    private WhiskySoundClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        boolean shouldPlay = player != null
                && minecraft.level != null
                && minecraft.screen == null
                && !minecraft.isPaused()
                && !player.isSpectator()
                && player.isAlive()
                && player.hasEffect(ModEffects.WHISKY.get());

        if (!shouldPlay) {
            stopHeartbeat(minecraft);
            return;
        }

        // isActive() нужен на случай, если звук остановился сам
        // (перезагрузка ресурсов, смена звукового устройства и т.п.).
        if (heartbeat == null || !minecraft.getSoundManager().isActive(heartbeat)) {
            startHeartbeat(minecraft);
        }
    }

    private static void startHeartbeat(Minecraft minecraft) {
        heartbeat = new SimpleSoundInstance(
                ModSounds.HEART_BEAT.get().getLocation(),
                SoundSource.MASTER,
                VOLUME,
                PITCH,
                SoundInstance.createUnseededRandom(),
                true,  // looping
                0,     // delay
                SoundInstance.Attenuation.NONE,
                0.0D, 0.0D, 0.0D,
                true   // relative — слышно только локальному игроку
        );
        minecraft.getSoundManager().play(heartbeat);
    }

    private static void stopHeartbeat(Minecraft minecraft) {
        if (heartbeat != null) {
            minecraft.getSoundManager().stop(heartbeat);
            heartbeat = null;
        }
    }
}
