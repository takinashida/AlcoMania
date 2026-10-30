package net.takinashida.moonshine.client;

import net.takinashida.moonshine.capability.DrunkennessCapability;
import net.takinashida.moonshine.capability.DrunkennessInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.hud.GuiGraphics;
import net.minecraft.client.gui.hud.Overlay;
import net.minecraft.client.gui.hud.OverlayType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.RemoteDist;

@RemoteDist(Dist.CLIENT)
public class DrunkennessHUD implements Overlay<OverlayType.HUD> {
    @Override
    public void render(OverlayContext context, Integer bufferState, int buffer) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        DrunkennessInstance instance = DrunkennessCapability.DRUNKENNESS_CAPABILITY.get(player);
        if (instance == null) return;

        float drunkenness = instance.getValue();
        
        GuiGraphics graphics = context.getGuiGraphics();
        int x = 10;
        int y = Minecraft.getInstance().getWindow().getGuiHeight() - 30;
        int width = 200;
        int height = 20;

        // Draw background
        graphics.fill(x, y, x + width, y + height, 0xFF000000);
        
        // Draw progress
        int progressWidth = (int) (width * (drunkenness / 100.0f));
        graphics.fill(x, y, x + progressWidth, y + height, 0xFFFF0000);
    }
}
