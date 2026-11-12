package net.takinashida.moonshine.client;

import net.minecraft.util.Mth;
import net.takinashida.moonshine.Config;

/**
 * Клиентский «кэш» значения опьянения.
 *
 * Зачем это нужно:
 *  - настоящие данные лежат на сервере (capability у игрока);
 *  - клиент не имеет права читать серверную capability напрямую;
 *  - поэтому сервер раз в секунду присылает пакет SyncDrunkennessPacket,
 *    а мы складываем число сюда.
 *
 * Значение отсюда используют:
 *  - DrunkennessHUD (рисует шкалу-бутылку внизу экрана);
 *  - DrunkennessBarHUD (рисует горизонтальную шкалу над хотбаром);
 *  - SwayClientHandler (шатание камеры и размытие).
 */
public final class ClientData {

    /** Последнее полученное с сервера значение опьянения. */
    private static int drunkenness;

    private ClientData() {
        // Утилитарный класс: создавать экземпляр не нужно.
    }

    /** Вызывается из сетевого пакета, когда сервер сообщает новое значение. */
    public static void setDrunkenness(int value) {
        // На всякий случай не даём отрицательному значению сломать HUD.
        drunkenness = Math.max(0, value);
    }

    /** Текущее значение для HUD и визуальных эффектов. */
    public static int getDrunkenness() {
        return drunkenness;
    }

    /**
     * Доля заполнения шкалы от 0 до 1.
     * Нужна HUD-у: половина смертельного порога -> 0.5, порог -> 1.0.
     *
     * Максимум берём из конфига (deathThreshold), чтобы шкала заполнялась
     * ровно к моменту смерти, даже если порог изменён.
     */
    public static float getFillRatio() {
        int max = Math.max(1, Config.deathThreshold);
        return Mth.clamp(drunkenness / (float) max, 0.0F, 1.0F);
    }

    /**
     * Сброс при выходе из мира.
     * Без этого на новом сервере/в новом мире шкала могла бы на секунду
     * показать старое значение до прихода первого пакета.
     */
    public static void reset() {
        drunkenness = 0;
    }
}
