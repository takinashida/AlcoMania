package net.takinashida.moonshine.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Описание capability "Опьянение".
 *
 * Capability — это способ Forge прикрепить свои данные к чужому объекту (например, к игроку),
 * не изменяя сам класс игрока.
 *
 * Здесь мы создаём "ключ", по которому потом сможем получить данные:
 *     player.getCapability(DrunkennessCapability.DRUNKENNESS)
 */
public final class DrunkennessCapability {

    /**
     * Токен нужен, чтобы Forge мог создать Capability до того, как будет зарегистрирована
     * реализация. Конкретный класс данных (DrunkennessInstance) указывается при регистрации
     * в CapabilityRegistration.
     */
    public static final Capability<DrunkennessInstance> DRUNKENNESS =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private DrunkennessCapability() {
    }
}
