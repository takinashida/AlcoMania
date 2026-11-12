package net.takinashida.moonshine.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.MoonShine;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.ModSounds;
import net.takinashida.moonshine.capability.DrunkennessCapability;
import net.takinashida.moonshine.effect.NightVisionEffect;
import net.takinashida.moonshine.network.Networking;
import net.takinashida.moonshine.network.SyncDrunkennessPacket;

/**
 * Главная серверная логика опьянения.
 *
 * Что здесь происходит каждый тик:
 *  1. Если опьянение достигло смертельного порога — игрок сразу умирает.
 *  2. Иначе раз в секунду (по умолчанию) опьянение уменьшается на 1.
 *     Новое значение сразу отправляется клиенту, чтобы шкала была актуальной.
 *  3. По порогам 25 / 50 / 75 / 100 выдаются эффекты:
 *       25+ — Sway (шатание камеры, визуал на клиенте);
 *       50+ — замедление;
 *       75+ — слабость;
 *       100 — слепота (перед смертью).
 *  4. Если опьянение СТРОГО ВЫШЕ witherThreshold — вешается иссушение IV.
 *
 * Все пороги и длительности берутся из конфига (класс Config), поэтому
 * баланс можно менять без правки кода.
 *
 * Вся логика выполняется ТОЛЬКО на сервере. Клиент получает лишь число для HUD
 * и сам рисует визуальные эффекты.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID)
public final class ModEvents {

    private ModEvents() {
    }

    /**
     * Вызывается каждый тик для каждого игрока.
     *
     * У TickEvent две фазы: START (начало тика) и END (конец тика).
     * Нам нужна только END, иначе логика выполнится дважды за тик.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        // На клиенте ничего не делаем: иначе эффекты и смерть сработают дважды.
        if (player.level().isClientSide) {
            return;
        }

        player.getCapability(DrunkennessCapability.DRUNKENNESS).ifPresent(drunkenness -> {
            // ---------- 1. Смерть ----------
            // Важно проверять её ДО трезвения. Иначе получится неприятный баг:
            // игрок выпил до 100, а в этот же тик сработало ежесекундное уменьшение,
            // значение стало 99 — и игрок выжил, хотя должен был умереть.
            //
            // applyEffects вызываем и здесь: если deathThreshold поднят в конфиге
            // выше 100, игрок успевает получить иссушение IV перед смертью.
//            if (drunkenness.getValue() >= Config.deathThreshold) {
//                applyEffects(player, drunkenness.getValue());
//                if (player.isAlive()) {
//                    player.kill();
//                }
//                return;
//            }

            // ---------- 2. Трезвение ----------
            // tickCount — сколько тиков существует игрок. Раз в Config.decayIntervalTicks
            // (по умолчанию 20 тиков = 1 секунда) уменьшаем опьянение.
            if (drunkenness.getValue() > 0
                    && player.tickCount % Config.decayIntervalTicks == 0) {

                drunkenness.setValue(drunkenness.getValue() - Config.decayAmount);

                // Сразу сообщаем клиенту новое значение, чтобы шкала и шатание
                // обновились без задержки.
                if (player instanceof ServerPlayer serverPlayer) {
                    Networking.sendToPlayer(
                            new SyncDrunkennessPacket(drunkenness.getValue()), serverPlayer);
                }
            }

            // ---------- 3. Пороговые эффекты ----------
            applyEffects(player, drunkenness.getValue());
        });
    }

    /**
     * Виски: эффект закончился — игрок гарантированно умирает.
     */
    @SubscribeEvent
    public static void onWhiskyExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (event.getEffectInstance().getEffect() == ModEffects.WHISKY.get() && player.isAlive()) {
            player.kill();
        }
    }

    /**
     * Виски: эффект сняли раньше времени (молоко, /effect clear и т.п.) —
     * игрок всё равно гарантированно умирает.
     */
    @SubscribeEvent
    public static void onWhiskyRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (event.getEffect() == ModEffects.WHISKY.get() && player.isAlive()) {
            player.kill();
        }
    }

    /**
     * Медовуха: эффект закончился естественным образом.
     * Играем звук разрушения щита.
     */
    @SubscribeEvent
    public static void onHoneyExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (event.getEffectInstance().getEffect() == ModEffects.HONEY.get()) {
            playHoneyBreakSound(player);
        }
    }

    /**
     * Медовуха: эффект сняли раньше времени (поглощённый удар, молоко,
     * /effect clear) — тоже играем звук разрушения щита.
     */
    @SubscribeEvent
    public static void onHoneyRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (event.getEffect() == ModEffects.HONEY.get()) {
            playHoneyBreakSound(player);
        }
    }

    private static void playHoneyBreakSound(Player player) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.HONEY_REMOVE.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    /** Кастомное ночное зрение сняли командой/молоком — убираем поддержку. */
    @SubscribeEvent
    public static void onCustomNightVisionRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof Player player
                && !player.level().isClientSide()
                && event.getEffect() == ModEffects.NIGHT_VISION.get()) {
            NightVisionEffect.removeSupportNightVision(player);
        }
    }

    /** Кастомное ночное зрение закончилось само — убираем поддержку. */
    @SubscribeEvent
    public static void onCustomNightVisionExpired(MobEffectEvent.Expired event) {
        if (event.getEntity() instanceof Player player
                && !player.level().isClientSide()
                && event.getEffectInstance().getEffect() == ModEffects.NIGHT_VISION.get()) {
            NightVisionEffect.removeSupportNightVision(player);
        }
    }


    /**
     * Выдаёт эффекты, соответствующие текущему уровню опьянения.
     *
     * Эффекты накапливаются: на 75 действуют Sway + замедление + слабость.
     */
    private static void applyEffects(Player player, int value) {
        // 25+: шатание камеры.
        if (value >= Config.swayThreshold) {
            keepEffect(player, ModEffects.SWAY.get(), 0);
        }

        // 50+: замедление (амплификатор 0 = уровень I).
//        if (value >= Config.slownessThreshold) {
//            keepEffect(player, MobEffects.MOVEMENT_SLOWDOWN, 0);
//        }

        // 75+: слабость (урон в ближнем бою уменьшается).
//        if (value >= Config.weaknessThreshold) {
//            keepEffect(player, MobEffects.WEAKNESS, 0);
//        }

        // 100: слепота. Игрок всё равно умирает в этом же тике, но эффект
        // соответствует описанию механики.
//        if (value >= Config.blindnessThreshold) {
//            keepEffect(player, MobEffects.BLINDNESS, 0);
//        }

        // Строго выше порога: иссушение (по умолчанию IV).
        // При deathThreshold = 100 и witherThreshold = 100 игрок умирает
        // в тот же тик, поэтому иссушение успевает только наложиться.
        // Поднимите deathThreshold (например, до 120), чтобы оно реально убивало.
        if (value > Config.witherThreshold) {
            keepEffect(player, MobEffects.WITHER, Config.witherAmplifier);
        }
    }

    /**
     * Поддерживает эффект активным, не отправляя пакет каждый тик.
     *
     * Логика:
     *  - если эффекта нет — добавляем;
     *  - если он скоро закончится (осталось меньше половины) — продлеваем;
     *  - если у игрока уже есть более сильная версия (например, от зелья) — не трогаем.
     *
     * @param effect    какой эффект поддерживать
     * @param amplifier сила эффекта: 0 = I, 1 = II и т.д.
     */
    private static void keepEffect(Player player, MobEffect effect, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);

        boolean missing = current == null;
        boolean almostExpired = current != null
                && current.getDuration() < Config.effectDurationTicks / 2;
        boolean weaker = current != null && current.getAmplifier() < amplifier;

        if (missing || almostExpired || weaker) {
            // visible = false  -> без частиц вокруг игрока;
            // showIcon = true  -> иконка эффекта видна в инвентаре.
            player.addEffect(new MobEffectInstance(
                    effect, Config.effectDurationTicks, amplifier, false, false, true));
        }
    }
}
