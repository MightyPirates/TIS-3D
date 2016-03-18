package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

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
    default boolean isUseableByPlayer(final EntityPlayer player) {
        return getInventory().isUseableByPlayer(player);
    }

    @Override
    default void openInventory(final EntityPlayer player) {
        getInventory().openInventory(player);
    }

    @Override
    default void closeInventory(final EntityPlayer player) {
        getInventory().closeInventory(player);
    }

    @Override
    default boolean isItemValidForSlot(final int slot, final ItemStack stack) {
        return getInventory().isItemValidForSlot(slot, stack);
    }

    @Override
    default int getField(final int index) {
        return getInventory().getField(index);
    }

    @Override
    default void setField(final int index, final int value) {
        getInventory().setField(index, value);
    }

    @Override
    default int getFieldCount() {
        return getInventory().getFieldCount();
    }

    @Override
    default void clear() {
        getInventory().clear();
    }

    @Override
    default String getName() {
        return getInventory().getName();
    }

    @Override
    default boolean hasCustomName() {
        return getInventory().hasCustomName();
    }

    @Override
    default ITextComponent getDisplayName() {
        return getInventory().getDisplayName();
    }
}
