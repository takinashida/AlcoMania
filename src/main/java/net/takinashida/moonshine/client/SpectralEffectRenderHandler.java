package net.takinashida.moonshine.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.MoonShine;

/**
 * Клиентская отрисовка эффекта «Спектральный» (alcomania:spectral).
 *
 * Эффект специально не трогает ванильный Glowing: обычные спектральные
 * стрелки и /effect glowing работают как раньше.
 *
 * Серверный SpectralEffect включает у сущности glowing-флаг, поэтому клиент
 * рисует её через OutlineBufferSource. Здесь мы перехватываем только игроков
 * с нашим эффектом и вместо x-ray-контура рисуем их в основной буфер
 * с бирюзовым оттенком. Такой игрок виден только в прямой видимости,
 * а не сквозь стены.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, value = Dist.CLIENT)
public final class SpectralEffectRenderHandler {

    /** Цвет тонировки: лёгкий бирюзовый оттенок. */
    private static final int TINT_RED = 120;
    private static final int TINT_GREEN = 225;
    private static final int TINT_BLUE = 255;

    /** Защита от повторного входа при ручном вызове рендера. */
    private static boolean renderingNormally = false;

    private SpectralEffectRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (renderingNormally) {
            return;
        }

        Player player = event.getEntity();
        if (!(player instanceof AbstractClientPlayer clientPlayer)) {
            return;
        }

        // Обрабатываем только НАШ эффект, не ванильный Glowing.
        if (!player.hasEffect(ModEffects.SPECTRAL.get())) {
            return;
        }

        // Нас интересует только контурный проход, в который ваниль отправляет
        // светящихся сущностей.
        if (!(event.getMultiBufferSource() instanceof OutlineBufferSource)) {
            return;
        }

        event.setCanceled(true);
        renderingNormally = true;
        try {
            float partialTick = event.getPartialTick();
            float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());

            MultiBufferSource normalBuffers = Minecraft.getInstance()
                    .renderBuffers()
                    .bufferSource();

            // Оборачиваем буфер, чтобы подкрасить вершины игрока, но не трогать
            // остальных сущностей и не менять глобальный шейдерный цвет.
            MultiBufferSource tintedBuffers = type -> {
                VertexConsumer consumer = normalBuffers.getBuffer(type);
                if (type.format().getElements().contains(DefaultVertexFormat.ELEMENT_COLOR)) {
                    return new TintedVertexConsumer(consumer, TINT_RED, TINT_GREEN, TINT_BLUE);
                }
                return consumer;
            };

            event.getRenderer().render(
                    clientPlayer,
                    yaw,
                    partialTick,
                    event.getPoseStack(),
                    tintedBuffers,
                    event.getPackedLight()
            );
        } finally {
            renderingNormally = false;
        }
    }

    /** Умножает цвет каждой вершины на заданный оттенок. */
    private static final class TintedVertexConsumer extends VertexConsumerWrapper {

        private final int red;
        private final int green;
        private final int blue;

        private TintedVertexConsumer(VertexConsumer parent, int red, int green, int blue) {
            super(parent);
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            parent.color(r * red / 255, g * green / 255, b * blue / 255, a);
            return this;
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            parent.defaultColor(r * red / 255, g * green / 255, b * blue / 255, a);
        }
    }
}
