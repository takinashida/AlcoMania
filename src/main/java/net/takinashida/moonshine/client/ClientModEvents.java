package net.takinashida.moonshine.client;

import com.moonshine.DrunkennessHUD;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.RenderEventManager;
import net.minecraftforge.client.event.RenderOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLJavaAddModuleAnnotations;

@Mod.EventBusSubscriber
public class ClientModEvents {
    @SubscribeEvent
    public static void onRenderOverlay(RenderOverlayEvent event) {
        if (event.getType() == OverlayType.HUD) {
            // Add the HUD here
        }
    }
}
