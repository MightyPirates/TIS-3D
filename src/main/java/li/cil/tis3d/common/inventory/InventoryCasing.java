package li.cil.tis3d.common.inventory;

import li.cil.tis3d.Constants;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.tile.TileEntityCasing;
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
        if (tileEntity.getWorld() != null) {
            tileEntity.getWorld().markBlockForUpdate(tileEntity.getPos());
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

        tileEntity.setModule(Face.VALUES[index], provider.createModule(stack, tileEntity, face));
    }

    @Override
    protected void onItemRemoved(final int index) {
        tileEntity.setModule(Face.VALUES[index], null);
    }
}
