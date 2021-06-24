package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface InventoryProxy extends IInventory {
    IInventory getInventory();

    @Override
    default int getContainerSize() {
        return getInventory().getContainerSize();
    }

    @Override
    default boolean isEmpty() {
        return getInventory().isEmpty();
    }

    @Override
    default ItemStack getItem(final int slot) {
        return getInventory().getItem(slot);
    }

    @Override
    default ItemStack removeItem(final int slot, final int count) {
        return getInventory().removeItem(slot, count);
    }

    @Override
    default ItemStack removeItemNoUpdate(final int slot) {
        return getInventory().removeItemNoUpdate(slot);
    }

    @Override
    default void setItem(final int slot, final ItemStack stack) {
        getInventory().setItem(slot, stack);
    }

    @Override
    default int getMaxStackSize() {
        return getInventory().getMaxStackSize();
    }

    @Override
    default void setChanged() {
        getInventory().setChanged();
    }

    @Override
    default boolean stillValid(final PlayerEntity player) {
        return getInventory().stillValid(player);
    }

    @Override
    default void startOpen(final PlayerEntity player) {
        getInventory().startOpen(player);
    }

    @Override
    default void stopOpen(final PlayerEntity player) {
        getInventory().stopOpen(player);
    }

    @Override
    default boolean canPlaceItem(final int slot, final ItemStack stack) {
        return getInventory().canPlaceItem(slot, stack);
    }

    @Override
    default void clearContent() {
        getInventory().clearContent();
    }
}
