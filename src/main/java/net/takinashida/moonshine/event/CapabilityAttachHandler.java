package net.takinashida.moonshine.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.MoonShine;
import net.takinashida.moonshine.capability.DrunkennessCapability;
import net.takinashida.moonshine.capability.DrunkennessInstance;
import net.takinashida.moonshine.capability.DrunkennessProvider;
import net.takinashida.moonshine.network.Networking;
import net.takinashida.moonshine.network.SyncDrunkennessPacket;

/**
 * Отвечает за "прикрепление" данных об опьянении к игроку,
 * а также за их перенос между мирами и отправку на клиент.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID)
public final class CapabilityAttachHandler {

    /** Уникальный ключ capability внутри мода. */
    private static final ResourceLocation KEY = new ResourceLocation(MoonShine.MOD_ID, "drunkenness");

    private CapabilityAttachHandler() {
    }

    /**
     * Forge вызывает это событие для каждого объекта, к которому можно прикрепить capability.
     * Нас интересуют только игроки — каждому даём свой DrunkennessProvider.
     */
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            DrunkennessProvider provider = new DrunkennessProvider();
            event.addCapability(KEY, provider);
            // Когда игрок удалится — корректно "выключим" capability.
            event.addListener(provider::invalidate);
        }
    }

    /**
     * Игрок умирает или переходит в другой мир — Forge создаёт НОВЫЙ объект игрока.
     * Здесь мы переносим опьянение со старого объекта на новый.
     *
     * Важный момент: при смерти опьянение НЕ переносим (иначе игрок возродился бы
     * с 100 единицами и умер бы снова). При переходе между измерениями — переносим.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Временно "оживляем" capability старого игрока, чтобы прочитать значение.
        event.getOriginal().reviveCaps();
        int oldValue = event.getOriginal().getCapability(DrunkennessCapability.DRUNKENNESS)
                .map(DrunkennessInstance::getValue)
                .orElse(0);
        event.getOriginal().invalidateCaps();

        if (!event.isWasDeath()) {
            event.getEntity().getCapability(DrunkennessCapability.DRUNKENNESS)
                    .ifPresent(drunkenness -> drunkenness.setValue(oldValue));
        }
    }

    /** Игрок зашёл в мир — отправляем текущее значение на клиент, чтобы шкала была верной. */
    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.getEntity());
    }

    /** Игрок возродился — тоже синхронизируем (после смерти значение равно 0). */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.getEntity());
    }

    /** Отправляет значение опьянения конкретному игроку на клиент. */
    private static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(DrunkennessCapability.DRUNKENNESS)
                    .ifPresent(drunkenness -> Networking.sendToPlayer(
                            new SyncDrunkennessPacket(drunkenness.getValue()), serverPlayer));
        }
    }
}
