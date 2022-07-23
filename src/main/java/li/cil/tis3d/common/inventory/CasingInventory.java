package li.cil.tis3d.common.inventory;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.api.module.traits.ModuleWithRotation;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.CasingInventoryMessage;
import li.cil.tis3d.common.provider.ModuleProviders;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Inventory implementation for casings, having six slots for modules, one per face.
 */
public final class CasingInventory extends Inventory implements WorldlyContainer {
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

        setChanged();
    }

    // --------------------------------------------------------------------- //
    // IInventory

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
        final Level level = blockEntity.getBlockEntityLevel();
        if (!level.isClientSide()) {
            BlockState state = blockEntity.getBlockState();
            for (final Face face : Face.VALUES) {
                final BooleanProperty property = CasingBlock.FACE_TO_PROPERTY.get(face);
                state = state.setValue(property, !items[face.ordinal()].isEmpty());
            }
            level.setBlockAndUpdate(blockEntity.getBlockPos(), state);
        }
    }

    // --------------------------------------------------------------------- //
    // ISidedInventory

    @Override
    public int[] getSlotsForFace(final Direction side) {
        return new int[side.ordinal()];
    }

    @Override
    public boolean canPlaceItemThroughFace(final int index, final ItemStack stack, @Nullable final Direction side) {
        return side != null && side.ordinal() == index &&
               getItem(index).isEmpty() &&
               blockEntity.getModule(Face.fromDirection(side)) == null && // Handles virtual modules.
               canInstall(stack, Face.fromDirection(side));
    }

    @Override
    public boolean canTakeItemThroughFace(final int index, final ItemStack stack, final Direction side) {
        return side.ordinal() == index && stack == getItem(index);
    }

    private boolean canInstall(final ItemStack stack, final Face face) {
        return ModuleProviders.getProviderFor(stack, blockEntity, face).isPresent();
    }

    // --------------------------------------------------------------------- //
    // Inventory

    @Override
    protected void onItemAdded(final int index) {
        onItemAdded(index, Port.UP);
    }

    private void onItemAdded(final int index, final Port facing) {
        final ItemStack stack = getItem(index);
        if (stack.isEmpty()) {
            return;
        }

        final Face face = Face.VALUES[index];
        final Optional<ModuleProvider> provider = ModuleProviders.getProviderFor(stack, blockEntity, face);
        if (!provider.isPresent()) {
            return;
        }

        final Module module = provider.get().createModule(stack, blockEntity, face);

        if (module instanceof ModuleWithRotation) {
            ((ModuleWithRotation) module).setFacing(facing);
        }

        if (!blockEntity.getCasingLevel().isClientSide()) {
            // Grab module data from newly created module, if any, don't rely on stack.
            // Rationale: module may initialize data from stack while contents of stack
            // are not synchronized to client, or do some fancy server-side only setup
            // based on the stack. The possibilities are endless. This is robust.
            final CompoundTag moduleData;
            if (module != null) {
                module.onInstalled(stack);
                module.save(moduleData = new CompoundTag());
            } else {
                moduleData = null;
            }

            final CasingInventoryMessage message = new CasingInventoryMessage(blockEntity, index, stack, moduleData);
            Network.INSTANCE.send(Network.getTracking(blockEntity), message);
        }

        blockEntity.setModule(Face.VALUES[index], module);
    }

    @Override
    protected void onItemRemoved(final int index) {
        final Face face = Face.VALUES[index];
        final Module module = blockEntity.getModule(face);
        blockEntity.setModule(face, null);
        if (!blockEntity.getCasingLevel().isClientSide()) {
            if (module != null) {
                module.onUninstalled(getItem(index));
                module.onDisposed();
            }

            final CasingInventoryMessage message = new CasingInventoryMessage(blockEntity, index, ItemStack.EMPTY, null);
            Network.INSTANCE.send(Network.getTracking(blockEntity), message);
        } else {
            if (module != null) {
                module.onDisposed();
            }
        }
    }
}
