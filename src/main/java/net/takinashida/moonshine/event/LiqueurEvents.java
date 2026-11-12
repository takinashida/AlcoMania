package net.takinashida.moonshine.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.takinashida.moonshine.Config;
import net.takinashida.moonshine.ModEffects;
import net.takinashida.moonshine.MoonShine;

import java.util.List;

/**
 * Механика ликёра.
 *
 *  - +50% к скорости добычи любой киркой/топором.
 *  - 20% шанс дополнительного предмета при добыче железной/угольной руды
 *    и jeg:brimstone_ore. Дополнительный предмет — ровно 1 штука,
 *    вне зависимости от уровня Fortune (не «удача»).
 */
@Mod.EventBusSubscriber(modid = MoonShine.MOD_ID)
public final class LiqueurEvents {

    private LiqueurEvents() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.hasEffect(ModEffects.LIQUEUR.get())) {
            return;
        }

        ItemStack tool = player.getMainHandItem();
        if (!isPickaxeOrAxe(tool)) {
            return;
        }

        float multiplier = 1.0F + Config.liqueurMiningSpeedBonusPercent / 100.0F;
        event.setNewSpeed(event.getNewSpeed() * multiplier);
    }

    private static boolean isPickaxeOrAxe(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.AXES)
                || stack.getItem() instanceof PickaxeItem
                || stack.getItem() instanceof AxeItem
                || stack.canPerformAction(ToolActions.PICKAXE_DIG)
                || stack.canPerformAction(ToolActions.AXE_DIG);
    }

    // LOWEST: сначала пусть отработают другие моды, которые могут отменить ломание.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasEffect(ModEffects.LIQUEUR.get())) {
            return;
        }

        BlockState state = event.getState();
        if (!isTargetOre(state)) {
            return;
        }

        if (level.random.nextFloat() >= Config.liqueurDoubleLootChancePercent / 100.0F) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(event.getPos());
        List<ItemStack> drops = Block.getDrops(
                state,
                level,
                event.getPos(),
                blockEntity,
                player,
                player.getMainHandItem()
        );

        if (drops.isEmpty()) {
            return;
        }

        // Берём тип обычного дропа и добавляем ровно 1 предмет.
        // Количество с Fortune не размножаем — это не «удача».
        ItemStack extra = drops.get(0).copy();
        if (extra.isEmpty()) {
            return;
        }

        extra.setCount(1);
        // Сразу в инвентарь игроку. Если места нет — placeItemBackInInventory
        // сам выбросит остаток на землю.
        player.getInventory().placeItemBackInInventory(extra);
    }

    private static boolean isTargetOre(BlockState state) {
        if (state.is(BlockTags.IRON_ORES) || state.is(BlockTags.COAL_ORES)) {
            return true;
        }

        // Не держим жёсткую ссылку на блок мода jeg: если его нет,
        // ключ просто никогда не совпадёт — краша не будет.
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return key != null
                && "jeg".equals(key.getNamespace())
                && "brimstone_ore".equals(key.getPath());
    }
}
