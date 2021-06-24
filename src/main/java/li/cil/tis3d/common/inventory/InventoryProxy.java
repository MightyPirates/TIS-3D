package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface InventoryProxy extends IInventory {
    IInventory getInventory();

    @Override
    default int getSizeInventory() {
        return getInventory().getSizeInventory();
    }

    @Override
    default boolean isEmpty() {
        return getInventory().isEmpty();
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
    default ItemStack removeStackFromSlot(final int slot) {
        return getInventory().removeStackFromSlot(slot);
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
    default boolean isUsableByPlayer(final PlayerEntity player) {
        return getInventory().isUsableByPlayer(player);
    }

    @Override
    default void openInventory(final PlayerEntity player) {
        getInventory().openInventory(player);
    }

    @Override
    default void closeInventory(final PlayerEntity player) {
        getInventory().closeInventory(player);
    }

    @Override
    default boolean isItemValidForSlot(final int slot, final ItemStack stack) {
        return getInventory().isItemValidForSlot(slot, stack);
    }

    @Override
    default void clear() {
        getInventory().clear();
    }
}
