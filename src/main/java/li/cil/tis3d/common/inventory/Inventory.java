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
            items[index] = ItemStack.of(itemList.getCompound(index));
        }
    }

    public void writeToNBT(final CompoundNBT nbt) {
        final ListNBT itemList = new ListNBT();
        for (final ItemStack stack : items) {
            final CompoundNBT stackTag = new CompoundNBT();
            if (stack != null) {
                stack.save(stackTag);
            }
            itemList.add(stackTag);
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
    public int getContainerSize() {
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
    public ItemStack getItem(final int index) {
        return items[index];
    }

    @Override
    public ItemStack removeItem(final int index, final int count) {
        if (items[index].getCount() <= count) {
            return removeItemNoUpdate(index);
        } else {
            final ItemStack stack = items[index].split(count);
            assert items[index].getCount() > 0;
            setChanged();
            return stack;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(final int index) {
        final ItemStack stack = items[index];
        setItem(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(final int index, final ItemStack stack) {
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

        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(final PlayerEntity player) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
