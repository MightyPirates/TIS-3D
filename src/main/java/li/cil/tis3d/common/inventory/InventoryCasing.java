package li.cil.tis3d.common.inventory;

import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
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

    // --------------------------------------------------------------------- //
    // IInventory

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
        tileEntity.markDirty();
        if (tileEntity.hasWorldObj()) {
            final IBlockState state = tileEntity.getWorld().getBlockState(tileEntity.getPos());
            tileEntity.getWorld().notifyBlockUpdate(tileEntity.getPos(), state, state, 3);
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
                getStackInSlot(index) == null &&
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
        final ItemStack stack = getStackInSlot(index);
        if (stack == null) {
            return;
        }

        final Face face = Face.VALUES[index];
        final ModuleProvider provider = ModuleAPI.getProviderFor(stack, tileEntity, face);
        if (provider == null) {
            return;
        }

        final Module module = provider.createModule(stack, tileEntity, face);
        if (module != null && !tileEntity.getCasingWorld().isRemote) {
            module.onInstalled(stack);
        }
        tileEntity.setModule(Face.VALUES[index], module);
    }

    @Override
    protected void onItemRemoved(final int index) {
        final Face face = Face.VALUES[index];
        final Module module = tileEntity.getModule(face);
        tileEntity.setModule(face, null);
        if (module != null && !tileEntity.getCasingWorld().isRemote) {
            module.onUninstalled(getStackInSlot(index));
            module.onDisposed();
        }
    }
}
