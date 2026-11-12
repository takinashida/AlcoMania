package net.takinashida.moonshine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.MoonShine;

/**
 * HUD-индикатор опьянения в виде бутылки.
 *
 * Текстура drunkenness_gui.png (246x150) содержит два спрайта:
 *  - пустая бутылка: x = 141..245, y = 0..149;
 *  - жидкость для полной бутылки: x = 8..95, y = 35..135.
 *
 * Жидкость накладывается поверх пустой бутылки со смещением:
 *  - по X — по центру стеклянной бутылки;
 *  - по Y — так, чтобы горлышко жидкости совпало с горлышком бутылки,
 *    а внизу осталось место для стеклянного дна.
 *
 * Волновая анимация работает как маска: для каждой колонки жидкости
 * рисуется только нижняя часть спрайта до волнистой линии поверхности.
 */
public final class DrunkennessHUD {

    /** Путь к текстуре бутылки. */
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MoonShine.MOD_ID, "textures/gui/drunkenness_gui.png");

    /** Отступ бутылки от левого края экрана (в GUI-пикселях). */
    private static final int POS_X = 10;

    /**
     * Отступ от нижнего края экрана. Бутылка приподнята над нижним краем,
     * чтобы не пересекаться с полосой брони/здоровья и другими HUD-модами.
     */
    private static final int BOTTOM_MARGIN = 22;

    /** Полный размер текстуры. */
    private static final int TEXTURE_WIDTH = 246;
    private static final int TEXTURE_HEIGHT = 150;

    // ---------- Пустая бутылка (фон) ----------
    private static final int EMPTY_U = 141;
    private static final int EMPTY_V = 0;
    private static final int BOTTLE_WIDTH = 105;
    private static final int BOTTLE_HEIGHT = 150;

    // ---------- Спрайт жидкости ----------
    private static final int LIQUID_U = 8;
    private static final int LIQUID_V = 35;
    private static final int LIQUID_WIDTH = 88;
    private static final int LIQUID_HEIGHT = 101;

    /** Смещение жидкости внутри бутылки: центрируем по ширине. */
    private static final int LIQUID_X_OFFSET = (BOTTLE_WIDTH - LIQUID_WIDTH + 1) / 2;

    /** Смещение жидкости по высоте: совпадает с горлышком пустой бутылки. */
    private static final int LIQUID_Y_OFFSET = 35;

    /**
     * Масштаб отображения HUD. Сама текстура не меняется:
     * 0.42 даёт примерно прежний размер бутылки (~44x63 вместо 105x150).
     */
    private static final float DISPLAY_SCALE = 0.42F;

    private static final int DISPLAY_BOTTLE_WIDTH = Math.round(BOTTLE_WIDTH * DISPLAY_SCALE);
    private static final int DISPLAY_BOTTLE_HEIGHT = Math.round(BOTTLE_HEIGHT * DISPLAY_SCALE);
    private static final int DISPLAY_LIQUID_WIDTH = Math.round(LIQUID_WIDTH * DISPLAY_SCALE);
    private static final int DISPLAY_LIQUID_HEIGHT = Math.round(LIQUID_HEIGHT * DISPLAY_SCALE);
    private static final int DISPLAY_LIQUID_X_OFFSET = Math.round(LIQUID_X_OFFSET * DISPLAY_SCALE);
    private static final int DISPLAY_LIQUID_Y_OFFSET = Math.round(LIQUID_Y_OFFSET * DISPLAY_SCALE);

    /** Полный оборот волны в радианах. */
    private static final float TAU = (float) (Math.PI * 2.0D);

    /**
     * Скорость «догоняния» отображаемого уровня за реальным (1/сек).
     * Сервер присылает значение раз в секунду, поэтому без сглаживания
     * жидкость прыгала бы целыми пикселями. 8.0 — уровень доезжает
     * примерно за 0.3 секунды.
     */
    private static final float FILL_SMOOTHING_SPEED = 8.0F;

    /** Сглаженное значение, которое реально рисуется (в единицах опьянения). */
    private static float displayedValue;

    private DrunkennessHUD() {
        // Утилитарный класс: создавать экземпляр не нужно.
    }

    /**
     * Вызывается каждый кадр из {@link ClientModEvents}.
     *
     * @param graphics    помощник для рисования
     * @param partialTick дробная часть текущего тика (0..1) — с ней волна
     *                    движется плавно, а не рывками 20 раз в секунду
     */
    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        // Нет игрока, открыт спектатор или интерфейс скрыт клавишей F1 — ничего не рисуем.
        if (player == null || player.isSpectator() || minecraft.options.hideGui) {
            return;
        }

        // Шкалу-бутылку можно отключить в клиентском конфиге.
        if (!ClientConfig.hudBottleVisible) {
            return;
        }

        int value = ClientData.getDrunkenness();

        /*
         * Сглаживание уровня.
         *
         * Пакет синхронизации приходит примерно раз в секунду, поэтому без
         * сглаживания жидкость в бутылке прыгала бы вверх/вниз целыми
         * группами пикселей. Здесь отображаемое значение экспоненциально
         * подтягивается к реальному; dt — время кадра в тиках, поэтому
         * скорость не зависит от FPS.
         */
        float dt = Math.min(1.0F, minecraft.getDeltaFrameTime());
        float smoothing = 1.0F - (float) Math.exp(-FILL_SMOOTHING_SPEED * dt);
        displayedValue += (value - displayedValue) * smoothing;

        // Если уровень упал до нуля, досматриваем, как жидкость «доливается» вниз.
        if (value <= 0 && displayedValue < 0.5F) {
            displayedValue = 0.0F;
            return;
        }

        // Максимум шкалы — смертельный порог из конфига.
        int max = Math.max(1, Config.deathThreshold);
        float ratio = Mth.clamp(displayedValue / max, 0.0F, 1.0F);

        // ---------- 1. Пустая бутылка (фон) ----------
        int posY = Math.max(4,
                minecraft.getWindow().getGuiScaledHeight() - DISPLAY_BOTTLE_HEIGHT - BOTTOM_MARGIN);

        graphics.blit(TEXTURE, POS_X, posY,
                DISPLAY_BOTTLE_WIDTH, DISPLAY_BOTTLE_HEIGHT,
                (float) EMPTY_U, (float) EMPTY_V,
                BOTTLE_WIDTH, BOTTLE_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // ---------- 2. Жидкость ----------
        int baseHeight = Math.round(DISPLAY_LIQUID_HEIGHT * ratio);
        if (baseHeight <= 0) {
            return;
        }

        /*
         * Амплитуду ограничиваем четвертью высоты жидкости: на дне бутылки
         * жидкости мало, и волна в 2 пикселя выглядела бы как дырка в бутылке.
         */
        float amplitude = Math.min((float) Config.hudWaveAmplitudePixels, baseHeight * 0.25F);

        float waveSpeed = (float) Config.hudWaveSpeed;
        float waveLength = Math.max(1.0F, (float) Config.hudWaveLengthPixels);

        // Игровое время + частичный тик: анимация не зависит от FPS.
        double time = (player.tickCount + partialTick) * waveSpeed;

        int liquidX = POS_X + DISPLAY_LIQUID_X_OFFSET;
        int liquidY = posY + DISPLAY_LIQUID_Y_OFFSET;

        /*
         * Рисуем колонки уже в уменьшенном размере.
         *
         * Маска — волнистая линия поверхности: для каждой колонки берём из
         * спрайта жидкости только ту часть, что лежит НИЖЕ линии волны.
         * Верхняя часть спрайта не рисуется, поэтому сквозь неё видна
         * пустая бутылка-фон.
         */
        for (int column = 0; column < DISPLAY_LIQUID_WIDTH; column++) {
            // Фаза бегущей волны: время + сдвиг по X.
            double phase = time + (column / waveLength) * TAU;

            // Две синусоиды: основная волна + небольшой «перекос» для живости.
            double wave = Math.sin(phase) * 0.7D + Math.sin(phase * 1.7D + 1.3D) * 0.3D;

            int columnHeight = baseHeight + (int) Math.round(wave * amplitude);
            columnHeight = Mth.clamp(columnHeight, 1, DISPLAY_LIQUID_HEIGHT);

            // Переводим уменьшенную колонку обратно в координаты текстуры.
            int sourceColumn = Math.min(LIQUID_WIDTH - 1,
                    (int) Math.round(column / DISPLAY_SCALE));
            int sourceHeight = Math.max(1, Math.min(LIQUID_HEIGHT,
                    (int) Math.round(columnHeight / DISPLAY_SCALE)));

            // Низ колонки остаётся на месте, двигается только поверхность.
            int sourceV = LIQUID_V + LIQUID_HEIGHT - sourceHeight;
            int destY = liquidY + DISPLAY_LIQUID_HEIGHT - columnHeight;

            graphics.blit(TEXTURE, liquidX + column, destY,
                    1, columnHeight,
                    (float) (LIQUID_U + sourceColumn), (float) sourceV,
                    1, sourceHeight,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }
}
