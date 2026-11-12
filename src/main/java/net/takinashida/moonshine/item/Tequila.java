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
 * Текила.
 *
 * Характеристики (все значения можно поменять в конфиге):
 *  - опьянение: items.tequila.drunkenness (по умолчанию +100);
 *  - применений: items.tequila.uses (по умолчанию 4);
 *  - перезарядка: items.tequila.cooldownSeconds (по умолчанию 0);
 *  - эффект «Текила» на items.tequila.effectDurationSeconds (30 секунд):
 *    попадания снарядов накладывают на цель горение.
 *
 * Горение: 1 урон раз в 2 секунды в течение 8 секунд, партиклы пламени
 * вокруг цели, а также -50% к входящему исцелению. Новое попадание
 * обновляет эффект. Огнестойкость не защищает, вода не останавливает эффект.
 */
public class Tequila extends DrinkableItem {

    public Tequila() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.tequilaDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.tequilaUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.tequilaCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        player.addEffect(new MobEffectInstance(
                ModEffects.TEQUILA.get(),
                secondsToTicks(Config.tequilaEffectDurationSeconds),
                0
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.alcomania.tequila")
                .withStyle(ChatFormatting.GOLD));
    }
}
