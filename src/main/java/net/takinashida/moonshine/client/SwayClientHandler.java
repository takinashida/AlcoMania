package net.takinashida.moonshine.client;

import net.minecraft.client.Minecraft;
// import net.minecraft.client.renderer.PostChain;      // БЛЮР ОТКЛЮЧЁН (нужен только для проверки текущего эффекта)
// import net.minecraft.resources.ResourceLocation;    // БЛЮР ОТКЛЮЧЁН (нужен только для пути к шейдеру)
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
// import net.minecraftforge.event.TickEvent;           // БЛЮР ОТКЛЮЧЁН (нужен только для onClientTick)
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.MoonShine;

/**
 * Клиентская «пьяная» визуализация.
 *
 * Сейчас здесь работает только ОДНА вещь:
 *  - плавное покачивание камеры и реального поворота игрока при опьянении 25+.
 *
 * Поворот меняется у самого игрока (yRot/xRot), поэтому снаряды летят
 * ровно туда, куда смотрит прицел, а не мимо него.
 *
 * Размытие (blur) ВРЕМЕННО ОТКЛЮЧЕНО — весь связанный код закомментирован ниже.
 * Причина: эффект не работал как надо. Файлы шейдеров при этом никуда не удалены,
 * поэтому вернуть размытие можно, раскомментировав блоки, помеченные «БЛЮР ОТКЛЮЧЁН».
 *
 * Класс работает только на клиенте (Dist.CLIENT), на сервере его код не выполняется.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, value = Dist.CLIENT)
public final class SwayClientHandler {

    /*
     * Все числа эффекта берутся из конфига (Config):
     *  - Config.swayThreshold        — начало первого сегмента шатания;
     *  - Config.swayTier2Threshold   — начало второго сегмента;
     *  - Config.swayTier3Threshold   — начало третьего (максимального) сегмента;
     *  - Config.swayTier1Strength    — сила в первом сегменте;
     *  - Config.swayTier2Strength    — сила во втором сегменте;
     *  - Config.swayTier3Strength    — сила в третьем сегменте;
     *  - Config.swayYawDegrees       — максимальная амплитуда влево-вправо;
     *  - Config.swayPitchDegrees     — максимальная амплитуда вверх-вниз;
     *  - Config.swayRollDegrees      — максимальный наклон горизонта.
     *
     * Сила шатания НЕ растёт с каждой единицей опьянения: она ступенчатая.
     * Внутри сегмента сила постоянна, при переходе на следующий — увеличивается.
     */

    /**
     * Текущая сглаженная сила эффекта (0..1).
     *
     * Сервер присылает новое значение опьянения раз в секунду, и если брать его напрямую,
     * амплитуда будет дёргаться рывками. Поэтому мы плавно «догоняем» цель каждый кадр.
     */
    private static float smoothedStrength = 0.0F;

    /** Время прошлого кадра в миллисекундах — нужно для сглаживания, не зависящего от FPS. */
    private static long lastFrameMillis = 0L;

    /**
     * Смещения, которые уже были записаны в реальный поворот игрока.
     * Нужны, чтобы покачивание не накапливалось кадр за кадром.
     */
    private static float appliedYawOffset = 0.0F;
    private static float appliedPitchOffset = 0.0F;

    /** Игрок, к которому относятся текущие применённые смещения. */
    private static Player swayedPlayer;

    /*
     * =========================================================================
     *  БЛЮР ОТКЛЮЧЁН
     * =========================================================================
     *  Ниже закомментированы поля, которые нужны были только для размытия.
     *
     *  private static final ResourceLocation BLUR_EFFECT =
     *          new ResourceLocation(MoonShine.MOD_ID, "shaders/post/blur.json");
     *
     *  private static boolean blurUnavailable = false;
     * =========================================================================
     */

    private SwayClientHandler() {
    }

    /**
     * Покачивание камеры.
     *
     * Forge вызывает это событие каждый кадр перед отрисовкой мира.
     * event.getYaw()/getPitch()/getRoll() — углы камеры в градусах.
     * Мы прибавляем к ним небольшое смещение и записываем обратно.
     *
     * Амплитуда зависит от сегмента опьянения (а не от каждой единицы):
     *   25..49  — слабое шатание;
     *   50..74  — среднее;
     *   75+     — максимальное.
     * Переходы сглаживаются, поэтому камера не дёргается на границах.
     */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) {
            clearAppliedOffsets();
            return;
        }

        // Если клиентский игрок сменился (респавн, другой мир), старые
        // смещения к нему уже не относятся.
        if (player != swayedPlayer) {
            swayedPlayer = player;
            clearAppliedOffsets();
        }

        // Нет игрока, открыт инвентарь/меню или игрок в спектаторе —
        // возвращаем реальный поворот к обычному состоянию.
        if (minecraft.screen != null || player.isSpectator()) {
            restorePlayerRotation(player);
            return;
        }

        /*
         * Не шатаем реальный поворот во время добычи блока.
         * Иначе прицел уезжает с блока, и клиент/сервер постоянно
         * сбрасывают прогресс ломания. Это особенно мешает эффектам
         * вроде ликёра, который должен ускорять добычу.
         */
        if (minecraft.gameMode != null && minecraft.gameMode.isDestroying()) {
            restorePlayerRotation(player);
            return;
        }

        // Целевая сила эффекта по текущему опьянению (0..1).
        float targetStrength = calculateTargetStrength(ClientData.getDrunkenness());

        // ---- Плавное сглаживание силы, чтобы не было «телепортации» камеры ----
        long now = System.currentTimeMillis();
        // Ограничиваем шаг времени, чтобы после лага/паузы камера не прыгнула.
        float deltaSeconds = lastFrameMillis == 0L
                ? 0.0F
                : Math.min((now - lastFrameMillis) / 1000.0F, 0.1F);
        lastFrameMillis = now;

        // Экспоненциальное сглаживание: чем больше deltaSeconds, тем быстрее догоняем цель.
        // Коэффициент 3.0 означает, что примерно за треть секунды значение подтягивается к цели.
        float lerpFactor = 1.0F - (float) Math.exp(-3.0F * deltaSeconds);
        smoothedStrength += (targetStrength - smoothedStrength) * lerpFactor;

        // Совсем маленькую силу не применяем — экономим вычисления и избегаем дрожания.
        if (smoothedStrength < 0.001F) {
            smoothedStrength = 0.0F;
            restorePlayerRotation(player);
            return;
        }

        // Игровое время + частичный тик: движение остаётся плавным между тиками сервера.
        double time = (player.level().getGameTime() + event.getPartialTick()) * 0.12D;

        /*
         * Комбинация двух синусоид с разной частотой.
         * Одна синусоида выглядит слишком «механически», две — живее.
         */
        double yawWave = Math.sin(time) * 1.0D + Math.sin(time * 2.1D) * 0.3D;
        double pitchWave = Math.cos(time * 1.3D) * 1.0D + Math.cos(time * 3.1D) * 0.3D;
        double rollWave = Math.sin(time * 0.8D);

        // Переводим волны (-1..1) в градусы и умножаем на сглаженную силу.
        // Амплитуды читаем из конфига при каждом кадре — правки применяются сразу.
        float yawOffset = (float) yawWave * (float) Config.swayYawDegrees * smoothedStrength / 2;
        float pitchOffset = (float) pitchWave * (float) Config.swayPitchDegrees * smoothedStrength / 2;
        float rollOffset = (float) rollWave * (float) Config.swayRollDegrees * smoothedStrength / 2;

        /*
         * Меняем РЕАЛЬНЫЙ поворот игрока, а не только углы камеры.
         *
         * Снаряды (стрелы, трезубцы и т.п.) берут направление из yRot/xRot
         * игрока, поэтому без этого они летели бы мимо прицела во время шатания.
         *
         * Чтобы покачивание не накапливалось, из реального поворота игрока
         * сначала вычитается смещение, применённое в прошлом кадре, затем
         * добавляется новое. yRotO/xRotO тоже синхронизируем — иначе камера
         * интерполировала бы между старым и новым поворотом и прицел «плыл» бы.
         */
        // Базу берём из реального поворота игрока: так мы не теряем движение
        // мыши и не зависим от интерполяции камеры между тиками.
        float baseYaw = player.getYRot() - appliedYawOffset;
        float basePitch = player.getXRot() - appliedPitchOffset;

        float targetYaw = baseYaw + yawOffset;
        float targetPitch = Mth.clamp(basePitch + pitchOffset, -90.0F, 90.0F);

        player.setYRot(targetYaw);
        player.yRotO = targetYaw;
        player.setXRot(targetPitch);
        player.xRotO = targetPitch;

        event.setYaw(targetYaw);
        event.setPitch(targetPitch);
        event.setRoll(event.getRoll() + rollOffset);

        appliedYawOffset = yawOffset;
        appliedPitchOffset = pitchOffset;
    }

    /** Снимает с игрока последнее применённое смещение покачивания. */
    private static void restorePlayerRotation(Player player) {
        if (player != null && (appliedYawOffset != 0.0F || appliedPitchOffset != 0.0F)) {
            float restoredYaw = player.getYRot() - appliedYawOffset;
            float restoredPitch = Mth.clamp(player.getXRot() - appliedPitchOffset, -90.0F, 90.0F);

            player.setYRot(restoredYaw);
            player.yRotO = restoredYaw;
            player.setXRot(restoredPitch);
            player.xRotO = restoredPitch;
        }

        clearAppliedOffsets();
    }

    private static void clearAppliedOffsets() {
        appliedYawOffset = 0.0F;
        appliedPitchOffset = 0.0F;
    }

    /**
     * Считает, насколько сильно должно шатать при данном опьянении.
     *
     * Сила ступенчатая — зависит только от сегмента, а не от каждой единицы:
     *   < swayThreshold          — 0 (трезв);
     *   swayThreshold..tier2-1   — swayTier1Strength;
     *   swayTier2Threshold..tier3-1 — swayTier2Strength;
     *   >= swayTier3Threshold    — swayTier3Strength.
     *
     * Пороги проверяются от старшего к младшему, поэтому даже если в конфиге
     * их случайно перепутают местами, игрок не останется без шатания.
     */
    private static float calculateTargetStrength(int drunkenness) {
        if (drunkenness >= Config.swayTier3Threshold) {
            return (float) Config.swayTier3Strength;
        }
        if (drunkenness >= Config.swayTier2Threshold) {
            return (float) Config.swayTier2Strength;
        }
        if (drunkenness >= Config.swayThreshold) {
            return (float) Config.swayTier1Strength;
        }
        return 0.0F;
    }

    /*
     * =========================================================================
     *  БЛЮР ОТКЛЮЧЁН
     * =========================================================================
     *  Раньше этот метод включал и выключал пост-эффект размытия.
     *  Сейчас он закомментирован целиком.
     *
     *  Чтобы вернуть размытие:
     *   1. Раскомментируйте импорты PostChain, ResourceLocation и TickEvent.
     *   2. Раскомментируйте поля BLUR_EFFECT и blurUnavailable выше.
     *   3. Раскомментируйте этот метод.
     *   4. Раскомментируйте блок с blur в onLoggingOut и строку
     *      blurUnavailable = false в resetSmoothing.
     *   5. Раскомментируйте метод isOurBlur в самом низу.
     *   6. Убедитесь, что на месте файлы:
     *      assets/moonshine/shaders/post/blur.json
     *      assets/moonshine/shaders/program/blur.json
     *      assets/moonshine/shaders/program/blur.vsh
     *      assets/moonshine/shaders/program/blur.fsh
     *
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        boolean shouldBlur = !player.isSpectator()
                && ClientData.getDrunkenness() >= Config.swayThreshold;
        PostChain currentEffect = minecraft.gameRenderer.currentEffect();

        if (shouldBlur) {
            if (currentEffect == null && !blurUnavailable) {
                minecraft.gameRenderer.loadEffect(BLUR_EFFECT);

                if (minecraft.gameRenderer.currentEffect() == null) {
                    blurUnavailable = true;
                    MoonShine.LOGGER.warn("Failed to load blur shader: {}", BLUR_EFFECT);
                }
            }
        } else if (isOurBlur(currentEffect)) {
            minecraft.gameRenderer.checkEntityPostEffect(player);
        }
    }
     * =========================================================================
     */

    /**
     * Игрок вышел из мира — сбрасываем сглаживание и клиентские данные,
     * чтобы в новом мире камера не начинала движение с половинчатой силы,
     * а HUD не показывал старое значение.
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        resetSmoothing();

        // Сбрасываем значение для HUD, чтобы оно не «переехало» на другой сервер.
        ClientData.reset();

        // ---- БЛЮР ОТКЛЮЧЁН ----
        // Если наш blur остался висеть — снимаем его, чтобы он не попал в главное меню.
        // Minecraft minecraft = Minecraft.getInstance();
        // if (isOurBlur(minecraft.gameRenderer.currentEffect())) {
        //     minecraft.gameRenderer.checkEntityPostEffect(event.getPlayer());
        // }
    }

    /** Игрок только зашёл в мир — тоже начинаем с нуля. */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        resetSmoothing();
        ClientData.reset();
    }

    /** Респавн/смена измерения: новый игрок мог унаследовать поворот со смещением. */
    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        restorePlayerRotation(event.getPlayer());
        resetSmoothing();
    }

    /** Обнуляет сглаженную силу и запоминает, что «прошлого кадра» ещё не было. */
    private static void resetSmoothing() {
        smoothedStrength = 0.0F;
        lastFrameMillis = 0L;
        swayedPlayer = null;
        clearAppliedOffsets();
        // ---- БЛЮР ОТКЛЮЧЁН ----
        // blurUnavailable = false;
    }

    /*
     * =========================================================================
     *  БЛЮР ОТКЛЮЧЁН
     * =========================================================================
     *
     * Проверяла, что текущий пост-эффект — именно наше размытие.
     *
     * private static boolean isOurBlur(PostChain chain) {
     *     return chain != null && BLUR_EFFECT.toString().equals(chain.getName());
     * }
     * =========================================================================
     */
}
