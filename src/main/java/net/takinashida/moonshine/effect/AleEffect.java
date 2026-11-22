package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект «Эль» (alcomania:ale).
 *
 * Это эффект-таймер: пока он активен, ничего не происходит. Когда он
 * заканчивается естественным образом, AleEvents исцеляет игрока.
 */
public class AleEffect extends MobEffect {

    public AleEffect() {
        // 0xC68A2B — янтарный цвет эля.
        super(MobEffectCategory.BENEFICIAL, 0xC68A2B);
    }
}
