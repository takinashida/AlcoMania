package net.takinashida.moonshine.client;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.takinashida.moonshine.MoonShine;

/**
 * Client config (config/alcomania-client.toml).
 *
 * Contains settings that affect only the HUD and are not synchronized
 * with the server:
 *  - whether to show the bottle gauge;
 *  - whether to show the horizontal bar;
 *  - fill color of the horizontal bar.
 *
 * Values are cached in static fields so the HUD does not query the config
 * every frame. They can be changed in the TOML file or via the settings
 * screen {@link MyConfigScreen} (the "Config" button for this mod).
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientConfig {

    /**
     * Palette cycled by the color button on the settings screen.
     * Values are plain RGB without alpha (0xRRGGBB).
     */
    private static final int[] BAR_COLOR_PALETTE = {
            0xD08A2C, // gold (default)
            0xD0402C, // red
            0x4CAF50, // green
            0x4A90D9, // blue
            0x9C5FD0, // purple
            0x2CC7C7, // cyan
            0xE8E8E8  // white
    };

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HUD_BOTTLE_VISIBLE = BUILDER
            .comment("Show the bottle-shaped drunkenness gauge in the bottom-left corner.")
            .define("hud.bottleVisible", true);

    private static final ForgeConfigSpec.BooleanValue HUD_BAR_VISIBLE = BUILDER
            .comment("Show the horizontal drunkenness bar above the hotbar.")
            .define("hud.barVisible", true);

    private static final ForgeConfigSpec.IntValue HUD_BAR_COLOR = BUILDER
            .comment("Horizontal bar fill color in RGB format (0xRRGGBB).",
                    "Examples: 0xD08A2C = gold, 0xD0402C = red.",
                    "The \"Bar color\" button on the settings screen cycles preset colors.")
            .defineInRange("hud.barColor", 0xD08A2C, 0x000000, 0xFFFFFF);

    /** Final client config spec. */
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // Cached values (defaults used before the config is loaded).
    public static boolean hudBottleVisible = true;
    public static boolean hudBarVisible = true;
    public static int barColor = 0xD08A2C;

    private ClientConfig() {
    }

    /** Called by Forge when the client config is loaded or reloaded. */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // The MOD bus also receives common config events; react only to our spec.
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        hudBottleVisible = HUD_BOTTLE_VISIBLE.get();
        hudBarVisible = HUD_BAR_VISIBLE.get();
        barColor = HUD_BAR_COLOR.get() & 0xFFFFFF;
    }

    /** Toggles the bottle gauge and saves the choice to TOML immediately. */
    public static void setHudBottleVisible(boolean visible) {
        hudBottleVisible = visible;
        HUD_BOTTLE_VISIBLE.set(visible);
        HUD_BOTTLE_VISIBLE.save();
    }

    /** Toggles the horizontal bar and saves the choice to TOML immediately. */
    public static void setHudBarVisible(boolean visible) {
        hudBarVisible = visible;
        HUD_BAR_VISIBLE.set(visible);
        HUD_BAR_VISIBLE.save();
    }

    /** Changes the horizontal bar color and saves the choice to TOML immediately. */
    public static void setBarColor(int rgb) {
        barColor = rgb & 0xFFFFFF;
        HUD_BAR_COLOR.set(barColor);
        HUD_BAR_COLOR.save();
    }

    /** Next color from the palette, used by the settings screen button. */
    public static int nextBarColor() {
        for (int i = 0; i < BAR_COLOR_PALETTE.length; i++) {
            if (BAR_COLOR_PALETTE[i] == barColor) {
                return BAR_COLOR_PALETTE[(i + 1) % BAR_COLOR_PALETTE.length];
            }
        }
        return BAR_COLOR_PALETTE[0];
    }

    /** Opaque fill color of the bar. */
    public static int getBarFillColor() {
        return 0xFF000000 | barColor;
    }

    /** Lighter shade of the fill color, used for the top highlight. */
    public static int getBarHighlightColor() {
        int red = lighten((barColor >> 16) & 0xFF);
        int green = lighten((barColor >> 8) & 0xFF);
        int blue = lighten(barColor & 0xFF);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int lighten(int channel) {
        return channel + (255 - channel) * 45 / 100;
    }

    /** Button label: "Bar color: #RRGGBB". */
    public static Component barColorText(Component label) {
        Component value = Component.literal(String.format("#%06X", barColor))
                .withStyle(style -> style.withColor(barColor));
        return Component.empty().append(label).append(": ").append(value);
    }
}
