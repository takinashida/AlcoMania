package net.takinashida.moonshine.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.MoonShine;

/**
 * Client-side rendering for the Spectral effect (alcomania:spectral).
 *
 * The effect deliberately does not touch vanilla Glowing: normal spectral
 * arrows and /effect glowing keep working as before.
 *
 * The server-side SpectralEffect sets the entity's glowing flag, so vanilla
 * routes that entity through OutlineBufferSource (the x-ray outline pass).
 * Here we intercept that pass for ALL living entities — players and mobs —
 * and re-render the entity into the normal depth-tested buffer with a cyan
 * tint. As a result, the entity is visible only in direct line of sight,
 * not through walls.
 *
 * Why RenderLivingEvent instead of RenderPlayerEvent:
 * RenderPlayerEvent only covers players, so mobs with the Spectral effect
 * would keep the vanilla through-wall outline. RenderLivingEvent covers both.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, value = Dist.CLIENT)
public final class SpectralEffectRenderHandler {

    /** Tint color: a light cyan/teal spectral shade. */
    private static final int TINT_RED = 120;
    private static final int TINT_GREEN = 225;
    private static final int TINT_BLUE = 255;

    /** Re-entry guard for the manual render call below. */
    private static boolean renderingNormally = false;

    private SpectralEffectRenderHandler() {
    }

    /**
     * Called by Forge for every living entity render.
     *
     * We only act when:
     *  1. the entity has OUR Spectral effect (not vanilla Glowing);
     *  2. vanilla is using OutlineBufferSource (the x-ray outline pass).
     *
     * In that case the vanilla outline render is cancelled and the entity is
     * rendered again through the main buffer with tinted vertices, so normal
     * depth testing hides it behind walls.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre event) {
        // The manual render below fires RenderLivingEvent again; let it pass.
        if (renderingNormally) {
            return;
        }

        LivingEntity entity = event.getEntity();

        // Only our custom effect, never vanilla Glowing.
        if (!entity.hasEffect(ModEffects.SPECTRAL.get())) {
            return;
        }

        // Only the outline/x-ray pass. Other passes render normally already.
        if (!(event.getMultiBufferSource() instanceof OutlineBufferSource)) {
            return;
        }

        event.setCanceled(true);
        renderingNormally = true;
        try {
            float partialTick = event.getPartialTick();
            float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());

            MultiBufferSource normalBuffers = Minecraft.getInstance()
                    .renderBuffers()
                    .bufferSource();

            /*
             * Wrap the normal buffer so only this entity's vertices are tinted.
             * The global shader color is not changed, so other entities keep
             * their normal colors.
             */
            MultiBufferSource tintedBuffers = type -> {
                VertexConsumer consumer = normalBuffers.getBuffer(type);
                if (type.format().getElements().contains(DefaultVertexFormat.ELEMENT_COLOR)) {
                    return new TintedVertexConsumer(consumer, TINT_RED, TINT_GREEN, TINT_BLUE);
                }
                return consumer;
            };

            // Raw renderer type: this works for PlayerRenderer and any mob
            // renderer because all of them extend LivingEntityRenderer.
            LivingEntityRenderer renderer = event.getRenderer();
            renderer.render(
                    entity,
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

    /** Multiplies every vertex color by the given tint. */
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
