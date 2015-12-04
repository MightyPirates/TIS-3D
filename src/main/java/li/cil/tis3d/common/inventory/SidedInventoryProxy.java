package li.cil.tis3d.common.inventory;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface SidedInventoryProxy extends InventoryProxy, ISidedInventory {
    @Override
    ISidedInventory getInventory();

    @Override
    default int[] getSlotsForFace(final EnumFacing facing) {
        return getInventory().getSlotsForFace(facing);
    }

    @Override
    default boolean canInsertItem(final int slot, final ItemStack stack, final EnumFacing facing) {
        return getInventory().canInsertItem(slot, stack, facing);
    }

    @Override
    default boolean canExtractItem(final int slot, final ItemStack stack, final EnumFacing facing) {
        return getInventory().canExtractItem(slot, stack, facing);
    }
}
