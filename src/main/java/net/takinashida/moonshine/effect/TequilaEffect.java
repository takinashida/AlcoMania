package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект «Текила» (alcomania:tequila).
 *
 * Это бафф-маркер выпившего: пока он активен, попадания его снарядов
 * накладывают на цель {@link TequilaBurnEffect}.
 *
 * Сама логика попадания находится в TequilaEvents.
 */
public class TequilaEffect extends MobEffect {

    public TequilaEffect() {
        // 0xD4A017 — золотисто-янтарный цвет текилы.
        super(MobEffectCategory.BENEFICIAL, 0xD4A017);
    }
}
