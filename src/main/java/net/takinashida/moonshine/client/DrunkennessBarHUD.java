package net.takinashida.moonshine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.takinashida.moonshine.Config;

/**
 * Горизонтальная шкала опьянения над хотбаром.
 *
 * Шкала состоит из четырёх отдельных сегментов с прозрачными промежутками.
 * Каждый сегмент отвечает за свою четверть шкалы: 0..25%, 25..50% и т.д.
 * Промежутки между сегментами не входят в заполнение — при расчёте прогресса
 * они не учитываются, поэтому заполнение всегда заканчивается ровно на
 * границе или внутри нужного сегмента.
 *
 * Шкала использует те же данные, что и бутылка: значение приходит с сервера
 * пакетом SyncDrunkennessPacket и хранится в {@link ClientData}.
 *
 * Видимость регулируется настройкой hud.barVisible, а цвет заполнения —
 * настройкой hud.barColor в клиентском конфиге (или кнопкой в настройках).
 */
public final class DrunkennessBarHUD {

    /** Ширина шкалы — как у ванильного хотбара. */
    private static final int BAR_WIDTH = 182;

    /** Высота самой полоски (сделана тоньше, чем раньше). */
    private static final int BAR_HEIGHT = 6;

    /** Количество частей, на которые разбита шкала. */
    private static final int SEGMENTS = 4;

    /** Прозрачный промежуток между частями. */
    private static final int GAP = 6;

    /** Ширина одной части: (182 - 3 * 6) / 4 = 41 пиксель. */
    private static final int SEGMENT_WIDTH = (BAR_WIDTH - GAP * (SEGMENTS - 1)) / SEGMENTS;

    /** Доля шкалы, которую заполняет один сегмент. */
    private static final float SEGMENT_PORTION = 1.0F / SEGMENTS;

    /** Отступ от нижнего края экрана: шкала висит над хотбаром и XP-баром. */
    private static final int BOTTOM_MARGIN = 56;

    private static final int COLOR_BORDER = 0xFF1A0F06;
    private static final int COLOR_BACKGROUND = 0xCC2B1B0E;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private DrunkennessBarHUD() {
        // Утилитарный класс: создавать экземпляр не нужно.
    }

    /**
     * Вызывается каждый кадр из {@link ClientModEvents}.
     *
     * @param graphics помощник для рисования
     */
    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        // Нет игрока, открыт спектатор или интерфейс скрыт клавишей F1 — ничего не рисуем.
        if (player == null || player.isSpectator() || minecraft.options.hideGui) {
            return;
        }

        // Горизонтальную шкалу можно отключить в клиентском конфиге.
        if (!ClientConfig.hudBarVisible) {
            return;
        }

        int value = ClientData.getDrunkenness();
        if (value <= 0) {
            return;
        }

        int max = Math.max(1, Config.deathThreshold);
        float ratio = ClientData.getFillRatio();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = Math.max(minecraft.font.lineHeight + 4, screenHeight - BOTTOM_MARGIN);

        // Цвета читаем каждый кадр, чтобы кнопка в настройках применялась сразу.
        int fillColor = ClientConfig.getBarFillColor();
        int highlightColor = ClientConfig.getBarHighlightColor();

        // Текст «значение/максимум» рисуем над шкалой: сама полоска теперь тонкая.
        String text = value + "/" + max;
        graphics.drawCenteredString(minecraft.font, text,
                x + BAR_WIDTH / 2, y - minecraft.font.lineHeight - 2, COLOR_TEXT);

        // Рисуем четыре независимых сегмента.
        for (int i = 0; i < SEGMENTS; i++) {
            int segmentX = x + i * (SEGMENT_WIDTH + GAP);

            // Рамка и фон каждого сегмента. Промежутки между ними остаются пустыми.
            graphics.fill(segmentX - 1, y - 1,
                    segmentX + SEGMENT_WIDTH + 1, y + BAR_HEIGHT + 1, COLOR_BORDER);
            graphics.fill(segmentX, y,
                    segmentX + SEGMENT_WIDTH, y + BAR_HEIGHT, COLOR_BACKGROUND);

            /*
             * Локальное заполнение сегмента.
             *
             * Например, при общем значении 60%:
             *  - сегмент 0 (0..25%) заполнен на 100%;
             *  - сегмент 1 (25..50%) заполнен на 100%;
             *  - сегмент 2 (50..75%) заполнен на 40%;
             *  - сегмент 3 (75..100%) пуст.
             *
             * Промежутки не участвуют в расчёте, поэтому они не «съедают» прогресс.
             */
            float localRatio = Mth.clamp(
                    (ratio - i * SEGMENT_PORTION) / SEGMENT_PORTION, 0.0F, 1.0F);
            int filledWidth = Math.round(SEGMENT_WIDTH * localRatio);
            if (filledWidth > 0) {
                graphics.fill(segmentX, y,
                        segmentX + filledWidth, y + BAR_HEIGHT, fillColor);
                // Светлая полоска сверху — объём.
                graphics.fill(segmentX, y, segmentX + filledWidth, y + 1, highlightColor);
            }
        }
    }
}
