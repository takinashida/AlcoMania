package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект «Виски» (alcomania:whisky).
 *
 * Пока эффект активен, CombatEvents отменяет весь входящий урон.
 * Смерть при окончании/снятии эффекта обрабатывается в ModEvents.
 */
public class WhiskyEffect extends MobEffect {

    public WhiskyEffect() {
        // Light blue color
        super(MobEffectCategory.BENEFICIAL, 0xADD8E6);
    }
}
