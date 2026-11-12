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
 * Текстура drunkenness_gui.png (100x61) состоит из двух половин:
 *  - x = 0..42  — полная бутылка с жидкостью (используется как «заполнение»);
 *  - x = 57..99 — пустая бутылка (рисуется как фон).
 *
 * Как рисуется уровень:
 *  1. Сначала полностью рисуется пустая бутылка (правая половина текстуры).
 *  2. Поверх неё накладывается ПОЛНАЯ бутылка (левая половина), но не целиком:
 *     волновая анимация работает как маска и оставляет только ту часть
 *     полной бутылки, которая лежит ниже волнистой линии поверхности.
 *     Всё, что выше линии, остаётся пустой бутылкой.
 *
 * Волновая анимация (маска):
 *  - полная бутылка рисуется не одним прямоугольником, а 43 колонками по
 *    1 пикселю: каждая колонка — это её нижняя часть до линии волны;
 *  - высота каждой колонки смещается по синусоиде, поэтому кромка жидкости
 *    колышется волной;
 *  - фаза волны зависит от времени и от X-координаты колонки, поэтому волна
 *    «бежит» слева направо, а не просто пульсирует;
 *  - две синусоиды с разными частотами дают неровную, живую поверхность.
 *
 * Значение опьянения берётся из {@link ClientData}: сервер присылает его
 * пакетом SyncDrunkennessPacket, читать серверную capability на клиенте нельзя.
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

    /** Размер одной бутылки в текстуре. */
    private static final int BOTTLE_WIDTH = 43;
    private static final int BOTTLE_HEIGHT = 61;

    /** Смещение пустой бутылки по X внутри текстуры (левая часть — полная, x=0). */
    private static final int EMPTY_U = 57;

    /** Полный размер текстуры — нужен, чтобы blit не «поплыл». */
    private static final int TEXTURE_WIDTH = 100;
    private static final int TEXTURE_HEIGHT = 61;

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
        // Привязываем бутылку к левому нижнему углу: так она не мешает
        // хотбару и не «плавает» при смене разрешения.
        int posY = Math.max(4, minecraft.getWindow().getGuiScaledHeight() - BOTTLE_HEIGHT - BOTTOM_MARGIN);

        // 9-аргументный blit: (текстура, x, y, u, v, ширина, высота, ширина_текстуры, высота_текстуры).
        graphics.blit(TEXTURE, POS_X, posY-10, EMPTY_U, 0,
                BOTTLE_WIDTH, BOTTLE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // ---------- 2. Жидкость ----------
        int baseHeight = Math.round(BOTTLE_HEIGHT * ratio);
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

        /*
         * Рисуем 43 вертикальные колонки по 1 пикселю.
         *
         * Маска — волнистая линия поверхности: для каждой колонки берём из
         * ПОЛНОЙ бутылки (левая половина текстуры, x = 0..42) только ту часть,
         * что лежит НИЖЕ линии волны. Верхняя часть полной бутылки не
         * рисуется, поэтому сквозь неё видна пустая бутылка-фон.
         *
         * Источник (u/v) и приёмник (x/y) синхронизированы так, чтобы низ
         * колонки всегда совпадал с дном бутылки, а верх гулял волной.
         */
        for (int column = 0; column < BOTTLE_WIDTH; column++) {
            // Фаза бегущей волны: время + сдвиг по X.
            double phase = time + (column / waveLength) * TAU;

            // Две синусоиды: основная волна + небольшой «перекос» для живости.
            double wave = Math.sin(phase) * 0.7D + Math.sin(phase * 1.7D + 1.3D) * 0.3D;

            int columnHeight = baseHeight + (int) Math.round(wave * amplitude);
            columnHeight = Mth.clamp(columnHeight, 1, BOTTLE_HEIGHT);

            // Верхняя строка источника и координата Y приёмника совпадают:
            // низ колонки остаётся на месте, двигается только поверхность.
            int sourceV = BOTTLE_HEIGHT - columnHeight;

            // uOffset = column: каждый столбец маски берёт СВОЙ столбец
            // текстуры полной бутылки, поэтому рисунок не растягивается.
            graphics.blit(TEXTURE, POS_X + column, posY -10 + sourceV,
                    1, columnHeight,
                    (float) column, (float) sourceV,
                    1, columnHeight,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }
}
