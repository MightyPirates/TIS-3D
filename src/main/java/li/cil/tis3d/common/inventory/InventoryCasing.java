package li.cil.tis3d.common.inventory;

import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.api.module.traits.Rotatable;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageCasingInventory;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/**
 * Inventory implementation for casings, having six slots for modules, one per face.
 */
public final class InventoryCasing extends Inventory implements ISidedInventory {
    private final TileEntityCasing tileEntity;

    public InventoryCasing(final TileEntityCasing tileEntity) {
        super(Constants.NAME_INVENTORY_CASING, Face.VALUES.length);
        this.tileEntity = tileEntity;
    }

    // Copy-paste of parent setInventorySlotContents, but allows passing along module facing.
    public void setInventorySlotContents(final int index, final ItemStack stack, final Port facing) {
        if (items[index] == stack) {
            return;
        }

        if (!items[index].isEmpty()) {
            onItemRemoved(index);
        }

        items[index] = stack;

        if (!items[index].isEmpty()) {
            onItemAdded(index, facing);
        }

        markDirty();
    }

    // --------------------------------------------------------------------- //
    // IInventory

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
        tileEntity.markDirty();
        if (tileEntity.hasWorld() && tileEntity.getWorld().isRemote) {
            // Re-render on client, as module presence changes the block model.
            final IBlockState state = tileEntity.getWorld().getBlockState(tileEntity.getPos());
            tileEntity.getWorld().notifyBlockUpdate(tileEntity.getPos(), state, state, 1);
        }
    }

    // --------------------------------------------------------------------- //
    // ISidedInventory

    @Override
    public int[] getSlotsForFace(final EnumFacing side) {
        return new int[side.ordinal()];
    }

    @Override
    public boolean canInsertItem(final int index, final ItemStack stack, final EnumFacing side) {
        return side.ordinal() == index &&
               getStackInSlot(index).isEmpty() &&
               tileEntity.getModule(Face.fromEnumFacing(side)) == null && // Handles virtual modules.
               canInstall(stack, Face.fromEnumFacing(side));
    }

    @Override
    public boolean canExtractItem(final int index, final ItemStack stack, final EnumFacing side) {
        return side.ordinal() == index && stack == getStackInSlot(index);
    }

    private boolean canInstall(final ItemStack stack, final Face face) {
        return ModuleAPI.getProviderFor(stack, tileEntity, face) != null;
    }

    // --------------------------------------------------------------------- //
    // Inventory

    @Override
    protected void onItemAdded(final int index) {
        onItemAdded(index, Port.UP);
    }

    private void onItemAdded(final int index, final Port facing) {
        final ItemStack stack = getStackInSlot(index);
        if (stack.isEmpty()) {
            return;
        }

        final Face face = Face.VALUES[index];
        final ModuleProvider provider = ModuleAPI.getProviderFor(stack, tileEntity, face);
        if (provider == null) {
            return;
        }

        final Module module = provider.createModule(stack, tileEntity, face);

        if (module instanceof Rotatable) {
            ((Rotatable) module).setFacing(facing);
        }

        if (!tileEntity.getCasingWorld().isRemote) {
            // Grab module data from newly created module, if any, don't rely on stack.
            // Rationale: module may initialize data from stack while contents of stack
            // are not synchronized to client, or do some fancy server-side only setup
            // based on the stack. The possibilities are endless. This is robust.
            final NBTTagCompound moduleData;
            if (module != null) {
                module.onInstalled(stack);
                module.writeToNBT(moduleData = new NBTTagCompound());
            } else {
                moduleData = null;
            }

            Network.INSTANCE.getWrapper().sendToAllAround(new MessageCasingInventory(tileEntity, index, stack, moduleData), Network.getTargetPoint(tileEntity, Network.RANGE_HIGH));
        }

        tileEntity.setModule(Face.VALUES[index], module);
    }

    @Override
    protected void onItemRemoved(final int index) {
        final Face face = Face.VALUES[index];
        final Module module = tileEntity.getModule(face);
        tileEntity.setModule(face, null);
        if (!tileEntity.getCasingWorld().isRemote) {
            if (module != null) {
                module.onUninstalled(getStackInSlot(index));
                module.onDisposed();
            }

            Network.INSTANCE.getWrapper().sendToAllAround(new MessageCasingInventory(tileEntity, index, ItemStack.EMPTY, null), Network.getTargetPoint(tileEntity, Network.RANGE_HIGH));
        }
    }
}
