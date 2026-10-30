package net.takinashida.moonshine.network;

import net.minecraft.network.RegistrationChannel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.msdk.PacketHandler;
import net.minecraftforge.registries.RegisterObject;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.ConnectingPacketHandler;
import net.minecraftforge.network.PacketHandler;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.PacketHandler.SimpleChannel;

import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class Networking {
    private static final String CHANNEL_NAME = "moonshine_channel";
    public static final SimpleChannel CHANNEL = new SimpleChannel(CHANNEL_NAME, new PacketHandler.PayloadHandler<SimpleChannel.Packet<?>>() {
        @Override
        public void handle(SimpleChannel.Packet<?> packet, ConnectingPacketHandler handler) {
            // Handle packets
        }
    });

    public static void register() {
        CHANNEL.register();
    }
}
