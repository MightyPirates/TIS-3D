package li.cil.tis3d.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface InventoryProxy extends Container {
    Container getInventory();

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
    default boolean stillValid(final Player player) {
        return getInventory().stillValid(player);
    }

    @Override
    default void startOpen(final ContainerUser user) {
        getInventory().startOpen(user);
    }

    @Override
    default void stopOpen(final ContainerUser user) {
        getInventory().stopOpen(user);
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
