package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface InventoryProxy extends Inventory {
    Inventory getInventory();

    @Override
    default int size() {
        return getInventory().size();
    }

    @Override
    default boolean isEmpty() {
        return getInventory().isEmpty();
    }

    @Override
    default ItemStack getStack(final int slot) {
        return getInventory().getStack(slot);
    }

    @Override
    default ItemStack removeStack(final int slot, final int count) {
        return getInventory().removeStack(slot, count);
    }

    @Override
    default ItemStack removeStack(final int slot) {
        return getInventory().removeStack(slot);
    }

    @Override
    default void setStack(final int slot, final ItemStack stack) {
        getInventory().setStack(slot, stack);
    }

    @Override
    default int getMaxCountPerStack() {
        return getInventory().getMaxCountPerStack();
    }

    @Override
    default void markDirty() {
        getInventory().markDirty();
    }

    @Override
    default boolean canPlayerUse(final PlayerEntity player) {
        return getInventory().canPlayerUse(player);
    }

    @Override
    default void onOpen(final PlayerEntity player) {
        getInventory().onOpen(player);
    }

    @Override
    default void onClose(final PlayerEntity player) {
        getInventory().onClose(player);
    }

    @Override
    default boolean isValid(final int slot, final ItemStack stack) {
        return getInventory().isValid(slot, stack);
    }

    @Override
    default void clear() {
        getInventory().clear();
    }
}
