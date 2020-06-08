package li.cil.tis3d.common.inventory;

import li.cil.tis3d.util.NBTIds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.Arrays;

/**
 * Base implementation of an array based inventory.
 */
public class ArrayInventory implements Inventory {
    private static final String TAG_ITEMS = "inventory";

    protected final ItemStack[] items;

    public ArrayInventory(final int size) {
        Arrays.fill(items = new ItemStack[size], ItemStack.EMPTY);
    }

    // --------------------------------------------------------------------- //

    public void readFromNBT(final CompoundTag nbt) {
        final ListTag itemList = nbt.getList(TAG_ITEMS, NBTIds.TAG_COMPOUND);
        final int count = Math.min(itemList.size(), items.length);
        for (int index = 0; index < count; index++) {
            items[index] = ItemStack.fromTag(itemList.getCompound(index));
        }
    }

    public void writeToNBT(final CompoundTag nbt) {
        final ListTag itemList = new ListTag();
        for (final ItemStack stack : items) {
            final CompoundTag stackNbt = new CompoundTag();
            if (stack != null) {
                stack.toTag(stackNbt);
            }
            itemList.add(stackNbt);
        }
        nbt.put(TAG_ITEMS, itemList);
    }

    // --------------------------------------------------------------------- //

    protected void onItemAdded(final int index) {
    }

    protected void onItemRemoved(final int index) {
    }

    // --------------------------------------------------------------------- //
    // Inventory

    @Override
    public int getInvSize() {
        return items.length;
    }

    public boolean isInvEmpty() {
        for (final ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getInvStack(final int index) {
        return items[index];
    }

    @Override
    public ItemStack takeInvStack(final int index, final int count) {
        if (items[index].getCount() <= count) {
            return removeInvStack(index);
        } else {
            final ItemStack stack = items[index].split(count);
            assert items[index].getCount() > 0;
            markDirty();
            return stack;
        }
    }

    @Override
    public ItemStack removeInvStack(final int index) {
        final ItemStack stack = items[index];
        setInvStack(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInvStack(final int index, final ItemStack stack) {
        if (items[index] == stack) {
            return;
        }

        if (!items[index].isEmpty()) {
            onItemRemoved(index);
        }

        items[index] = stack;

        if (!items[index].isEmpty()) {
            onItemAdded(index);
        }

        markDirty();
    }

    @Override
    public int getInvMaxStackAmount() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUseInv(final PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }
}
