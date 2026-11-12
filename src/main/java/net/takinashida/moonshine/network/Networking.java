package net.takinashida.moonshine.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.takinashida.moonshine.MoonShine;

/**
 * Сетевой канал мода.
 *
 * Нужен, чтобы сервер мог сообщить клиенту текущее значение опьянения
 * (клиент не может сам читать серверную capability игрока).
 */
public final class Networking {

    /** Версия протокола. Если она не совпадёт у клиента и сервера — соединение отклонят. */
    private static final String PROTOCOL_VERSION = "1";

    /** Счётчик id пакетов. Каждый новый пакет получает уникальный номер. */
    private static int nextMessageId = 0;

    /** Основной канал мода. */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MoonShine.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private Networking() {
    }

    /** Регистрирует пакеты. Вызывается из MoonShine в commonSetup. */
    public static void register() {
        CHANNEL.messageBuilder(SyncDrunkennessPacket.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                // Как превратить объект пакета в байты.
                .encoder(SyncDrunkennessPacket::encode)
                // Как собрать объект пакета обратно из байтов.
                .decoder(SyncDrunkennessPacket::decode)
                // Что делать при получении. consumerMainThread гарантирует главный поток клиента.
                .consumerMainThread(SyncDrunkennessPacket::handle)
                .add();
    }

    /** Отправить пакет конкретному игроку. */
    public static void sendToPlayer(SyncDrunkennessPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
