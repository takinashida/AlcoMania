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
 * Ликёр.
 *
 * Характеристики:
 *  - опьянение: +50;
 *  - 2 применения;
 *  - перезарядка: 10 минут;
 *  - эффект «Ликёр» на 6 минут:
 *      * +50% к скорости добычи киркой и топором;
 *      * 20% шанс двойного лута на железной/угольной руде и jeg:brimstone_ore.
 */
public class Liqueur extends DrinkableItem {

    public Liqueur() {
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.liqueurDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.liqueurUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.liqueurCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        player.addEffect(new MobEffectInstance(
                ModEffects.LIQUEUR.get(),
                secondsToTicks(Config.liqueurEffectDurationSeconds),
                0
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.alcomania.liqueur")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
