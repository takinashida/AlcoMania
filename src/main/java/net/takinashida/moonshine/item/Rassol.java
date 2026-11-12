package net.takinashida.moonshine.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.capability.DrunkennessCapability;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Рассол (Pickle Brine).
 *
 * Особый "напиток" — лечебный, а не алкогольный:
 *  - снимает ВСЕ эффекты с игрока (и плохие, и хорошие);
 *  - обнуляет шкалу опьянения;
 *  - опьянения сам не добавляет (getDrunkenness() = 0).
 *
 * Значения в конфиге:
 *  - items.rassol.uses — количество применений (по умолчанию 1);
 *  - items.rassol.cooldownSeconds — перезарядка (по умолчанию 30 секунд).
 */
public class Rassol extends DrinkableItem {

    public Rassol() {
        // Общие настройки (1 стак, прочность-заглушка) задаются в DrinkableItem.
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        // Рассол не добавляет опьянения — он его сбрасывает.
        return 0;
    }

    @Override
    protected int getMaxUses() {
        return Config.rassolUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.rassolCooldownSeconds;
    }

    @Override
    protected boolean dropsEmptyBottle() {
        // Рассол не алкоголь — пустую бутылку от него не оставляем.
        return false;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        // 1. Снимаем все эффекты. removeAllEffects() сам отправит клиенту пакеты
        //    об удалении, поэтому дополнительная синхронизация не нужна.
        player.removeAllEffects();

        // 2. Опускаем шкалу опьянения до нуля. Синхронизацию с клиентом
        //    DrinkableItem делает сразу после вызова этого метода.
        player.getCapability(DrunkennessCapability.DRUNKENNESS)
                .ifPresent(drunkenness -> drunkenness.setValue(0));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // Поясняем, что предмет делает: иначе из названия это неочевидно.
        tooltip.add(Component.translatable("tooltip.alcomania.rassol")
                .withStyle(ChatFormatting.GREEN));
    }
}
