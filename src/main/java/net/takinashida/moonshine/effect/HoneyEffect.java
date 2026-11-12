package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект «Медовый щит» (alcomania:honey).
 *
 * Пока эффект активен, CombatEvents отменяет весь входящий урон.
 * В отличие от виски, щит одноразовый: после первого поглощённого удара
 * эффект сразу снимается, а игрок остаётся жив.
 *
 * Длительность эффекта не ограничена — он держится до первого попадания.
 */
public class HoneyEffect extends MobEffect {

    public HoneyEffect() {
        // 0xE8A317 — медово-золотой цвет.
        super(MobEffectCategory.BENEFICIAL, 0xE8A317);
    }
}


