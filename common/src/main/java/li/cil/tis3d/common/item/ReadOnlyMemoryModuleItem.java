/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.item;

import dev.architectury.registry.menu.MenuRegistry;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.container.ReadOnlyMemoryModuleContainer;
import li.cil.tis3d.util.ItemStackUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class ReadOnlyMemoryModuleItem extends ModuleItem {
    private static final String TAG_DATA = "data";
    private static final byte[] EMPTY_DATA = new byte[0];

    public ReadOnlyMemoryModuleItem(final Properties properties) {
        super(properties.stacksTo(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (!level.isClientSide() && player instanceof final ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Override
                public AbstractContainerMenu createMenu(final int id, final Inventory playerInventory, final Player player) {
                    return new ReadOnlyMemoryModuleContainer(id, player, hand);
                }
            }, buffer -> buffer.writeEnum(hand));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        return CasingBlock.useIfCasing(context).orElseGet(() -> super.useOn(context));
    }

    // --------------------------------------------------------------------- //

    /**
     * Load ROM data from the specified tag.
     *
     * @param tag the tag to load the data from.
     * @return the data loaded from the tag.
     */
    public static byte[] loadFromTag(@Nullable final CompoundTag tag) {
        if (tag != null) {
            return tag.getByteArray(TAG_DATA).orElse(EMPTY_DATA);
        }
        return EMPTY_DATA;
    }

    /**
     * Load ROM data from the specified item stack.
     *
     * @param stack the item stack to load the data from.
     * @return the data loaded from the stack.
     */
    public static byte[] loadFromStack(final ItemStack stack) {
        return loadFromTag(ItemStackUtils.getData(stack));
    }

    /**
     * Save the specified ROM data to the specified item stack.
     *
     * @param stack the item stack to save the data to.
     * @param data  the data to save to the item stack.
     */
    public static void saveToStack(final ItemStack stack, final byte[] data) {
        ItemStackUtils.updateData(stack, tag -> tag.putByteArray(TAG_DATA, data.clone()));
    }
}
