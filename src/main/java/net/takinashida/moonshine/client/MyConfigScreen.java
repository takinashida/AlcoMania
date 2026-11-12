package net.takinashida.moonshine.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Простой экран клиентских настроек мода.
 *
 * Открывается кнопкой «Настройка» у MoonShine в списке модов
 * (Forge вызывает его через ConfigScreenHandler.ConfigScreenFactory).
 * Здесь можно независимо включать и выключать обе шкалы опьянения,
 * а также переключать цвет горизонтальной шкалы.
 *
 * Изменения применяются сразу и сохраняются в config/alcomania-client.toml.
 */
public final class MyConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("config.alcomania.title");
    private static final Component BOTTLE_LABEL = Component.translatable("config.alcomania.hudBottle");
    private static final Component BAR_LABEL = Component.translatable("config.alcomania.hudBar");
    private static final Component BAR_COLOR_LABEL = Component.translatable("config.alcomania.barColor");

    private static final int BUTTON_WIDTH = 210;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 8;

    /** Экран, на который нужно вернуться по кнопке «Готово» / Esc. */
    private final Screen parent;

    public MyConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int left = centerX - BUTTON_WIDTH / 2;
        int y = this.height / 2 - 58;

        this.addRenderableWidget(Button.builder(
                        toggleText(BOTTLE_LABEL, ClientConfig.hudBottleVisible),
                        button -> {
                            ClientConfig.setHudBottleVisible(!ClientConfig.hudBottleVisible);
                            button.setMessage(toggleText(BOTTLE_LABEL, ClientConfig.hudBottleVisible));
                        })
                .bounds(left, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(
                        toggleText(BAR_LABEL, ClientConfig.hudBarVisible),
                        button -> {
                            ClientConfig.setHudBarVisible(!ClientConfig.hudBarVisible);
                            button.setMessage(toggleText(BAR_LABEL, ClientConfig.hudBarVisible));
                        })
                .bounds(left, y + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(
                        ClientConfig.barColorText(BAR_COLOR_LABEL),
                        button -> {
                            ClientConfig.setBarColor(ClientConfig.nextBarColor());
                            button.setMessage(ClientConfig.barColorText(BAR_COLOR_LABEL));
                        })
                .bounds(left, y + (BUTTON_HEIGHT + BUTTON_GAP) * 2, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(left, y + (BUTTON_HEIGHT + BUTTON_GAP) * 3 + 12, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    /** «Название: Вкл» / «Название: Выкл». */
    private static Component toggleText(Component label, boolean enabled) {
        return Component.empty()
                .append(label)
                .append(": ")
                .append(Component.translatable(enabled
                        ? "config.alcomania.enabled"
                        : "config.alcomania.disabled"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("config.alcomania.hint"),
                this.width / 2, 34, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
