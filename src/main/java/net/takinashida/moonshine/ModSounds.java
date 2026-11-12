package net.takinashida.moonshine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Звуковые события мода.
 *
 * Сами ogg-файлы лежат в assets/alcomania/sounds/:
 *  - honeys_remove.ogg — звук снятия/окончания «Медового щита»;
 *  - heart_beat.ogg    — сердцебиение во время действия виски;
 *  - sonar.ogg         — «пинг» импульса подсветки от водки.
 *
 * Регистрация описана в assets/alcomania/sounds.json.
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MoonShine.MOD_ID);

    /** Звук снятия/окончания эффекта медовухи. */
    public static final RegistryObject<SoundEvent> HONEY_REMOVE =
            register("honeys_remove");

    /** Сердцебиение во время эффекта виски. */
    public static final RegistryObject<SoundEvent> HEART_BEAT =
            register("heart_beat");

    /** «Пинг» сонара, звучащий в начале каждого импульса подсветки от водки. */
    public static final RegistryObject<SoundEvent> SONAR =
            register("sonar");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(MoonShine.MOD_ID, name)));
    }

    /** Подписывает реестр на шину событий мода. Вызывается из MoonShine. */
    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
