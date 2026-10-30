package net.takinashida.moonshine.item;

import net.takinashida.moonshine.capability.DrunkennessCapability;
import net.takinashida.moonshine.capability.DrunkennessInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.attributes.Attributes;

public class BeerBottle extends DrinkableItem {
    public BeerBottle() {
        super(new Item.Properties().stacks(16).useTime(1));
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!world.isClientSide) {
            DrunkennessInstance instance = DrunkennessCapability.DRUNKENNESS_CAPABILITY.get(player);
            if (instance != null) {
                int current = instance.getValue();
                instance.setValue(current + 40);
            }
            
            // Restore 16 HP
            if (player.getHealth() < player.getMaxHealth()) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 16));
            }
            
            // Reduce charge
            if (stack.getCount() > 1) {
                stack.setAmount(stack.getCount() - 1);
            } else {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
        }
        
        return InteractionResult.SUCCESS;
    }
}
