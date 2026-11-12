package net.takinashida.moonshine.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.takinashida.moonshine.client.ClientData;

import java.util.function.Supplier;

/**
 * Пакет "сервер -> клиент": сообщает клиенту, сколько сейчас опьянения у игрока.
 *
 * Пакет нужен только для HUD и визуальных эффектов. Серверная логика от него не зависит.
 */
public class SyncDrunkennessPacket {

    /** Новое значение опьянения. */
    private final int value;

    public SyncDrunkennessPacket(int value) {
        this.value = value;
    }

    /** Запись значения в сетевой буфер. */
    public static void encode(SyncDrunkennessPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.value);
    }

    /** Чтение значения из сетевого буфера. */
    public static SyncDrunkennessPacket decode(FriendlyByteBuf buffer) {
        return new SyncDrunkennessPacket(buffer.readVarInt());
    }

    /**
     * Обработка пакета.
     *
     * DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...) нужен, чтобы на выделенном сервере
     * даже не пытаться загрузить клиентский класс ClientData.
     */
    public static void handle(SyncDrunkennessPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientData.setDrunkenness(packet.value));
        context.setPacketHandled(true);
    }
}
