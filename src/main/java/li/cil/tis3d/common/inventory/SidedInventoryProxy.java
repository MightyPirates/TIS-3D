package li.cil.tis3d.common.inventory;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public interface SidedInventoryProxy extends InventoryProxy, ISidedInventory {
    @Override
    ISidedInventory getInventory();

    @Override
    default int[] getSlotsForFace(final Direction facing) {
        return getInventory().getSlotsForFace(facing);
    }

    @Override
    default boolean canInsertItem(final int slot, final ItemStack stack, final Direction facing) {
        return getInventory().canInsertItem(slot, stack, facing);
    }

    @Override
    default boolean canExtractItem(final int slot, final ItemStack stack, final Direction facing) {
        return getInventory().canExtractItem(slot, stack, facing);
    }
}
