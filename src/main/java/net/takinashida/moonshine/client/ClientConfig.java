package net.takinashida.moonshine.client;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.takinashida.moonshine.MoonShine;

/**
 * Клиентский конфиг мода (файл config/alcomania-client.toml).
 *
 * Здесь находятся настройки, которые влияют только на внешний вид HUD
 * и не должны синхронизироваться с сервером:
 *  - показывать ли шкалу-бутылку;
 *  - показывать ли горизонтальную шкалу;
 *  - цвет заполнения горизонтальной шкалы.
 *
 * Значения кэшируются в static-полях, чтобы HUD не дёргал конфиг каждый кадр.
 * Менять их можно как вручную в TOML-файле, так и через экран настроек
 * {@link MyConfigScreen} (кнопка «Настройка» у мода в списке модов).
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientConfig {

    /**
     * Палитра, по которой ходит кнопка цвета в экране настроек.
     * Значения — обычные RGB без альфы (0xRRGGBB).
     */
    private static final int[] BAR_COLOR_PALETTE = {
            0xD08A2C, // золотой (по умолчанию)
            0xD0402C, // красный
            0x4CAF50, // зелёный
            0x4A90D9, // синий
            0x9C5FD0, // фиолетовый
            0x2CC7C7, // бирюзовый
            0xE8E8E8  // белый
    };

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HUD_BOTTLE_VISIBLE = BUILDER
            .comment("Показывать шкалу опьянения в виде бутылки в левом нижнем углу.")
            .define("hud.bottleVisible", true);

    private static final ForgeConfigSpec.BooleanValue HUD_BAR_VISIBLE = BUILDER
            .comment("Показывать горизонтальную шкалу опьянения над хотбаром.")
            .define("hud.barVisible", true);

    private static final ForgeConfigSpec.IntValue HUD_BAR_COLOR = BUILDER
            .comment("Цвет заполнения горизонтальной шкалы в формате RGB (0xRRGGBB).",
                    "Например: 0xD08A2C — золотой, 0xD0402C — красный.",
                    "Кнопка «Цвет шкалы» в экране настроек переключает готовые цвета.")
            .defineInRange("hud.barColor", 0xD08A2C, 0x000000, 0xFFFFFF);

    /** Итоговое описание клиентского конфига. */
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // Кэш значений (значения по умолчанию — до загрузки конфига).
    public static boolean hudBottleVisible = true;
    public static boolean hudBarVisible = true;
    public static int barColor = 0xD08A2C;

    private ClientConfig() {
    }

    /** Вызывается Forge при загрузке/перезагрузке клиентского конфига. */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // На MOD-шине приходят события и от общего конфига — реагируем только на свой.
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        hudBottleVisible = HUD_BOTTLE_VISIBLE.get();
        hudBarVisible = HUD_BAR_VISIBLE.get();
        barColor = HUD_BAR_COLOR.get() & 0xFFFFFF;
    }

    /** Включает/выключает шкалу-бутылку и сразу сохраняет выбор в TOML. */
    public static void setHudBottleVisible(boolean visible) {
        hudBottleVisible = visible;
        HUD_BOTTLE_VISIBLE.set(visible);
        HUD_BOTTLE_VISIBLE.save();
    }

    /** Включает/выключает горизонтальную шкалу и сразу сохраняет выбор в TOML. */
    public static void setHudBarVisible(boolean visible) {
        hudBarVisible = visible;
        HUD_BAR_VISIBLE.set(visible);
        HUD_BAR_VISIBLE.save();
    }

    /** Меняет цвет горизонтальной шкалы и сразу сохраняет выбор в TOML. */
    public static void setBarColor(int rgb) {
        barColor = rgb & 0xFFFFFF;
        HUD_BAR_COLOR.set(barColor);
        HUD_BAR_COLOR.save();
    }

    /** Следующий цвет из палитры — для кнопки в экране настроек. */
    public static int nextBarColor() {
        for (int i = 0; i < BAR_COLOR_PALETTE.length; i++) {
            if (BAR_COLOR_PALETTE[i] == barColor) {
                return BAR_COLOR_PALETTE[(i + 1) % BAR_COLOR_PALETTE.length];
            }
        }
        return BAR_COLOR_PALETTE[0];
    }

    /** Цвет заполнения шкалы с непрозрачной альфой. */
    public static int getBarFillColor() {
        return 0xFF000000 | barColor;
    }

    /** Более светлый оттенок цвета заполнения — для блика сверху. */
    public static int getBarHighlightColor() {
        int red = lighten((barColor >> 16) & 0xFF);
        int green = lighten((barColor >> 8) & 0xFF);
        int blue = lighten(barColor & 0xFF);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int lighten(int channel) {
        return channel + (255 - channel) * 45 / 100;
    }

    /** Текст кнопки цвета: «Цвет шкалы: #RRGGBB». */
    public static Component barColorText(Component label) {
        Component value = Component.literal(String.format("#%06X", barColor))
                .withStyle(style -> style.withColor(barColor));
        return Component.empty().append(label).append(": ").append(value);
    }
}
