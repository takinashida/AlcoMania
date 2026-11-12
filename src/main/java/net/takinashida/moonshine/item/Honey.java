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
 * Медовуха.
 *
 * Характеристики (все значения можно поменять в конфиге):
 *  - опьянение: items.honey.drunkenness (по умолчанию +30);
 *  - применений: items.honey.uses (по умолчанию 4);
 *  - перезарядка: items.honey.cooldownSeconds (по умолчанию 10 секунд);
 *  - эффект alcomania:honey — одноразовый щит, который блокирует 100%
 *    урона из любых источников и исчезает после первого поглощения.
 */
public class Honey extends DrinkableItem {

    public Honey() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.honeyDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.honeyUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.honeyCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        // INFINITE_DURATION: щит не имеет таймера и держится до первого удара.
        // visible = false — без постоянных частиц вокруг игрока,
        // showIcon = true — иконка эффекта видна в интерфейсе.
        player.addEffect(new MobEffectInstance(
                ModEffects.HONEY.get(),
                secondsToTicks(Config.honeyEffectDurationSeconds),
                0,
                false,
                false,
                true
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.alcomania.honey")
                .withStyle(ChatFormatting.GOLD));
    }
}
