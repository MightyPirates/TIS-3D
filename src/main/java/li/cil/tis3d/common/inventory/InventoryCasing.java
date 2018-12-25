package li.cil.tis3d.common.inventory;

import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.api.module.traits.Rotatable;
import li.cil.tis3d.charset.PacketRegistry;
import li.cil.tis3d.charset.PacketServerHelper;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.network.message.MessageCasingInventory;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.math.Direction;

/**
 * Inventory implementation for casings, having six slots for modules, one per face.
 */
public final class InventoryCasing extends Inventory implements SidedInventory {
    private final TileEntityCasing tileEntity;

    public InventoryCasing(final TileEntityCasing tileEntity) {
        super(new TranslatableTextComponent(Constants.NAME_INVENTORY_CASING), Face.VALUES.length);
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
    public int getInvMaxStackAmount() {
        return 1;
    }

    @Override
    public void markDirty() {
        final BlockState state = tileEntity.getWorld().getBlockState(tileEntity.getPos());
        Blocks.casing.updateBlockState(state, tileEntity.getWorld(), tileEntity.getPos());
        tileEntity.markDirty();
        if (tileEntity.hasWorld() && tileEntity.getWorld().isClient) {
            // Re-render on client, as module presence changes the block model.
            tileEntity.getWorld().scheduleBlockRender(tileEntity.getPos());
        }
    }

    // --------------------------------------------------------------------- //
    // ISidedInventory

    @Override
    public int[] getInvAvailableSlots(final Direction side) {
        return new int[side.ordinal()];
    }

    @Override
    public boolean canInsertInvStack(final int index, final ItemStack stack, final Direction side) {
        return side.ordinal() == index &&
            getInvStack(index).isEmpty() &&
            tileEntity.getModule(Face.fromEnumFacing(side)) == null && // Handles virtual modules.
            canInstall(stack, Face.fromEnumFacing(side));
    }

    @Override
    public boolean canExtractInvStack(final int index, final ItemStack stack, final Direction side) {
        return side.ordinal() == index && stack == getInvStack(index);
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
        final ItemStack stack = getInvStack(index);
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

        if (!tileEntity.getCasingWorld().isClient) {
            // Grab module data from newly created module, if any, don't rely on stack.
            // Rationale: module may initialize data from stack while contents of stack
            // are not synchronized to client, or do some fancy server-side only setup
            // based on the stack. The possibilities are endless. This is robust.
            final CompoundTag moduleData;
            if (module != null) {
                module.onInstalled(stack);
                module.writeToNBT(moduleData = new CompoundTag());
            } else {
                moduleData = null;
            }

            Packet packet = PacketRegistry.SERVER.wrap(new MessageCasingInventory(tileEntity, index, stack, moduleData));
            PacketServerHelper.forEachWatching(tileEntity.getCasingWorld(), tileEntity.getPos(), (player) -> {
                player.networkHandler.sendPacket(packet);
            });
        }

        tileEntity.setModule(Face.VALUES[index], module);
    }

    @Override
    protected void onItemRemoved(final int index) {
        final Face face = Face.VALUES[index];
        final Module module = tileEntity.getModule(face);
        tileEntity.setModule(face, null);
        if (!tileEntity.getCasingWorld().isClient) {
            if (module != null) {
                module.onUninstalled(getInvStack(index));
                module.onDisposed();
            }

            Packet packet = PacketRegistry.SERVER.wrap(new MessageCasingInventory(tileEntity, index, ItemStack.EMPTY, null));
            PacketServerHelper.forEachWatching(tileEntity.getCasingWorld(), tileEntity.getPos(), (player) -> {
                player.networkHandler.sendPacket(packet);
            });
        }
    }
}
