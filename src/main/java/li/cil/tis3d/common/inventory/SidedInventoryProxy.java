package li.cil.tis3d.common.inventory;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public interface SidedInventoryProxy extends InventoryProxy, ISidedInventory {
    @Override
    ISidedInventory getInventory();

    @Override
    default int[] getSlotsForFace(final Direction facing) {
        return getInventory().getSlotsForFace(facing);
    }

    @Override
    default boolean canPlaceItemThroughFace(final int slot, final ItemStack stack, @Nullable final Direction facing) {
        return getInventory().canPlaceItemThroughFace(slot, stack, facing);
    }

    @Override
    default boolean canTakeItemThroughFace(final int slot, final ItemStack stack, final Direction facing) {
        return getInventory().canTakeItemThroughFace(slot, stack, facing);
    }
}
