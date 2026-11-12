package net.takinashida.moonshine.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * "Контейнер" capability для одного игрока.
 *
 * ICapabilitySerializable означает две вещи:
 *  1. Умеет отдавать данные по запросу getCapability(...).
 *  2. Умеет сохраняться в NBT и загружаться из NBT (чтобы опьянение не сбрасывалось
 *     при перезаходе в мир).
 */
public class DrunkennessProvider implements ICapabilitySerializable<CompoundTag> {

    /** Сами данные конкретного игрока. */
    private final DrunkennessInstance instance = new DrunkennessInstance();

    /**
     * LazyOptional — обёртка, которая позволяет безопасно "выключить" capability,
     * когда игрок удаляется. Пока игрок жив, optional отдаёт instance.
     */
    private final LazyOptional<DrunkennessInstance> optional = LazyOptional.of(() -> instance);

    /**
     * Forge спрашивает: "есть ли у тебя capability такого-то типа?".
     * Если тип совпадает с нашим — отдаём данные, иначе пустой optional.
     */
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return DrunkennessCapability.DRUNKENNESS.orEmpty(cap, optional);
    }

    /** Сохранение в NBT (например, при выходе из мира). */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Drunkenness", instance.getValue());
        return tag;
    }

    /** Загрузка из NBT (при входе в мир). */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setValue(nbt.getInt("Drunkenness"));
    }

    /** Вызывается, когда игрок удаляется: больше не отдаём данные. */
    public void invalidate() {
        optional.invalidate();
    }
}
