package li.cil.tis3d.common.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;

/**
 * Base implementation of an array based inventory.
 */
public class Inventory implements Container {
    private static final String TAG_ITEMS = "inventory";

    protected final ItemStack[] items;

    public Inventory(final int size) {
        Arrays.fill(items = new ItemStack[size], ItemStack.EMPTY);
    }

    // --------------------------------------------------------------------- //

    public void load(final CompoundTag tag) {
        final ListTag itemList = tag.getList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        final int count = Math.min(itemList.size(), items.length);
        for (int index = 0; index < count; index++) {
            items[index] = ItemStack.of(itemList.getCompound(index));
        }
    }

    public void save(final CompoundTag tag) {
        final ListTag itemList = new ListTag();
        for (final ItemStack stack : items) {
            final CompoundTag stackTag = new CompoundTag();
            if (stack != null) {
                stack.save(stackTag);
            }
            itemList.add(stackTag);
        }
        tag.put(TAG_ITEMS, itemList);
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
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
