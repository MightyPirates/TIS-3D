package li.cil.tis3d.common.inventory;

import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.api.module.traits.Rotatable;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.CasingInventoryMessage;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Inventory implementation for casings, having six slots for modules, one per face.
 */
public final class CasingInventory extends ArrayInventory implements SidedInventory {
    private final CasingBlockEntity blockEntity;

    public CasingInventory(final CasingBlockEntity blockEntity) {
        super(Face.VALUES.length);
        this.blockEntity = blockEntity;
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
    // Inventory

    @Override
    public int getInvMaxStackAmount() {
        return 1;
    }

    @Override
    public void markDirty() {
        final World world = Objects.requireNonNull(blockEntity.getWorld());

        final BlockState state = world.getBlockState(blockEntity.getPos());
        final BlockState newState = Blocks.CASING.updateBlockState(state, world, blockEntity.getPos());
        blockEntity.markDirty();
        if (world.isClient) {
            // Re-render on client, as module presence changes the block model.
            world.checkBlockRerender(blockEntity.getPos(), state, newState);
        }
    }

    // --------------------------------------------------------------------- //
    // SidedInventory

    @Override
    public int[] getInvAvailableSlots(final Direction side) {
        return new int[side.ordinal()];
    }

    @Override
    public boolean canInsertInvStack(final int index, final ItemStack stack, @Nullable final Direction side) {
        return side != null && side.ordinal() == index &&
            getInvStack(index).isEmpty() &&
            blockEntity.getModule(Face.fromDirection(side)) == null && // Handles virtual modules.
            canInstall(stack, Face.fromDirection(side));
    }

    @Override
    public boolean canExtractInvStack(final int index, final ItemStack stack, final Direction side) {
        return side.ordinal() == index && stack == getInvStack(index);
    }

    private boolean canInstall(final ItemStack stack, final Face face) {
        return ModuleAPI.getProviderFor(stack, blockEntity, face) != null;
    }

    // --------------------------------------------------------------------- //
    // Inventory

    @Override
    protected void onItemAdded(final int index) {
        onItemAdded(index, Port.UP);
    }

    private void onItemAdded(final int index, final Port facing) {
        final World world = Objects.requireNonNull(blockEntity.getWorld());

        final ItemStack stack = getInvStack(index);
        if (stack.isEmpty()) {
            return;
        }

        final Face face = Face.VALUES[index];
        final ModuleProvider provider = ModuleAPI.getProviderFor(stack, blockEntity, face);
        if (provider == null) {
            return;
        }

        final Module module = provider.createModule(stack, blockEntity, face);

        if (module instanceof Rotatable) {
            ((Rotatable)module).setFacing(facing);
        }

        if (!world.isClient) {
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

            final CasingInventoryMessage message = new CasingInventoryMessage(blockEntity, index, stack, moduleData);
            Network.INSTANCE.sendToClientsNearLocation(message, world, blockEntity.getPosition(), Network.RANGE_HIGH);
        }

        blockEntity.setModule(Face.VALUES[index], module);
    }

    @Override
    protected void onItemRemoved(final int index) {
        final World world = Objects.requireNonNull(blockEntity.getWorld());

        final Face face = Face.VALUES[index];
        final Module module = blockEntity.getModule(face);
        blockEntity.setModule(face, null);
        if (!world.isClient) {
            if (module != null) {
                module.onUninstalled(getInvStack(index));
                module.onDisposed();
            }

            final CasingInventoryMessage message = new CasingInventoryMessage(blockEntity, index, ItemStack.EMPTY, null);
            Network.INSTANCE.sendToClientsNearLocation(message, world, blockEntity.getPosition(), Network.RANGE_HIGH);
        }
    }
}
