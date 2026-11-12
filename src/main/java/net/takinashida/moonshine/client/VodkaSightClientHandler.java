package net.takinashida.moonshine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.ModSounds;
import net.takinashida.moonshine.MoonShine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Client-side vodka effect: sonar-like pulses that highlight entities around
 * the drinker.
 *
 * Every {@link #PULSE_INTERVAL_TICKS} ticks the effect emits one pulse: the
 * sonar sound plays {@link #SONAR_LEAD_TICKS} ticks (0.5 s) before the glow,
 * then all entities within the configured radius glow for
 * {@link #GLOW_DURATION_TICKS} ticks (1 second), after which the glow is
 * removed again for the rest of the cycle.
 *
 * Vanilla Glowing would be visible to everyone, so we temporarily set the
 * glowing flag only on the drinking player's client. As a result:
 *  - the drinker sees entities within the configured radius through walls
 *    during each pulse;
 *  - other players see (and hear) nothing, because their clients are
 *    untouched.
 *
 * All changes are reverted when a pulse ends, the vodka effect ends, entities
 * leave the radius/unload, or the player leaves the world.
 *
 * NOTE: the glow flag must be written through {@link Entity#setSharedFlag}
 * (exposed by META-INF/accesstransformer.cfg), not through
 * {@link Entity#setGlowingTag}. See {@link #setClientGlowing} for details.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, value = Dist.CLIENT)
public final class VodkaSightClientHandler {

    /** Index of the "glowing" bit in the entity's shared flags byte. */
    private static final int GLOWING_FLAG = 6;

    /** One pulse every 5 seconds. */
    private static final int PULSE_INTERVAL_TICKS = 100;

    /** The glow stays visible for 1 second per pulse. */
    private static final int GLOW_DURATION_TICKS = 20;

    /** The sonar plays this many ticks (0.5 s) before the glow starts. */
    private static final int SONAR_LEAD_TICKS = 10;

    /** Tick inside the cycle at which the sonar plays. */
    private static final int SONAR_TICK = PULSE_INTERVAL_TICKS - SONAR_LEAD_TICKS;

    /** Sonar sound settings. */
    private static final float SONAR_VOLUME = 0.7F;
    private static final float SONAR_PITCH = 1.0F;

    /** Ticks elapsed since the current pulse cycle started. */
    private static int pulseTicks;

    /** Entity id -> true if we enabled the glow and must remove it later. */
    private static final Map<Integer, Boolean> TRACKED = new HashMap<>();

    /** Client level the current tracking belongs to. */
    private static ClientLevel trackedLevel;

    private VodkaSightClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;

        if (level == null || player == null) {
            TRACKED.clear();
            trackedLevel = null;
            // Start mid-cycle so a freshly applied effect pings first and
            // lights up SONAR_LEAD_TICKS later.
            pulseTicks = SONAR_TICK;
            return;
        }

        if (trackedLevel != level) {
            // The old level is unloaded; its entities no longer matter.
            TRACKED.clear();
            trackedLevel = level;
            pulseTicks = SONAR_TICK;
        }

        if (player.isSpectator() || !player.hasEffect(ModEffects.VODKA.get())) {
            releaseAll(level);
            pulseTicks = SONAR_TICK;
            return;
        }

        // Pulse timing: the sonar fires SONAR_LEAD_TICKS before the lit
        // window; the first GLOW_DURATION_TICKS of every cycle are lit and
        // the rest of the cycle is dark.
        if (pulseTicks == SONAR_TICK) {
            playSonar(minecraft);
        }

        boolean glowingNow = pulseTicks < GLOW_DURATION_TICKS;
        if (++pulseTicks >= PULSE_INTERVAL_TICKS) {
            pulseTicks = 0;
        }

        if (!glowingNow) {
            // Outside the pulse window nothing should stay highlighted.
            releaseAll(level);
            return;
        }

        double radius = Math.max(1.0D, Config.vodkaGlowRadius);
        Set<Integer> current = new HashSet<>();

        // Every entity (players and mobs) inside the configured radius.
        for (Entity entity : level.getEntities(
                player,
                player.getBoundingBox().inflate(radius),
                e -> e != player && !e.isSpectator())) {

            int id = entity.getId();
            current.add(id);

            // Do not touch real Glowing: it is already visible to everyone.
            if (hasVanillaGlowing(entity)) {
                continue;
            }

            Boolean weSet = TRACKED.get(id);
            if (weSet == null) {
                if (entity.isCurrentlyGlowing()) {
                    // Already glowing for another reason; don't break it.
                    TRACKED.put(id, false);
                } else {
                    setClientGlowing(entity, true);
                    TRACKED.put(id, true);
                }
            } else if (weSet) {
                if (!entity.isCurrentlyGlowing()) {
                    // The server resends the shared flags byte when, for
                    // example, the entity starts sneaking or sprinting; that
                    // clears our bit, so restore it.
                    setClientGlowing(entity, true);
                }
            } else if (!entity.isCurrentlyGlowing()) {
                // Another glow ended while vodka is still active; take over.
                setClientGlowing(entity, true);
                TRACKED.put(id, true);
            }
        }

        // Remove glow from entities that unloaded or disappeared.
        Iterator<Map.Entry<Integer, Boolean>> iterator = TRACKED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Boolean> entry = iterator.next();
            if (current.contains(entry.getKey())) {
                continue;
            }

            if (entry.getValue()) {
                clearGlow(level, entry.getKey());
            }
            iterator.remove();
        }
    }

    private static void releaseAll(ClientLevel level) {
        for (Map.Entry<Integer, Boolean> entry : TRACKED.entrySet()) {
            if (entry.getValue()) {
                clearGlow(level, entry.getKey());
            }
        }
        TRACKED.clear();
    }

    /**
     * Plays the sonar ping locally, SONAR_LEAD_TICKS before the glow starts.
     *
     * The sound is relative and non-positional, so only the drinking player
     * hears it; other clients are untouched.
     */
    private static void playSonar(Minecraft minecraft) {
        // Do not ping while the game is paused or a menu is open.
        if (minecraft.screen != null || minecraft.isPaused()) {
            return;
        }

        SoundInstance sound = new SimpleSoundInstance(
                ModSounds.SONAR.get().getLocation(),
                SoundSource.MASTER,
                SONAR_VOLUME,
                SONAR_PITCH,
                SoundInstance.createUnseededRandom(),
                false, // not looping
                0,     // no delay
                SoundInstance.Attenuation.NONE,
                0.0D, 0.0D, 0.0D,
                true   // relative — only the local player hears it
        );
        minecraft.getSoundManager().play(sound);
    }

    private static void clearGlow(ClientLevel level, int entityId) {
        Entity entity = level.getEntity(entityId);
        if (entity != null && !hasVanillaGlowing(entity)) {
            setClientGlowing(entity, false);
        }
    }

    /**
     * Turns the vanilla glowing flag on or off for this client only.
     *
     * {@link Entity#setGlowingTag} must not be used here. In 1.20.1 it writes
     * the local {@code hasGlowingTag} field and then does
     * {@code setSharedFlag(6, isCurrentlyGlowing())}; on the client
     * {@code isCurrentlyGlowing()} reads the shared flags byte, so that call
     * simply writes back the current value and never enables the flag. The
     * renderer checks {@code isCurrentlyGlowing()} too, which is why setting
     * the tag produced no visible outline.
     *
     * {@code setSharedFlag} writes the byte directly, so
     * {@code LevelRenderer} routes the entity through the outline pass.
     * The method is made public by META-INF/accesstransformer.cfg.
     */
    private static void setClientGlowing(Entity entity, boolean glowing) {
        entity.setSharedFlag(GLOWING_FLAG, glowing);
    }

    private static boolean hasVanillaGlowing(Entity entity) {
        return entity instanceof LivingEntity living && living.hasEffect(MobEffects.GLOWING);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TRACKED.clear();
        trackedLevel = null;
        pulseTicks = SONAR_TICK;
    }

    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && trackedLevel == minecraft.level) {
            releaseAll(minecraft.level);
        } else {
            TRACKED.clear();
        }
        trackedLevel = null;
        pulseTicks = SONAR_TICK;
    }
}
