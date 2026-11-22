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
 * Эль (alcomania:ale).
 *
 * Характеристики (все значения можно поменять в конфиге):
 *  - опьянение: items.ale.drunkenness (по умолчанию +40);
 *  - применений: items.ale.uses (по умолчанию 2);
 *  - перезарядка: items.ale.cooldownSeconds (по умолчанию 10 секунд);
 *  - эффект alcomania:ale на items.ale.effectDurationSeconds (по умолчанию 8 секунд).
 *    Когда эффект заканчивается, игрок исцеляется на items.ale.healAmount HP
 *    (по умолчанию 12 HP = 6 сердец).
 */
public class Ale extends DrinkableItem {

    public Ale() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.aleDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.aleUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.aleCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        // Сам эффект ничего не делает: он служит таймером.
        // Лечение выдаётся в AleEvents, когда эффект заканчивается.
        player.addEffect(new MobEffectInstance(
                ModEffects.ALE.get(),
                secondsToTicks(Config.aleEffectDurationSeconds),
                0
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.alcomania.ale",
                        Config.aleEffectDurationSeconds, Config.aleHealAmount)
                .withStyle(ChatFormatting.GREEN));
    }
}
