package net.takinashida.moonshine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.takinashida.moonshine.MoonShine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Серверный эффект водки на клиенте: подсветка сущностей в радиусе.
 *
 * Водка не накладывает настоящий ванильный Glowing (его увидели бы все),
 * а временно включает glowing-флаг только на клиенте выпившего игрока.
 * Поэтому:
 *  - сам игрок видит сущности в радиусе 16 блоков сквозь стены;
 *  - другие игроки не видят ничего, потому что на их клиентах флаг не меняется.
 *
 * Все изменения откатываются, когда эффект водки заканчивается, сущность
 * выходит из радиуса или игрок покидает мир.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, value = Dist.CLIENT)
public final class VodkaSightClientHandler {

    /** id сущности -> true, если подсветку включили именно мы (её нужно снять). */
    private static final Map<Integer, Boolean> TRACKED = new HashMap<>();

    /** Мир, для которого сейчас действует подсветка. */
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
            return;
        }

        if (trackedLevel != level) {
            // Старый мир выгружен — его сущности уже не важны.
            TRACKED.clear();
            trackedLevel = level;
        }

        if (player.isSpectator() || !player.hasEffect(ModEffects.VODKA.get())) {
            releaseAll(level);
            return;
        }

        double radius = Math.max(1.0D, Config.vodkaGlowRadius);
        Set<Integer> nearby = new HashSet<>();

        for (Entity entity : level.getEntities(player,
                player.getBoundingBox().inflate(radius),
                e -> e != player && !e.isSpectator())) {

            int id = entity.getId();
            nearby.add(id);

            // Настоящий Glowing не трогаем: он виден всем и так.
            if (hasVanillaGlowing(entity)) {
                continue;
            }

            Boolean weSet = TRACKED.get(id);
            if (weSet == null) {
                if (entity.isCurrentlyGlowing()) {
                    // Уже светится по другой причине — не ломаем чужую подсветку.
                    TRACKED.put(id, false);
                } else {
                    entity.setGlowingTag(true);
                    TRACKED.put(id, true);
                }
            } else if (weSet) {
                if (!entity.isCurrentlyGlowing()) {
                    entity.setGlowingTag(true);
                }
            } else if (!entity.isCurrentlyGlowing()) {
                // Чужая подсветка закончилась, а водка ещё действует — берём на себя.
                entity.setGlowingTag(true);
                TRACKED.put(id, true);
            }
        }

        // Снимаем подсветку с тех, кто вышел из радиуса или исчез.
        Iterator<Map.Entry<Integer, Boolean>> iterator = TRACKED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Boolean> entry = iterator.next();
            if (nearby.contains(entry.getKey())) {
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

    private static void clearGlow(ClientLevel level, int entityId) {
        Entity entity = level.getEntity(entityId);
        if (entity != null && !hasVanillaGlowing(entity)) {
            entity.setGlowingTag(false);
        }
    }

    private static boolean hasVanillaGlowing(Entity entity) {
        return entity instanceof LivingEntity living && living.hasEffect(MobEffects.GLOWING);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TRACKED.clear();
        trackedLevel = null;
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
    }
}
