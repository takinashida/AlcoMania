package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект «Водка» (alcomania:vodka).
 *
 * Пока эффект активен, клиент выпившего подсвечивает все сущности
 * в радиусе 16 блоков (настраивается) и позволяет видеть их сквозь стены.
 * Подсветка существует только на клиенте этого игрока — другие игроки
 * ничего не видят.
 *
 * Категория BENEFICIAL — эффект положительный (зелёная рамка в инвентаре).
 */
public class VodkaEffect extends MobEffect {

    public VodkaEffect() {
        // 0xBFE9FF — прозрачно-голубой цвет, ассоциируется с водкой.
        super(MobEffectCategory.BENEFICIAL, 0xBFE9FF);
    }
}
