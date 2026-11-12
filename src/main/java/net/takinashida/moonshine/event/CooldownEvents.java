package net.takinashida.moonshine.event;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import net.takinashida.moonshine.ModItems;
import net.takinashida.moonshine.MoonShine;
import net.takinashida.moonshine.item.DrinkableItem;

/**
 * Переносит кулдауны напитков на нового игрока после смерти.
 *
 * Ванильный Minecraft при респавне создаёт новый объект игрока и не копирует
 * ItemCooldowns. Поэтому перезарядка напитков пропадала: умерев сразу после
 * глотка, игрок мог возродиться и выпить снова без ожидания.
 *
 * Работает в два шага:
 *  1. {@link PlayerEvent.Clone} — копируем остаток кулдауна со старого
 *     объекта игрока на новый (серверная сторона);
 *  2. {@link PlayerEvent.PlayerRespawnEvent} — повторно выдаём кулдаун уже
 *     после пакета респавна. Клиент при респавне пересоздаёт LocalPlayer,
 *     поэтому пакет кулдауна, отправленный на шаге 1, до него не доходит.
 *
 * Остаток считается в долях от полной длительности, поэтому не нужно знать,
 * сколько тиков успело пройти на старом объекте игрока. Затрагиваются только
 * напитки этого мода ({@link DrinkableItem}); кулдауны ванильных предметов
 * (эндер-жемчуг, хорус и т.п.) остаются как в ваниле.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID)
public final class CooldownEvents {

    private CooldownEvents() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Clone вызывается и при смене измерения, но там это тот же игрок
        // и кулдауны никуда не деваются.
        if (!event.isWasDeath()) {
            return;
        }

        copyCooldowns(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();

        // Перечитываем остаток и добавляем заново: addCooldown на сервере
        // отправит клиенту свежий ClientboundCooldownPacket, и на этот раз
        // он придёт уже после ClientboundRespawnPacket.
        copyCooldowns(player, player);
    }

    /** Переносит остаток кулдаунов напитков с {@code from} на {@code to}. */
    private static void copyCooldowns(Player from, Player to) {
        ItemCooldowns fromCooldowns = from.getCooldowns();
        ItemCooldowns toCooldowns = to.getCooldowns();

        for (RegistryObject<Item> entry : ModItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof DrinkableItem drinkable)) {
                continue;
            }

            int totalTicks = drinkable.getCooldownTicks();
            if (totalTicks <= 0 || !fromCooldowns.isOnCooldown(drinkable)) {
                continue;
            }

            float remainingFraction = fromCooldowns.getCooldownPercent(drinkable, 0.0F);
            int remainingTicks = Mth.ceil(remainingFraction * totalTicks);
            if (remainingTicks > 0) {
                toCooldowns.addCooldown(drinkable, remainingTicks);
            }
        }
    }
}
