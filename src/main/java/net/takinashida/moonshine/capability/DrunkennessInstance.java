package net.takinashida.moonshine.capability;

import net.takinashida.moonshine.Config;

/**
 * Данные одного игрока: сколько у него сейчас "опьянения".
 *
 * 0   — трезвый.
 * 25  — начинается лёгкое шатание камеры.
 * 50  — добавляется замедление.
 * 75  — добавляется слабость.
 * 100 — смерть (порог настраивается в конфиге).
 *
 * Конкретные числа берутся из {@link Config}, чтобы менять баланс без правки кода.
 */
public class DrunkennessInstance {

    /** Текущее значение опьянения. */
    private int value;

    /** Получить текущее значение. */
    public int getValue() {
        return value;
    }

    /**
     * Установить значение.
     * Ниже нуля опуститься нельзя, сверху ограничения нет:
     * если игрок выпьет слишком много за раз, значение уйдёт выше порога и он умрёт.
     */
    public void setValue(int value) {
        this.value = Math.max(0, value);
    }

    /** Прибавить к опьянению указанное количество (например, +30 за сидр). */
    public void add(int amount) {
        this.value = Math.max(0, this.value + amount);
    }

    /** Достигнут ли смертельный порог (значение из конфига). */
    public boolean isDeadly() {
        return value >= Config.deathThreshold;
    }
}
