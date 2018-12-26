package li.cil.tis3d.common.inventory;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

public interface SidedInventoryProxy extends InventoryProxy, SidedInventory {
    @Override
    SidedInventory getInventory();

    @Override
    default int[] getInvAvailableSlots(final Direction facing) {
        return getInventory().getInvAvailableSlots(facing);
    }

    @Override
    default boolean canInsertInvStack(final int slot, final ItemStack stack, @Nullable final Direction facing) {
        return getInventory().canInsertInvStack(slot, stack, facing);
    }

    @Override
    default boolean canExtractInvStack(final int slot, final ItemStack stack, final Direction facing) {
        return getInventory().canExtractInvStack(slot, stack, facing);
    }
}
