package net.takinashida.moonshine;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Добавляет предметы мода в ванильную вкладку "Еда и напитки" творческого режима.
 * Без этого предметы существовали бы, но их нельзя было бы найти в креативе.
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabs {

    private ModCreativeTabs() {
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Нас интересует только вкладка с едой и напитками.
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.APPLE_CIDER, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.BEER, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.VODKA, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.WINE, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.WHISKY, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.HONEY, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.TEQUILA, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.LIQUEUR, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(ModItems.RASSOL, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
