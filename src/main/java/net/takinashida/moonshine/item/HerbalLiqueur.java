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
 * Травяной ликёр.
 *
 * Характеристики:
 *  - опьянение: +50;
 *  - 2 применения;
 *  - перезарядка: 10 минут;
 *  - эффект «Травяной ликёр» на 6 минут:
 *      * +50% к скорости добычи киркой и топором;
 *      * 20% шанс двойного лута на железной/угольной руде и jeg:brimstone_ore.
 */
public class HerbalLiqueur extends DrinkableItem {

    public HerbalLiqueur() {
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.herbalLiqueurDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.herbalLiqueurUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.herbalLiqueurCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        player.addEffect(new MobEffectInstance(
                ModEffects.HERBAL_LIQUEUR.get(),
                secondsToTicks(Config.herbalLiqueurEffectDurationSeconds),
                0
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.alcomania.herbal_liqueur")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
