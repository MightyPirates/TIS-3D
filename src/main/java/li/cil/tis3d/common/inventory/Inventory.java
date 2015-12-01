package li.cil.tis3d.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;

import java.util.AbstractList;

/**
 * Base implementation of an array based inventory.
 */
public class Inventory extends AbstractList<ItemStack> implements IInventory {
    private final String name;
    private final ItemStack[] items;

    public Inventory(final String name, final int size) {
        this.name = name;
        this.items = new ItemStack[size];
    }

    // --------------------------------------------------------------------- //

    protected void onItemAdded(final int index) {
    }

    protected void onItemRemoved(final int index) {
    }

    public void readFromNBT(final NBTTagCompound nbt) {
        final NBTTagList itemList = nbt.getTagList("inventory", Constants.NBT.TAG_COMPOUND);
        final int count = Math.min(itemList.tagCount(), items.length);
        for (int index = 0; index < count; index++) {
            setInventorySlotContents(index, ItemStack.loadItemStackFromNBT(itemList.getCompoundTagAt(index)));
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
        nbt.setTag("inventory", itemList);
    }

    // --------------------------------------------------------------------- //
    // IWorldNameable

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public IChatComponent getDisplayName() {
        return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
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
            return removeStackFromSlot(index);
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
    public ItemStack removeStackFromSlot(final int index) {
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
    public void openInventory(final EntityPlayer player) {
    }

    @Override
    public void closeInventory(final EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return true;
    }

    @Override
    public int getField(final int id) {
        return 0;
    }

    @Override
    public void setField(final int id, final int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    // --------------------------------------------------------------------- //
    // List

    @Override
    public ItemStack get(final int index) {
        return getStackInSlot(index);
    }

    @Override
    public int size() {
        return getSizeInventory();
    }

    @Override
    public ItemStack set(final int index, final ItemStack element) {
        final ItemStack oldStack = get(index);
        setInventorySlotContents(index, element);
        return oldStack;
    }
}
