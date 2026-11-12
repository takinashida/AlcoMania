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
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

/**
 * Эффект «Спектральный» (alcomania:spectral).
 *
 * Новый эффект, сделанный по мотивам ванильного Glowing, но с другим
 * поведением для игроков:
 *  - на сервере эффект включает сущности glowing-флаг, поэтому клиент
 *    получает возможность отрисовать «спектральную» подсветку;
 *  - клиентский {@link net.takinashida.moonshine.client.SpectralEffectRenderHandler}
 *    перехватывает такую подсветку у игроков и рисует их обычным образом
 *    с лёгким бирюзовым оттенком — без «рентгена» сквозь стены.
 *
 * Ванильный Glowing при этом не меняется: спектральные стрелы и команда
 * /effect продолжают работать как раньше.
 *
 * Иконка в интерфейсе берётся у ванильного Glowing, чтобы не добавлять
 * отдельный PNG-файл.
 */
public class SpectralEffect extends MobEffect {

    /** Метка в persistent data: glowing-флаг выставлен именно этим эффектом. */
    private static final String GLOW_TAG_KEY = "alcomania_spectral_glow";

    public SpectralEffect() {
        // 0x4DE8E8 — бирюзовый «спектральный» цвет.
        super(MobEffectCategory.NEUTRAL, 0x4DE8E8);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Эффект должен проверяться каждый тик, чтобы вовремя включить/снять
        // glowing-флаг, если его сняли другим способом.
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide() || entity.isCurrentlyGlowing()) {
            return;
        }

        entity.setGlowingTag(true);
        entity.getPersistentData().putBoolean(GLOW_TAG_KEY, true);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }

        // Снимаем флаг только если его выставили именно мы. Ванильный Glowing
        // или чужая подсветка останутся нетронутыми.
        if (entity.getPersistentData().getBoolean(GLOW_TAG_KEY)) {
            entity.setGlowingTag(false);
            entity.getPersistentData().remove(GLOW_TAG_KEY);
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
                        .get(MobEffects.GLOWING);
                graphics.blit(x, y, blitOffset, 18, 18, icon);
                return true;
            }

            @Override
            public boolean renderGuiIcon(MobEffectInstance instance, Gui gui,
                                         GuiGraphics graphics,
                                         int x, int y, float z, float alpha) {
                TextureAtlasSprite icon = Minecraft.getInstance()
                        .getMobEffectTextures()
                        .get(MobEffects.GLOWING);
                graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                graphics.blit(x + 3, y + 3, 0, 18, 18, icon);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                return true;
            }
        });
    }
}

