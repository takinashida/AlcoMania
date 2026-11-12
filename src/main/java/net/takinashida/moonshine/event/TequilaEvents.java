package net.takinashida.moonshine.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.MoonShine;

/**
 * Механика текилы.
 *
 *  - Попадание снаряда игрока с эффектом «Текила» накладывает на цель
 *    горение.
 *  - Любой новый урон по цели с горением обновляет его длительность.
 *  - Цель с горением получает на 50% меньше исцеления.
 *
 * Урон самого горения помечается флагом, чтобы он не обновлял эффект
 * и не превращал его в бесконечный.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID)
public final class TequilaEvents {

    /** true, пока TequilaBurnEffect наносит свой собственный урон. */
    private static boolean burnDamageInProgress = false;

    private TequilaEvents() {
    }

    public static void setBurnDamageInProgress(boolean value) {
        burnDamageInProgress = value;
    }

    // LOWEST: сначала пусть отработают щиты/неуязвимость и отменят событие,
    // чтобы поглощённый удар не накладывал горение.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()
                || burnDamageInProgress
                || event.isCanceled()
                || event.getAmount() <= 0.0F) {
            return;
        }

        LivingEntity target = event.getEntity();

        boolean shouldApply = target.hasEffect(ModEffects.TEQUILA_BURN.get());

        // Выстрел игрока под текилой накладывает горение.
        boolean isShot = event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                || event.getSource().getDirectEntity() instanceof Projectile;
        if (!shouldApply
                && isShot
                && event.getSource().getEntity() instanceof Player shooter
                && shooter.hasEffect(ModEffects.TEQUILA.get())) {
            shouldApply = true;
        }

        if (shouldApply) {
            target.addEffect(new MobEffectInstance(
                    ModEffects.TEQUILA_BURN.get(),
                    Config.tequilaBurnDurationSeconds * 20,
                    0,
                    false,
                    true,
                    true
            ));
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity().hasEffect(ModEffects.TEQUILA_BURN.get())) {
            float multiplier = 1.0F - Config.tequilaHealingReductionPercent / 100.0F;
            event.setAmount(event.getAmount() * multiplier);
        }
    }
}
