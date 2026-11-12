package net.takinashida.moonshine.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;

/**
 * Яблочный сидр.
 *
 * Характеристики (все значения можно поменять в конфиге):
 *  - опьянение: items.cider.drunkenness (по умолчанию +30);
 *  - кастомный эффект alcomania:cider (Скорость II) на items.cider.effectDurationSeconds;
 *  - применений: items.cider.uses (по умолчанию 2);
 *  - перезарядка: items.cider.cooldownSeconds (по умолчанию 30 секунд).
 */
public class AppleCider extends DrinkableItem {

    public AppleCider() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.ciderDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.ciderUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.ciderCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        // Выдаём НАШ кастомный эффект, а не ванильную MobEffects.MOVEMENT_SPEED.
        // Своя иконка + своё название в интерфейсе, плюс он не конфликтует
        // с обычной Скоростью из зелий (у модификаторов разные UUID).
        player.addEffect(new MobEffectInstance(
                ModEffects.CIDER.get(),
                secondsToTicks(Config.ciderEffectDurationSeconds),
                Config.ciderSpeedAmplifier
        ));
    }
}
