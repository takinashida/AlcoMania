package net.takinashida.moonshine.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.takinashida.moonshine.Config;

import java.util.UUID;

/**
 * Эффект «Вино» (alcomania:wine).
 *
 * Даёт два эффекта:
 *  1. -1 единица ко всему входящему урону (кроме голода и утопления) —
 *     считается в CombatEvents (LivingHurtEvent);
 *  2. -10% к скорости передвижения — модификатор атрибута MOVEMENT_SPEED здесь.
 *
 * Почему модификатор добавляется вручную, а не через addAttributeModifier()
 * в конструкторе:
 *  - конструктор эффекта выполняется ДО загрузки конфига, поэтому там доступны
 *    только значения по умолчанию;
 *  - addAttributeModifiers() вызывается в момент выдачи эффекта, когда конфиг
 *    уже загружен, поэтому штраф всегда берётся из items.wine.speedPenaltyPercent.
 */
public class WineEffect extends MobEffect {

    /**
     * Уникальный UUID модификатора скорости.
     *
     * Он не должен совпадать с UUID замедления или других эффектов, иначе
     * модификаторы начнут удалять друг друга.
     */
    private static final UUID SPEED_MODIFIER_UUID =
            UUID.fromString("5f7d2c1a-9b3e-4c8d-a1f2-7e6d0b4c9a35");

    /** Имя модификатора — видно в отладочной информации атрибутов. */
    private static final String SPEED_MODIFIER_NAME = "alcomania:wine_speed";

    public WineEffect() {
        // 0x8E1B2E — тёмно-красный цвет вина.
        super(MobEffectCategory.BENEFICIAL, 0x8E1B2E);
    }

    /**
     * Вызывается, когда эффект появляется на сущности (и при обновлении).
     * Здесь мы и накладываем штраф к скорости.
     */
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        AttributeInstance speed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }

        // Сначала снимаем старый модификатор — на случай повторного вызова.
        speed.removeModifier(SPEED_MODIFIER_UUID);

        double penalty = Config.wineSpeedPenaltyPercent / 100.0D;
        if (penalty <= 0.0D) {
            return;
        }

        // MULTIPLY_TOTAL: итог = база * (1 + сумма всех таких модификаторов).
        // Поэтому -0.10 = ровно -10% скорости.
        speed.addTransientModifier(new AttributeModifier(
                SPEED_MODIFIER_UUID,
                SPEED_MODIFIER_NAME,
                -penalty,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        ));
    }

    /** Вызывается, когда эффект заканчивается — убираем штраф к скорости. */
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        AttributeInstance speed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_MODIFIER_UUID);
        }
    }
}
