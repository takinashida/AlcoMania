package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект «Ликёр» (alcomania:liqueur).
 *
 * Маркер: пока активен, LiqueurEvents даёт +50% к скорости добычи
 * киркой/топором и 20% шанс двойного лута на железной/угольной руде
 * и jeg:brimstone_ore.
 */
public class LiqueurEffect extends MobEffect {

    public LiqueurEffect() {
        // 0x9B59B6 — фиолетовый ликёрный цвет.
        super(MobEffectCategory.BENEFICIAL, 0x9B59B6);
    }
}
