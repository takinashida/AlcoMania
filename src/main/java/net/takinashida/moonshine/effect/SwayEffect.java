package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект "Шатание" (alcomania:sway).
 *
 * Сам по себе эффект ничего не делает на сервере — это "маркер".
 * Клиент видит, что у игрока есть этот эффект, и:
 *  - плавно шатает камеру (ViewportEvent.ComputeCameraAngles);
 *  - включает лёгкое размытие экрана (пост-шейдер blur).
 *
 * Категория HARMFUL — эффект считается негативным (красная рамка в инвентаре).
 */
public class SwayEffect extends MobEffect {

    public SwayEffect() {
        // 0x8A2BE2 — фиолетовый цвет иконки/частиц эффекта.
        super(MobEffectCategory.HARMFUL, 0x8A2BE2);
    }
}
