package net.takinashida.moonshine.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.takinashida.moonshine.capability.DrunkennessCapability;
import net.takinashida.moonshine.network.Networking;
import net.takinashida.moonshine.network.SyncDrunkennessPacket;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Базовый класс для всех алкогольных напитков.
 *
 * Общая механика:
 *  - у предмета есть "заряды" через прочность (durability), число задаётся в конфиге;
 *  - при зажатии ПКМ проигрывается анимация питья ~1.6 сек (как у зелий);
 *  - после завершения питья добавляется опьянение, применяется бонус напитка,
 *    запускается перезарядка и тратится 1 единица прочности;
 *  - когда прочность заканчивается, предмет исчезает.
 */
public abstract class DrinkableItem extends Item {

    /** Сколько тиков длится питьё. 32 тика = 1.6 секунды (столько же у зелий). */
    private static final int DRINK_DURATION_TICKS = 32;

    /** Мод и предмет, которые нужны для выпадения пустой бутылки. */
    private static final String SURVIVORS_ARSENAL_ID = "survivorsarsenal";
    private static final String SURVIVORS_ARSENAL_JAR = "survivorsarsenal-1.1.7_test_version.jar";
    private static final ResourceLocation EMPTY_GLASS_BOTTLE =
            new ResourceLocation(SURVIVORS_ARSENAL_ID, "empty_glass_bottle");

    /**
     * Заглушка прочности в конструкторе.
     *
     * Настоящее количество применений читается из конфига в {@link #getMaxDamage}.
     * Здесь важно только то, что значение больше нуля: иначе Minecraft не считает
     * предмет "ломаемым" и не рисует полоску прочности.
     */
    private static final int PLACEHOLDER_MAX_DAMAGE = 1;

    protected DrinkableItem(Properties properties) {
        // stacksTo(1) — предмет не стакается. Предмет с прочностью обязан лежать
        // в отдельном слоте: иначе непонятно, к какому из стака предметов относится износ.
        super(properties.stacksTo(1).durability(PLACEHOLDER_MAX_DAMAGE));
    }

    /** Сколько единиц опьянения даёт напиток. */
    protected abstract int getDrunkenness();

    /**
     * Сколько раз можно выпить из одного предмета.
     * Читается из конфига при каждом обращении — см. {@link #getMaxDamage}.
     */
    protected abstract int getMaxUses();

    /** Перезарядка между глотками в секундах. 0 = без перезарядки. */
    protected abstract int getCooldownSeconds();

    /**
     * Перезарядка в тиках; 0 — без перезарядки.
     * Нужна снаружи класса (например, чтобы перенести остаток кулдауна
     * на нового игрока после смерти).
     */
    public int getCooldownTicks() {
        return Math.max(0, getCooldownSeconds()) * 20;
    }

    /** Дополнительный эффект конкретного напитка (скорость, лечение и т.п.). */
    protected abstract void applyBonus(Player player, Level level);

    /** Должен ли предмет после последнего глотка оставить пустую бутылку. */
    protected boolean dropsEmptyBottle() {
        return true;
    }

    /**
     * Максимальная прочность предмета = количество применений.
     *
     * Minecraft вызывает этот метод и при отрисовке полоски, и при износе,
     * поэтому значение из конфига подхватывается без перезапуска игры.
     * Math.max(1, ...) защищает от нуля: предмет с нулевой прочностью сломался бы сразу.
     */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return Math.max(1, getMaxUses());
    }

    /**
     * Длительность использования предмета.
     * Именно столько тиков игрок должен держать ПКМ.
     */
    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_DURATION_TICKS;
    }

    /**
     * Тип анимации. UseAnim.DRINK — анимация питья, как у зелий.
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    /**
     * Вызывается в момент, когда игрок только начал зажимать ПКМ.
     * Мы не применяем эффект сразу — просто запускаем процесс использования.
     * Через 32 тика Forge сам вызовет finishUsingItem.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    /**
     * Вызывается, когда игрок продержал ПКМ полное время (32 тика).
     * Здесь и происходят все реальные изменения.
     *
     * Важно: выполняем логику только на сервере (!level.isClientSide),
     * иначе эффекты и опьянение применятся дважды.
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // 1. Добавляем опьянение. У рассола getDrunkenness() = 0,
            //    а сброс шкалы делает его applyBonus().
            player.getCapability(DrunkennessCapability.DRUNKENNESS).ifPresent(
                            value -> value.add(getDrunkenness()));

            // 2. Применяем бонус конкретного напитка.
            //    Именно здесь рассол снимает эффекты и обнуляет опьянение,
            //    поэтому синхронизацию шкалы делаем ПОСЛЕ этого шага.
            applyBonus(player, level);

            // 3. Отправляем клиенту итоговое значение — HUD и шатание камеры
            //    должны обновиться сразу, не дожидаясь следующего тика.
            if (player instanceof ServerPlayer serverPlayer) {
                player.getCapability(DrunkennessCapability.DRUNKENNESS).ifPresent(
                        drunkenness -> Networking.sendToPlayer(
                                new SyncDrunkennessPacket(drunkenness.getValue()), serverPlayer));
            }

            // 4. Запускаем перезарядку. Кулдаун хранится у игрока и сам
            //    синхронизируется на клиент, поэтому вызываем только на сервере.
            int cooldownTicks = getCooldownTicks();
            if (cooldownTicks > 0) {
                player.getCooldowns().addCooldown(this, cooldownTicks);
            }

            // 5. Тратим одно применение.
            //    Мы не храним "заряды" в NBT: вместо этого наносим предмету 1 урон прочности.
            //    Когда износ дойдёт до максимума, предмет сломается сам.
            //    Именно поэтому в инвентаре видна обычная полоска прочности — она и есть
            //    индикатор оставшихся глотков.
            InteractionHand hand = player.getUsedItemHand();
            boolean lastUse = stack.getDamageValue() + 1 >= stack.getMaxDamage();
            stack.hurtAndBreak(1, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(hand));

            if (lastUse && stack.isEmpty()) {
                dropEmptyBottle(player);
            }
        }

        return super.finishUsingItem(stack, level, entity);
    }

    /**
     * После последнего глотка алкоголя, если установлен Survivor's Arsenal,
     * выбрасывает survivorsarsenal:empty_glass_bottle.
     *
     * Проверяем и id мода, и точное имя jar-файла: если мод отсутствует
     * или jar другой версии, ничего не дропаем.
     */
    private void dropEmptyBottle(Player player) {
        if (!dropsEmptyBottle() || !ModList.get().isLoaded(SURVIVORS_ARSENAL_ID)) {
            return;
        }

        boolean exactJarPresent = ModList.get().getModFiles().stream()
                .anyMatch(info -> SURVIVORS_ARSENAL_JAR.equals(info.getFile().getFileName()));
        if (!exactJarPresent) {
            return;
        }

        Item emptyBottle = ForgeRegistries.ITEMS.getValue(EMPTY_GLASS_BOTTLE);
        if (emptyBottle != null) {
            player.drop(new ItemStack(emptyBottle), false);
        }
    }

    /**
     * Подсказка в инвентаре: сколько применений осталось и сколько опьянения даёт напиток.
     * Считаем как "максимальная прочность минус текущий износ".
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int usesLeft = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("tooltip.alcomania.uses", usesLeft)
                .withStyle(ChatFormatting.GRAY));

        // У рассола опьянение не добавляется — вместо "+0" показываем его особенность.
        if (getDrunkenness() > 0) {
            tooltip.add(Component.translatable("tooltip.alcomania.drunkenness", getDrunkenness())
                    .withStyle(ChatFormatting.DARK_GREEN));
        }

        // Перезарядка, если она вообще есть (у пива по умолчанию её нет).
        if (getCooldownSeconds() > 0) {
            tooltip.add(Component.translatable("tooltip.alcomania.cooldown", getCooldownSeconds())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /**
     * Утилита для наследников: переводит секунды из конфига в тики.
     * В Minecraft 1 секунда = 20 тиков.
     */
    protected static int secondsToTicks(int seconds) {
        return seconds * 20;
    }
}
