package net.takinashida.moonshine.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

/**
 * Эффект «Ночное зрение» (alcomania:night_vision).
 *
 * Пока эффект активен, на игрока на сервере поддерживается ванильный
 * Night Vision с короткой длительностью и скрытой иконкой. Поэтому:
 *  - Hardcore True Darkness видит Night Vision и отключает свою темноту;
 *  - эффект нормально синхронизируется и снимается командами;
 *  - в HUD не появляется вторая иконка.
 *
 * Ванильный Night Vision периодически обновляется, поэтому даже если его
 * снимут другой командой/модом, наш эффект вернёт его в ближайшем тике.
 */
public class NightVisionEffect extends MobEffect {

    /** Длительность ванильного Night Vision, которую держим (тики). */
    private static final int REFRESH_DURATION = 200;

    /** Обновляем, когда осталось меньше этого количества тиков. */
    private static final int REFRESH_THRESHOLD = 100;

    /**
     * Метка в persistent data: ванильный Night Vision добавлен именно нами.
     * Нужна, чтобы снять его при окончании кастомного эффекта.
     */
    public static final String SUPPORT_KEY = "alcomania_night_vision_support";

    public NightVisionEffect() {
        // 0x1F8B4C — зелёный цвет ночного зрения.
        super(MobEffectCategory.BENEFICIAL, 0x1F8B4C);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Проверяем каждый тик, чтобы поддерживать ванильный Night Vision.
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);

        if (current == null) {
            addSupportNightVision(player);
            player.getPersistentData().putBoolean(SUPPORT_KEY, true);
            return;
        }

        // Если игрок выпил настоящий длинный Night Vision, наша метка больше
        // не нужна — при окончании эффекта его нельзя снимать.
        if (current.getDuration() > REFRESH_DURATION + 40) {
            player.getPersistentData().remove(SUPPORT_KEY);
            return;
        }

        if (current.getDuration() < REFRESH_THRESHOLD) {
            addSupportNightVision(player);
        }
    }

    private static void addSupportNightVision(Player player) {
        // showIcon = false: не дублируем иконку в HUD.
        // visible = false: без частиц.
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                REFRESH_DURATION,
                0,
                false,
                false,
                false
        ));
    }

    /** Снимает поддерживаемый Night Vision, если его добавляли мы. */
    public static void removeSupportNightVision(Player player) {
        if (player.getPersistentData().getBoolean(SUPPORT_KEY)) {
            player.getPersistentData().remove(SUPPORT_KEY);
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new IClientMobEffectExtensions() {
            @Override
            public boolean renderInventoryIcon(MobEffectInstance instance,
                                               EffectRenderingInventoryScreen<?> screen,
                                               GuiGraphics graphics,
                                               int x, int y, int blitOffset) {
                TextureAtlasSprite icon = Minecraft.getInstance()
                        .getMobEffectTextures()
                        .get(MobEffects.NIGHT_VISION);
                graphics.blit(x, y, blitOffset, 18, 18, icon);
                return true;
            }

            @Override
            public boolean renderGuiIcon(MobEffectInstance instance, Gui gui,
                                         GuiGraphics graphics,
                                         int x, int y, float z, float alpha) {
                TextureAtlasSprite icon = Minecraft.getInstance()
                        .getMobEffectTextures()
                        .get(MobEffects.NIGHT_VISION);
                graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                graphics.blit(x + 3, y + 3, 0, 18, 18, icon);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                return true;
            }
        });
    }
}
