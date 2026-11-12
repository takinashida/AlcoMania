package net.takinashida.moonshine.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Кастомный эффект «Сидр» (alcomania:cider), который даёт яблочный сидр.
 *
 * Зачем отдельный эффект, а не ванильная «Скорость»:
 *  - у него своя иконка и своё название в интерфейсе;
 *  - мод может менять его поведение, не трогая ванильные эффекты;
 *  - он не конфликтует с обычной «Скоростью» из зелий.
 *
 * По силе эффект повторяет Скорость II:
 *  - в Minecraft один уровень скорости = +20% к скорости передвижения;
 *  - здесь базовый модификатор +20% (0.2), а применяется эффект с amplifier = 1,
 *    поэтому итог = +40% (Скорость II).
 *
 * Категория BENEFICIAL — эффект положительный (в интерфейсе рисуется с зелёным фоном).
 */
public class CiderEffect extends MobEffect {

    /**
     * Уникальный UUID модификатора скорости.
     *
     * Он ДОЛЖЕН отличаться от UUID ванильной скорости, иначе два эффекта
     * (Скорость и Сидр) будут удалять друг друга из-за одинакового id модификатора.
     * Формат обязан быть корректным UUID: 8-4-4-4-12 шестнадцатеричных символов.
     */
    private static final String SPEED_MODIFIER_UUID = "3c2a9b4e-6d1f-4a7b-9e2c-8f5d1a3b7c60";

    /** Насколько сильно базовый уровень эффекта увеличивает скорость (0.2 = +20%). */
    private static final double SPEED_PER_LEVEL = 0.3D;

    public CiderEffect() {
        // 0xE0A83C — тёплый янтарный цвет, ассоциируется с яблочным сидром.
        super(MobEffectCategory.BENEFICIAL, 0xE0A83C);

        // Добавляем модификатор скорости. Значение умножается на (amplifier + 1),
        // поэтому при amplifier = 1 игрок получит 0.2 * 2 = +40% скорости.
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_UUID,
                SPEED_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}
