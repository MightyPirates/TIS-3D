package li.cil.tis3d.common.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface SidedInventoryProxy extends InventoryProxy, WorldlyContainer {
    @Override
    WorldlyContainer getInventory();

    @Override
    default int[] getSlotsForFace(final Direction facing) {
        return getInventory().getSlotsForFace(facing);
    }

    @Override
    default boolean canPlaceItemThroughFace(final int slot, final ItemStack stack, @Nullable final Direction facing) {
        return getInventory().canPlaceItemThroughFace(slot, stack, facing);
    }

    @Override
    default boolean canTakeItemThroughFace(final int slot, final ItemStack stack, final Direction facing) {
        return getInventory().canTakeItemThroughFace(slot, stack, facing);
    }
}
