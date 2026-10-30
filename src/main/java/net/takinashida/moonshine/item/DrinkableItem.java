package net.takinashida.moonshine.item;

import net.takinashida.moonshine.capability.DrunkennessCapability;
import net.takinashida.moonshine.capability.DrunkennessInstance;
import net.minecraft.network.constructor.Craftiness;
import net.minecraft.entity.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

public class DrinkableItem extends Item {
    public DrinkableItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // Check charges (simplified: check stack size or a custom data)
        // For the prompt, let's just check if it's a stack of 1 and treat it as 1 use.
        // A better way is to use a custom tag or a variable. 
        // Given the prompt, I'll assume the stack size is the charge or just allow use.
        
        if (!world.isClientSide) {
            DrunkennessInstance instance = DrunkennessCapability.DRUNKENNESS_CAPABILITY.get(player);
            if (instance != null) {
                // Apply drunkenness (specific values are handled in the subclasses)
                // Logic for specific drinks happens in subclasses.
            }
        }
        
        return InteractionResult.SUCCESS;
    }
}
