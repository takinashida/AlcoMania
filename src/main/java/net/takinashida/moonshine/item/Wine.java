package net.takinashida.moonshine.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Вино.
 *
 * Характеристики (все значения можно поменять в конфиге):
 *  - опьянение: items.wine.drunkenness (по умолчанию +75);
 *  - кастомный эффект alcomania:wine:
 *      * -1 единица ко ВСЕМУ входящему урону, кроме голода и утопления
 *        (items.wine.damageReduction);
 *      * -10% к скорости передвижения (items.wine.speedPenaltyPercent);
 *  - длительность эффекта: items.wine.effectDurationSeconds (по умолчанию 30 секунд);
 *  - применений: items.wine.uses (по умолчанию 4);
 *  - перезарядка: items.wine.cooldownSeconds (по умолчанию 30 секунд).
 */
public class Wine extends DrinkableItem {

    public Wine() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.wineDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.wineUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.wineCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        // Скорость уменьшает сам эффект WineEffect (модификатор атрибута),
        // а снижение входящего урона считает CombatEvents.
        player.addEffect(new MobEffectInstance(
                ModEffects.WINE.get(),
                secondsToTicks(Config.wineEffectDurationSeconds),
                0
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.alcomania.wine",
                        Config.wineDamageReduction, Config.wineSpeedPenaltyPercent)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
