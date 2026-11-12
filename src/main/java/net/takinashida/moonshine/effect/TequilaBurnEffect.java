package net.takinashida.moonshine.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.event.TequilaEvents;

/**
 * Эффект «Горение» от текилы (alcomania:tequila_burn).
 *
 * Наносит 1 урон раз в 2 секунды в течение 8 секунд (всего 4 урона).
 * Каждое новое попадание по цели с этим эффектом обновляет длительность.
 * Входящее исцеление цели уменьшается на 50% (см. TequilaEvents).
 *
 * Огнестойкость НЕ защищает от этого урона, а вода/снег не останавливают
 * эффект — он продолжает работать в любых условиях.
 *
 * Визуал — только партиклы пламени вокруг цели, без огненной анимации
 * на самой сущности/экране.
 */
public class TequilaBurnEffect extends MobEffect {

    public TequilaBurnEffect() {
        // 0xFF6A00 — оранжево-красный цвет огня.
        super(MobEffectCategory.HARMFUL, 0xFF6A00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Тикаем каждый тик ради частиц. Урон наносим только в нужные моменты.
        return duration > 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }

        // Визуал: только партиклы пламени вокруг цели.
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5D,
                    entity.getZ(),
                    2,
                    entity.getBbWidth() * 0.35D,
                    entity.getBbHeight() * 0.35D,
                    entity.getBbWidth() * 0.35D,
                    0.01D
            );
        }

        MobEffectInstance instance = entity.getEffect(ModEffects.TEQUILA_BURN.get());
        if (instance == null) {
            return;
        }

        int interval = Math.max(1, Config.tequilaBurnIntervalSeconds * 20);

        // При длительности 8 секунд (160 тиков) удары происходят на
        // 2, 4, 6 и 8-й секундах: 121, 81, 41 и 1 тик длительности.
        if (instance.getDuration() % interval != 1) {
            return;
        }

        // Флаг нужен, чтобы собственный урон горения не обновлял эффект
        // и не продлевал его бесконечно.
        TequilaEvents.setBurnDamageInProgress(true);
        try {
            // Не onFire(): огнестойкость не должна защищать от горения текилы.
            // Магический урон не входит в fire-тег, но визуал огня остаётся.
            entity.hurt(entity.damageSources().magic(), Config.tequilaBurnDamage);
        } finally {
            TequilaEvents.setBurnDamageInProgress(false);
        }
    }
}
