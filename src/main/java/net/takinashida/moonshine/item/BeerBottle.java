package net.takinashida.moonshine.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.takinashida.moonshine.Config;

/**
 * Пиво (alcomania:beer).
 *
 * Характеристики (все значения можно поменять в конфиге):
 *  - опьянение: items.beer.drunkenness (по умолчанию +40);
 *  - лечение: items.beer.healAmount HP (по умолчанию 16 HP = 8 сердец);
 *  - применений: items.beer.uses (по умолчанию 2);
 *  - перезарядка: items.beer.cooldownSeconds (по умолчанию 0 — единственный напиток без кулдауна).
 */
public class BeerBottle extends DrinkableItem {

    public BeerBottle() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.beerDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.beerUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.beerCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        // heal(16) восстановит 8 сердец, но не больше максимального здоровья.
        player.heal(Config.beerHealAmount);
    }
}
