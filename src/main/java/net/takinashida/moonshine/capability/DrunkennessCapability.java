package net.takinashida.moonshine.capability;

import net.minecraftforge.common.Capability;
import net.minecraftforge.common.CapabilityHolder;
import net.minecraftforge.common.network.NetworkRegistry;

public class DrunkennessCapability {
    public static final Capability<DrunkennessInstance> DRUNKENNESS = Capability.newCapability(
            "moonshine",
            "drunkenness",
            () -> new DrunkennessInstance(),
            DrunkennessInstance::new,
            DrunkennessInstance::new
    );

    public static final CapabilityHolder<DrunkennessInstance> DRUNKENNESS_CAPABILITY = 
            NetworkRegistry.INSTANCE.registerCapability(DRUNKENNESS, "drunkenness");
}
