package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface InventoryProxy extends Inventory {
    Inventory getInventory();

    @Override
    default int getInvSize() {
        return getInventory().getInvSize();
    }

    @Override
    default boolean isInvEmpty() {
        return getInventory().isInvEmpty();
    }

    @Override
    default ItemStack getInvStack(final int slot) {
        return getInventory().getInvStack(slot);
    }

    @Override
    default ItemStack takeInvStack(final int slot, final int count) {
        return getInventory().takeInvStack(slot, count);
    }

    @Override
    default ItemStack removeInvStack(final int slot) {
        return getInventory().removeInvStack(slot);
    }

    @Override
    default void setInvStack(final int slot, final ItemStack stack) {
        getInventory().setInvStack(slot, stack);
    }

    @Override
    default int getInvMaxStackAmount() {
        return getInventory().getInvMaxStackAmount();
    }

    @Override
    default void markDirty() {
        getInventory().markDirty();
    }

    @Override
    default boolean canPlayerUseInv(final PlayerEntity player) {
        return getInventory().canPlayerUseInv(player);
    }

    @Override
    default void onInvOpen(final PlayerEntity player) {
        getInventory().onInvOpen(player);
    }

    @Override
    default void onInvClose(final PlayerEntity player) {
        getInventory().onInvClose(player);
    }

    @Override
    default boolean isValidInvStack(final int slot, final ItemStack stack) {
        return getInventory().isValidInvStack(slot, stack);
    }

    @Override
    default void clear() {
        getInventory().clear();
    }
}
