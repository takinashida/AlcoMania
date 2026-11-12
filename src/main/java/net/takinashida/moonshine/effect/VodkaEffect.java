package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Эффект «Водка» (alcomania:vodka).
 *
 * Пока эффект активен, клиент выпившего раз в 5 секунд на 1 секунду
 * подсвечивает все сущности в радиусе (по умолчанию 16 блоков) и позволяет
 * видеть их сквозь стены; в начале каждого импульса играет звук сонара.
 * Подсветка и звук существуют только на клиенте этого игрока — другие игроки
 * ничего не видят и не слышат.
 *
 * Категория BENEFICIAL — эффект положительный (зелёная рамка в инвентаре).
 */
public class VodkaEffect extends MobEffect {

    public VodkaEffect() {
        // 0xBFE9FF — прозрачно-голубой цвет, ассоциируется с водкой.
        super(MobEffectCategory.BENEFICIAL, 0xBFE9FF);
    }
}
