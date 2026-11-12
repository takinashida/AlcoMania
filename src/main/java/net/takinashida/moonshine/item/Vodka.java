package net.takinashida.moonshine.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;

/**
 * Водка.
 *
 * Характеристики (все значения можно поменять в конфиге):
 *  - опьянение: items.vodka.drunkenness (по умолчанию +100);
 *  - эффект alcomania:vodka: подсвечивает все сущности в радиусе
 *    items.vodka.glowRadius (по умолчанию 16 блоков) и позволяет видеть
 *    их сквозь стены только выпившему игроку;
 *  - длительность эффекта: items.vodka.effectDurationSeconds (по умолчанию 30 секунд);
 *  - применений: items.vodka.uses (по умолчанию 4);
 *  - перезарядка: items.vodka.cooldownSeconds (по умолчанию 30 секунд).
 */
public class Vodka extends DrinkableItem {

    public Vodka() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.vodkaDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.vodkaUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.vodkaCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        // Сам бонус к урону начисляется в CombatEvents: там мы видим и ближний бой,
        // и стрелы, и любой другой источник урона. Здесь только вешаем эффект-маркер
        // с иконкой и таймером.
        player.addEffect(new MobEffectInstance(
                ModEffects.VODKA.get(),
                secondsToTicks(Config.vodkaEffectDurationSeconds),
                0
        ));
    }
}
