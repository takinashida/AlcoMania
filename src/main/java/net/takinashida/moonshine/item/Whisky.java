package net.takinashida.moonshine.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;

/**
 * Виски.
 *
 * Даёт эффект «Виски» на 10 секунд (по умолчанию):
 *  - пока эффект активен, игрок полностью неуязвим;
 *  - когда эффект заканчивается или его снимают (молоко/команда),
 *    игрок гарантированно умирает.
 *
 * В момент употребления один раз проигрывается звук плевка ламы.
 */
public class Whisky extends DrinkableItem {

    public Whisky() {
        super(new net.minecraft.world.item.Item.Properties());
    }

    @Override
    protected int getDrunkenness() {
        return Config.whiskyDrunkenness;
    }

    @Override
    protected int getMaxUses() {
        return Config.whiskyUses;
    }

    @Override
    protected int getCooldownSeconds() {
        return Config.whiskyCooldownSeconds;
    }

    @Override
    protected void applyBonus(Player player, Level level) {
        player.addEffect(new MobEffectInstance(
                ModEffects.WHISKY.get(),
                secondsToTicks(Config.whiskyEffectDurationSeconds),
                0
        ));

        // Один звук плевка ламы от игрока в момент, когда он выпил виски.
        level.playSound(
                null,
                player.getX(),
                player.getY() + player.getEyeHeight(),
                player.getZ(),
                SoundEvents.LLAMA_SPIT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
}
