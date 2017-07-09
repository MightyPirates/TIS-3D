package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * Base implementation of an array based inventory.
 */
public class Inventory implements IInventory {
    private static final String TAG_ITEMS = "inventory";

    private final String name;
    protected final ItemStack[] items;

    public Inventory(final String name, final int size) {
        this.name = name;
        this.items = new ItemStack[size];
    }

    // --------------------------------------------------------------------- //

    public void readFromNBT(final NBTTagCompound nbt) {
        final NBTTagList itemList = nbt.getTagList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        final int count = Math.min(itemList.tagCount(), items.length);
        for (int index = 0; index < count; index++) {
            items[index] = ItemStack.loadItemStackFromNBT(itemList.getCompoundTagAt(index));
        }
    }

    public void writeToNBT(final NBTTagCompound nbt) {
        final NBTTagList itemList = new NBTTagList();
        for (final ItemStack stack : items) {
            final NBTTagCompound stackNbt = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(stackNbt);
            }
            itemList.appendTag(stackNbt);
        }
        nbt.setTag(TAG_ITEMS, itemList);
    }

    // --------------------------------------------------------------------- //

    protected void onItemAdded(final int index) {
    }

    protected void onItemRemoved(final int index) {
    }

    // --------------------------------------------------------------------- //
    // IWorldNameable

    @Override
    public String getInventoryName() {
        return name;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    // --------------------------------------------------------------------- //
    // IInventory

    @Override
    public int getSizeInventory() {
        return items.length;
    }

    @Override
    public ItemStack getStackInSlot(final int index) {
        return items[index];
    }

    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        if (items[index] == null) {
            return null;
        } else if (items[index].stackSize <= count) {
            return getStackInSlotOnClosing(index);
        } else {
            final ItemStack stack = items[index].splitStack(count);
            if (items[index].stackSize < 1) {
                items[index] = null;
            }
            markDirty();
            return stack;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(final int index) {
        final ItemStack stack = items[index];
        setInventorySlotContents(index, null);
        return stack;
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        if (items[index] == stack) {
            return;
        }

        if (items[index] != null) {
            onItemRemoved(index);
        }

        items[index] = stack;

        if (items[index] != null) {
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
    public boolean isUseableByPlayer(final EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return true;
    }
}
