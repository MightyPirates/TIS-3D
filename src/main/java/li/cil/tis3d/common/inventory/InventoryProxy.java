package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface InventoryProxy extends IInventory {
    IInventory getInventory();

    @Override
    default int getSizeInventory() {
        return getInventory().getSizeInventory();
    }

    @Override
    default ItemStack getStackInSlot(final int slot) {
        return getInventory().getStackInSlot(slot);
    }

    @Override
    default ItemStack decrStackSize(final int slot, final int count) {
        return getInventory().decrStackSize(slot, count);
    }

    @Override
    default ItemStack getStackInSlotOnClosing(final int slot) {
        return getInventory().getStackInSlotOnClosing(slot);
    }

    @Override
    default void setInventorySlotContents(final int slot, final ItemStack stack) {
        getInventory().setInventorySlotContents(slot, stack);
    }

    @Override
    default int getInventoryStackLimit() {
        return getInventory().getInventoryStackLimit();
    }

    @Override
    default void markDirty() {
        getInventory().markDirty();
    }

    @Override
    default boolean isUseableByPlayer(final EntityPlayer player) {
        return getInventory().isUseableByPlayer(player);
    }

    @Override
    default void openInventory() {
        getInventory().openInventory();
    }

    @Override
    default void closeInventory() {
        getInventory().closeInventory();
    }

    @Override
    default boolean isItemValidForSlot(final int slot, final ItemStack stack) {
        return getInventory().isItemValidForSlot(slot, stack);
    }

    @Override
    default String getInventoryName() {
        return getInventory().getInventoryName();
    }

    @Override
    default boolean hasCustomInventoryName() {
        return getInventory().hasCustomInventoryName();
    }
}
