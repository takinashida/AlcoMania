package net.takinashida.moonshine;

import net.takinashida.moonshine.item.AppleCider;
import net.takinashida.moonshine.item.BeerBottle;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryObjects;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoonShine.MOD_ID);

    public static final RegistryObject<Item> APPLE_CIDER = ITEMS.register("apple_cider", 
            () -> new AppleCider());

    public static final RegistryObject<Item> BEER_BOTTLE = ITEMS.register("beer_bottle", 
            () -> new BeerBottle());

    public static final RegistryObject<Item>[] ITEMS_ARRAY = { APPLE_CIDER, BEER_BOTTLE };
}
