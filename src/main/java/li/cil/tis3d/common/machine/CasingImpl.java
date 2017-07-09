package li.cil.tis3d.common.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of a {@link Casing}, holding up to six {@link Module}s.
 */
public final class CasingImpl implements Casing {
    // --------------------------------------------------------------------- //
    // Persisted data.

    /**
     * The {@link Module}s currently installed in this {@link Casing}.
     */
    private final Module[] modules = new Module[Face.VALUES.length];

    /**
     * The key the casing is currently locked with. If this is set, players
     * cannot add or remove modules from the casing. A key with the correct
     * UUID in its NBT tag is required to unlock a casing.
     */
    private UUID lock = null;

    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_MODULES = "modules";
    private static final String TAG_KEY_MS = "keyMostSignificant";
    private static final String TAG_KEY_LS = "keyLeastSignificant";

    /**
     * The tile entity hosting this casing.
     */
    private final TileEntityCasing tileEntity;

    // --------------------------------------------------------------------- //

    public CasingImpl(final TileEntityCasing tileEntity) {
        this.tileEntity = tileEntity;
    }

    /**
     * Calls {@link Module#onEnabled()} on all modules.
     * <p>
     * Used by the controller when its state changes to {@link TileEntityController.ControllerState#RUNNING}.
     */
    public void onEnabled() {
        for (final Module module : modules) {
            if (module != null) {
                module.onEnabled();
            }
        }
        markDirty();
    }

    /**
     * Calls {@link Module#onDisabled()} on all modules and resets all pipes.
     * <p>
     * Used by the controller when its state changes from {@link TileEntityController.ControllerState#RUNNING},
     * or the controller is reset (scan scheduled), or the controller is unloaded.
     */
    public void onDisabled() {
        for (final Module module : modules) {
            if (module != null) {
                module.onDisabled();
            }
        }
        for (final Pipe pipe : tileEntity.getPipes()) {
            pipe.cancelRead();
            pipe.cancelWrite();
        }
        markDirty();
    }

    /**
     * Calls {@link Module#onDisposed()} on all modules.
     * <p>
     * Used by the casing when it is being unloaded.
     */
    public void onDisposed() {
        for (final Module module : modules) {
            if (module != null) {
                module.onDisposed();
            }
        }
    }

    /**
     * Advance the logic of all modules by calling {@link Module#step()} on them.
     */
    public void stepModules() {
        for (final Module module : modules) {
            if (module != null) {
                module.step();
            }
        }
    }

    /**
     * Set the module for the specified face of the casing.
     * <p>
     * This is automatically called by the casing tile entity when items are
     * added or removed and as a special case directly for forwarder modules.
     * <p>
     * This calls {@link Module#onEnabled()} and {@link Module#onDisabled()}
     * appropriately if the casing is enabled or disabled, respectively.
     *
     * @param face   the face to install the module on.
     * @param module the module to install on the face, or <tt>null</tt> for none.
     */
    public void setModule(final Face face, final Module module) {
        if (getModule(face) == module) {
            return;
        }

        // End-of-life notification for module if it was active.
        final Module oldModule = getModule(face);
        if (tileEntity.isEnabled() && oldModule != null && getCasingWorld() != null && !getCasingWorld().isRemote) {
            oldModule.onDisabled();
        }

        // Remember for below.
        final boolean hadRedstone = oldModule instanceof Redstone;

        // Apply new module before adjust remaining state.
        modules[face.ordinal()] = module;

        // Reset redstone output if the previous module was redstone capable.
        if (hadRedstone) {
            if (!getCasingWorld().isRemote) {
                tileEntity.markDirty();
                getCasingWorld().notifyBlocksOfNeighborChange(getPositionX(), getPositionY(), getPositionZ(), tileEntity.getBlockType());
            }
        }

        // Reset pipe state if module is removed. Don't reset when one is set,
        // because it might be set via a load or scan, in which case we
        // absolutely do not want to reset our state!
        if (module == null) {
            for (final Port port : Port.VALUES) {
                getReceivingPipe(face, port).cancelRead();
                getSendingPipe(face, port).cancelWrite();
            }
        }

        // Activate the module if the controller is active.
        if (tileEntity.isEnabled() && module != null && getCasingWorld() != null && !getCasingWorld().isRemote) {
            module.onEnabled();
        }

        tileEntity.markDirty();
    }

    @SideOnly(Side.CLIENT)
    public void setLocked(final boolean locked) {
        if (locked) {
            lock = UUID.randomUUID();
        } else {
            lock = null;
        }
    }

    /**
     * Locks the casing and returns the key for unlocking it.
     *
     * @param stack the item to store the key for unlocking on.
     * @throws IllegalStateException if the casing is already locked.
     */
    public void lock(final ItemStack stack) {
        if (isLocked()) {
            throw new IllegalStateException("Casing is already locked.");
        }
        if (Items.isKeyCreative(stack)) {
            lock = UUID.randomUUID();
        } else {
            final UUID key = getKeyFromStack(stack).orElse(UUID.randomUUID());
            setKeyForStack(stack, key);
            lock = key;
        }
    }

    /**
     * Try to unlock the casing with the key stored on the specified item.
     *
     * @param stack the item containing the key.
     * @return <code>true</code> if the casing was successfully unlocked; <code>false</code> otherwise.
     */
    public boolean unlock(final ItemStack stack) {
        if (Items.isKeyCreative(stack)) {
            lock = null;
            return true;
        } else {
            return getKeyFromStack(stack).map(this::unlock).orElse(false);
        }
    }

    /**
     * Try to unlock the casing with the specified key.
     *
     * @param key the key to use to unlock the casing.
     * @return <code>true</code> if the casing was successfully unlocked; <code>false</code> otherwise.
     */
    public boolean unlock(final UUID key) {
        if (key.equals(lock)) {
            lock = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Restore data of all modules and pipes from the specified NBT tag.
     *
     * @param nbt the data to load.
     */
    public void readFromNBT(final NBTTagCompound nbt) {
        for (int index = 0; index < tileEntity.getSizeInventory(); index++) {
            final ItemStack stack = tileEntity.getStackInSlot(index);
            if (stack == null) {
                if (modules[index] != null) {
                    modules[index].onDisposed();
                }
                modules[index] = null;
                continue;
            }

            final Face face = Face.VALUES[index];
            final ModuleProvider provider = ModuleAPI.getProviderFor(stack, tileEntity, face);
            if (provider == null) {
                if (modules[index] != null) {
                    modules[index].onDisposed();
                }
                modules[index] = null;
                continue;
            }

            final Module module = provider.createModule(stack, tileEntity, face);
            modules[index] = module;
        }

        final NBTTagList modulesNbt = nbt.getTagList(TAG_MODULES, Constants.NBT.TAG_COMPOUND);
        final int moduleCount = Math.min(modulesNbt.tagCount(), modules.length);
        for (int i = 0; i < moduleCount; i++) {
            if (modules[i] != null) {
                modules[i].readFromNBT(modulesNbt.getCompoundTagAt(i));
            }
        }

        if (nbt.hasKey(TAG_KEY_MS) && nbt.hasKey(TAG_KEY_LS)) {
            lock = new UUID(nbt.getLong(TAG_KEY_MS), nbt.getLong(TAG_KEY_LS));
        } else {
            lock = null;
        }
    }

    /**
     * Write the state of all modules and pipes to the specified NBT tag.
     *
     * @param nbt the tag to write the data to.
     */
    public void writeToNBT(final NBTTagCompound nbt) {
        final NBTTagList modulesNbt = new NBTTagList();
        for (final Module module : modules) {
            final NBTTagCompound moduleNbt = new NBTTagCompound();
            if (module != null) {
                module.writeToNBT(moduleNbt);
            }
            modulesNbt.appendTag(moduleNbt);
        }
        nbt.setTag(TAG_MODULES, modulesNbt);

        if (lock != null) {
            nbt.setLong(TAG_KEY_MS, lock.getMostSignificantBits());
            nbt.setLong(TAG_KEY_LS, lock.getLeastSignificantBits());
        }
    }

    // --------------------------------------------------------------------- //
    // Casing

    @Override
    public World getCasingWorld() {
        return tileEntity.getWorldObj();
    }

    @Override
    public int getPositionX() {
        return tileEntity.xCoord;
    }

    @Override
    public int getPositionY() {
        return tileEntity.yCoord;
    }

    @Override
    public int getPositionZ() {
        return tileEntity.zCoord;
    }

    @Override
    public void markDirty() {
        tileEntity.markDirty();
    }

    @Override
    public boolean isEnabled() {
        return tileEntity.isCasingEnabled();
    }

    @Override
    public boolean isLocked() {
        return lock != null;
    }

    @Override
    public Module getModule(final Face face) {
        return modules[face.ordinal()];
    }

    @Override
    public Pipe getReceivingPipe(final Face face, final Port port) {
        return tileEntity.getReceivingPipe(face, port);
    }

    @Override
    public Pipe getSendingPipe(final Face face, final Port port) {
        return tileEntity.getSendingPipe(face, port);
    }

    @Override
    public void sendData(final Face face, final NBTTagCompound data, final byte type) {
        Network.sendModuleData(this, face, data, type);
    }

    @Override
    public void sendData(final Face face, final NBTTagCompound data) {
        sendData(face, data, (byte) -1);
    }

    @Override
    public void sendData(final Face face, final ByteBuf data, final byte type) {
        Network.sendModuleData(this, face, data, type);
    }

    @Override
    public void sendData(final Face face, final ByteBuf data) {
        sendData(face, data, (byte) -1);
    }

    // --------------------------------------------------------------------- //

    /**
     * Read a stored key from the specified stack.
     *
     * @param stack the stack to get the key from.
     * @return the key, if present.
     */
    private static Optional<UUID> getKeyFromStack(final ItemStack stack) {
        final NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            return Optional.empty();
        }
        if (!nbt.hasKey(TAG_KEY_MS) || !nbt.hasKey(TAG_KEY_LS)) {
            return Optional.empty();
        }
        return Optional.of(new UUID(nbt.getLong(TAG_KEY_MS), nbt.getLong(TAG_KEY_LS)));
    }

    /**
     * Store the specified key on the specified item stack.
     *
     * @param stack the stack to store the key on.
     * @param key   the key to store on the stack.
     */
    private static void setKeyForStack(final ItemStack stack, final UUID key) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            stack.setTagCompound(nbt = new NBTTagCompound());
        }
        nbt.setLong(TAG_KEY_MS, key.getMostSignificantBits());
        nbt.setLong(TAG_KEY_LS, key.getLeastSignificantBits());
    }
}
