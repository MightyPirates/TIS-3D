package li.cil.tis3d.common.inventory;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public interface SidedInventoryProxy extends InventoryProxy, ISidedInventory {
    @Override
    ISidedInventory getInventory();

    @Override
    default int[] getAccessibleSlotsFromSide(final int facing) {
        return getInventory().getAccessibleSlotsFromSide(facing);
    }

    @Override
    default boolean canInsertItem(final int slot, final ItemStack stack, final int facing) {
        return getInventory().canInsertItem(slot, stack, facing);
    }

    @Override
    default boolean canExtractItem(final int slot, final ItemStack stack, final int facing) {
        return getInventory().canExtractItem(slot, stack, facing);
    }
}
