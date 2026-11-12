package net.takinashida.moonshine.event;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.MoonShine;

/**
 * Боевые эффекты напитков.
 *
 * Здесь обрабатываются эффекты, которые должны влиять на ЛЮБОЙ урон:
 *  - вино: -N единиц к входящему урону игрока (кроме голода и утопления);
 *  - медовуха: одноразовая блокировка 100% входящего урона.
 *
 * Медовуха обрабатывается в LivingHurtEvent (до брони), а вино —
 * в LivingDamageEvent, чтобы вычитать 1 из уже итогового урона.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID)
public final class CombatEvents {

    private CombatEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // На клиенте урон не считается — выходим, чтобы не удвоить логику.
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // ---------- Виски: полная неуязвимость ----------
        // Пока эффект активен, отменяем ЛЮБОЙ урон. Исключение — genericKill:
        // он используется самим модом, чтобы гарантированно убить игрока
        // в момент окончания/снятия эффекта (а также командой /kill).
        if (event.getEntity() instanceof Player victim
                && victim.hasEffect(ModEffects.WHISKY.get())
                && !event.getSource().is(DamageTypes.GENERIC_KILL)) {
            event.setCanceled(true);
            return;
        }

        // ---------- Медовуха: одноразовый щит ----------
        // Блокирует 100% урона из любого источника и сразу снимается после
        // первого поглощённого удара. genericKill не поглощаем — иначе /kill
        // и служебное убийство игрока модом перестали бы работать.
        if (event.getEntity() instanceof Player victim
                && victim.hasEffect(ModEffects.HONEY.get())
                && !event.getSource().is(DamageTypes.GENERIC_KILL)
                && !event.isCanceled()
                && event.getAmount() > 0.0F) {
            event.setCanceled(true);
            victim.removeEffect(ModEffects.HONEY.get());
        }
    }

    /**
     * Вино: фиксированное снижение уже итогового урона.
     *
     * Используем LivingDamageEvent, а не LivingHurtEvent, чтобы вычиталась
     * ровно 1 единица после брони и магии. Голод и утопление не трогаем.
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Player victim
                && victim.hasEffect(ModEffects.WINE.get())
                && !event.getSource().is(DamageTypes.STARVE)
                && !event.getSource().is(DamageTypes.DROWN)) {
            event.setAmount(Math.max(0.0F, event.getAmount() - Config.wineDamageReduction));
        }
    }
}
