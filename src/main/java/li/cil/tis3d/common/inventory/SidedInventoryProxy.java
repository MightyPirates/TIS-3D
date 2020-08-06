package li.cil.tis3d.common.inventory;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

public interface SidedInventoryProxy extends InventoryProxy, SidedInventory {
    @Override
    SidedInventory getInventory();

    @Override
    default int[] getAvailableSlots(final Direction facing) {
        return getInventory().getAvailableSlots(facing);
    }

    @Override
    default boolean canInsert(final int slot, final ItemStack stack, @Nullable final Direction facing) {
        return getInventory().canInsert(slot, stack, facing);
    }

    @Override
    default boolean canExtract(final int slot, final ItemStack stack, final Direction facing) {
        return getInventory().canExtract(slot, stack, facing);
    }
}
