package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;

/**
 * Base implementation of an array based inventory.
 */
public class Inventory implements IInventory {
    private static final String TAG_ITEMS = "inventory";

    protected final ItemStack[] items;

    public Inventory(final int size) {
        Arrays.fill(items = new ItemStack[size], ItemStack.EMPTY);
    }

    // --------------------------------------------------------------------- //

    public void readFromNBT(final CompoundNBT nbt) {
        final ListNBT itemList = nbt.getList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        final int count = Math.min(itemList.size(), items.length);
        for (int index = 0; index < count; index++) {
            items[index] = ItemStack.read(itemList.getCompound(index));
        }
    }

    public void writeToNBT(final CompoundNBT nbt) {
        final ListNBT itemList = new ListNBT();
        for (final ItemStack stack : items) {
            final CompoundNBT stackNbt = new CompoundNBT();
            if (stack != null) {
                stack.write(stackNbt);
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
    // IInventory

    @Override
    public int getSizeInventory() {
        return items.length;
    }

    public boolean isEmpty() {
        for (final ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStackInSlot(final int index) {
        return items[index];
    }

    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        if (items[index].getCount() <= count) {
            return removeStackFromSlot(index);
        } else {
            final ItemStack stack = items[index].split(count);
            assert items[index].getCount() > 0;
            markDirty();
            return stack;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(final int index) {
        final ItemStack stack = items[index];
        setInventorySlotContents(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
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
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player) {
        return true;
    }

    @Override
    public void openInventory(final PlayerEntity player) {
    }

    @Override
    public void closeInventory(final PlayerEntity player) {
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return true;
    }

    @Override
    public void clear() {
    }
}
