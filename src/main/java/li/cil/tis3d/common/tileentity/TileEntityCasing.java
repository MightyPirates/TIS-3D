package li.cil.tis3d.common.tileentity;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.BlockChangeAware;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.integration.redlogic.ProxyRedLogic;
import li.cil.tis3d.common.integration.redlogic.RedLogicBundledRedstone;
import li.cil.tis3d.common.integration.redlogic.RedLogicConnectable;
import li.cil.tis3d.common.integration.redlogic.RedLogicRedstone;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import li.cil.tis3d.common.inventory.InventoryCasing;
import li.cil.tis3d.common.inventory.SidedInventoryProxy;
import li.cil.tis3d.common.machine.CasingImpl;
import li.cil.tis3d.common.machine.CasingProxy;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageCasingEnabledState;
import li.cil.tis3d.common.network.message.MessageCasingLockedState;
import li.cil.tis3d.common.network.message.MessageReceivingPipeLockedState;
import li.cil.tis3d.util.InventoryUtils;
import li.cil.tis3d.util.OneEightCompat;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Tile entity for casings.
 * <p>
 * Manages modules installed in it and takes care of maintaining network state
 * with other casings (i.e. injects virtual forwarding module in slots between
 * two casing blocks to relay data between the casings).
 * <p>
 * Also takes care of notifying a connected controller if some state changed,
 * so that the controller can re-scan for a multi-block.
 * <p>
 * Casings do not tick. The modules installed in them are driven by a
 * controller (transitively) connected to their casing.
 */
@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.tis3d.common.integration.redlogic.RedLogicBundledRedstone", modid = ProxyRedLogic.MOD_ID),
                         @Optional.Interface(iface = "li.cil.tis3d.common.integration.redlogic.RedLogicConnectable", modid = ProxyRedLogic.MOD_ID),
                         @Optional.Interface(iface = "li.cil.tis3d.common.integration.redlogic.RedLogicRedstone", modid = ProxyRedLogic.MOD_ID)})
public final class TileEntityCasing extends TileEntityComputer implements
                                                               RedLogicConnectable, RedLogicRedstone, RedLogicBundledRedstone,
                                                               SidedInventoryProxy, CasingProxy, InfraredReceiver {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final InventoryCasing inventory = new InventoryCasing(this);
    private final CasingImpl casing = new CasingImpl(this);

    /**
     * Which receiving pipes of this casing are currently locked, per face.
     */
    private final boolean[][] locked = new boolean[6][4];

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_CASING = "casing";
    private static final String TAG_ENABLED = "enabled";
    private static final String TAG_INVENTORY = "inventory";
    private static final String TAG_LOCKED = "closed"; // backwards compat .-. muh ocd

    private TileEntityController controller;
    private boolean isEnabled;
    private boolean redstoneDirty = true;

    // --------------------------------------------------------------------- //
    // Networking

    /**
     * Actual state tracking implementation of enabled state, used in {@link CasingImpl#isEnabled()}.
     *
     * @return whether the casing is currently enabled.
     */
    public boolean isCasingEnabled() {
        return isEnabled;
    }

    /**
     * Used to notify the case that redstone inputs may have changed, which
     * will in turn cause modules implementing {@link Redstone} and/or {@link BundledRedstone}
     * to get notified.
     */
    public void markRedstoneDirty() {
        redstoneDirty = true;
    }

    /**
     * Set whether the specified <em>receiving</em> pipe on the specified face
     * of the casing is locked. A locked pipe will not allow any reads or
     * writes and cause blocking read/write operations to never finish.
     * <p>
     * Useful for forcing adjacent modules to not communicate when they are
     * omnidirectional, such as the redstone module.
     *
     * @param face  the face to set the locked state for.
     * @param port  the port of the receiving pipe to set the locked state for.
     * @param value the locked state to set; <code>true</code> for locked, <code>false</code> for open (default).
     */
    public void setReceivingPipeLocked(final Face face, final Port port, final boolean value) {
        if (isReceivingPipeLocked(face, port) != value) {
            getReceivingPipe(face, port).cancelRead();
            locked[face.ordinal()][port.ordinal()] = value;
            sendReceivingPipeLockedState(face, port);
        }
    }

    /**
     * Get the current locked state of the specified <em>receiving</em> pipe
     * on the specified face of the casing.
     *
     * @param face the face to get the locked state for.
     * @param port the port of the receiving pipe to get the locked state for.
     * @return <code>true</code> if the port is locked; <code>false</code> otherwise.
     */
    public boolean isReceivingPipeLocked(final Face face, final Port port) {
        return locked[face.ordinal()][port.ordinal()];
    }

    /**
     * Place a module stack into the specified slot, immediately setting the
     * module's rotation to the specified facing if it is a {@link li.cil.tis3d.api.module.traits.Rotatable}
     * module.
     *
     * @param index  the slot to place the module into.
     * @param stack  the stack representing the module.
     * @param facing the rotation of the module.
     */
    public void setInventorySlotContents(final int index, final ItemStack stack, final Port facing) {
        inventory.setInventorySlotContents(index, stack, facing);
    }

    // --------------------------------------------------------------------- //
    // Networking

    public TileEntityController getController() {
        return controller;
    }

    public void setController(final TileEntityController controller) {
        this.controller = controller;
    }

    public void scheduleScan() {
        if (getWorldObj().isRemote) {
            return;
        }
        if (getController() != null) {
            getController().scheduleScan();
        } else {
            // If we don't have a controller there either isn't one, or
            // the controller is in an error state. In the latter case we
            // have ot actively look for a controller and notify it.
            final TileEntityController controller = findController();
            if (controller != null) {
                controller.scheduleScan();
            }
        }
    }

    public void onEnabled() {
        if (isEnabled) {
            return;
        }
        isEnabled = true;
        sendState();
        casing.onEnabled();
    }

    public void onDisabled() {
        if (!isEnabled) {
            return;
        }
        isEnabled = false;
        sendState();
        casing.onDisabled();
    }

    public void stepRedstone() {
        if (!redstoneDirty) {
            return;
        }
        redstoneDirty = false;

        for (final Face face : Face.VALUES) {
            final Module module = getCasing().getModule(face);
            if (module instanceof Redstone) {
                final Redstone redstone = (Redstone) module;
                final short signal = (short) RedstoneIntegration.INSTANCE.getRedstoneInput(redstone);
                redstone.setRedstoneInput(signal);
            }

            if (module instanceof BundledRedstone) {
                final BundledRedstone bundledRedstone = (BundledRedstone) module;
                for (int channel = 0; channel < 16; channel++) {
                    final short signal = (short) RedstoneIntegration.INSTANCE.getBundledRedstoneInput(bundledRedstone, channel);
                    bundledRedstone.setBundledRedstoneInput(channel, signal);
                }
            }
        }
    }

    public void stepModules() {
        casing.stepModules();
    }

    public void setModule(final Face face, final Module module) {
        casing.setModule(face, module);
    }

    public void lock(final ItemStack stack) {
        casing.lock(stack);
        sendCasingLockedState();
    }

    public void unlock(final ItemStack stack) {
        if (casing.unlock(stack)) {
            sendCasingLockedState();
        }
    }

    public void notifyModulesOfBlockChange(final Block neighborBlock) {
        for (final Face face : Face.VALUES) {
            final Module module = getModule(face);
            if (module instanceof BlockChangeAware) {
                ((BlockChangeAware) module).onNeighborBlockChange(neighborBlock);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // PipeHost

    @Override
    protected void setNeighbor(final Face face, final TileEntityComputer neighbor) {
        super.setNeighbor(face, neighbor);

        // Ensure there are no modules installed between two casings.
        if (neighbors[face.ordinal()] != null) {
            InventoryUtils.drop(getWorldObj(), xCoord, yCoord, zCoord, this, face.ordinal(), getInventoryStackLimit(), Face.toEnumFacing(face));
        }

        if (neighbor instanceof TileEntityController) {
            // If we have a controller and it's not our controller, tell our
            // controller to do a re-scan (because now we have more than one
            // controller, which is invalid).
            if (getController() != neighbor && getController() != null) {
                getController().scheduleScan();
            }
        }
    }

    @Override
    public void onWriteComplete(final Face sendingFace, final Port sendingPort) {
        super.onWriteComplete(sendingFace, sendingPort);

        final Module module = getModule(sendingFace);
        if (module != null) {
            module.onWriteComplete(sendingPort);
        }
    }

    // --------------------------------------------------------------------- //
    // RedLogicConnectable, RedLogicRedstone, RedLogicBundledRedstone

    @Override
    public TileEntityCasing getTileEntity() {
        return this;
    }

    // --------------------------------------------------------------------- //
    // IInventory

    @Override
    public boolean isUseableByPlayer(final EntityPlayer player) {
        if (worldObj.getTileEntity(getPositionX(), getPositionY(), getPositionZ()) != this) {
            return false;
        }
        final double maxDistance = 64;
        return OneEightCompat.getDistanceSqToCenter(player, getPositionX(), getPositionY(), getPositionZ()) <= maxDistance;
    }

    // --------------------------------------------------------------------- //
    // SidedInventoryProxy

    @Override
    public ISidedInventory getInventory() {
        return inventory;
    }

    // --------------------------------------------------------------------- //
    // CasingProxy

    @Override
    public Casing getCasing() {
        return casing;
    }

    // --------------------------------------------------------------------- //
    // InfraredReceiver

    @Override
    public void onInfraredPacket(final InfraredPacket packet, final MovingObjectPosition hit) {
        final Module module = getModule(Face.fromEnumFacing(EnumFacing.getFront(hit.sideHit)));
        if (module instanceof InfraredReceiver) {
            ((InfraredReceiver) module).onInfraredPacket(packet, hit);
        }
    }

    // --------------------------------------------------------------------- //
    // TileEntity

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!getCasingWorld().isRemote) {
            onDisabled();
        }
        dispose();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        dispose();
    }

    @Override
    public void onDataPacket(final NetworkManager manager, final S35PacketUpdateTileEntity packet) {
        super.onDataPacket(manager, packet);

        final World world = getWorldObj();
        final Chunk chunk = world.getChunkFromBlockCoords(xCoord, zCoord);
        final Block block = world.getBlock(xCoord, yCoord, zCoord);
        world.markAndNotifyBlock(xCoord, yCoord, zCoord, chunk, block, block, 2);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Network.RANGE_HIGH * Network.RANGE_HIGH;
    }

    // --------------------------------------------------------------------- //
    // TileEntityComputer

    @Override
    public Pipe getReceivingPipe(final Face face, final Port port) {
        return isReceivingPipeLocked(face, port) ? LockedPipe.INSTANCE : super.getReceivingPipe(face, port);
    }

    @Override
    protected void readFromNBTForClient(final NBTTagCompound nbt) {
        super.readFromNBTForClient(nbt);

        isEnabled = nbt.getBoolean(TAG_ENABLED);
    }

    @Override
    protected void writeToNBTForClient(final NBTTagCompound nbt) {
        super.writeToNBTForClient(nbt);

        nbt.setBoolean(TAG_ENABLED, isEnabled);
    }

    @Override
    protected void readFromNBTCommon(final NBTTagCompound nbt) {
        super.readFromNBTCommon(nbt);

        decompressClosed(nbt.getByteArray(TAG_LOCKED), locked);

        final NBTTagCompound inventoryNbt = nbt.getCompoundTag(TAG_INVENTORY);
        inventory.readFromNBT(inventoryNbt);

        final NBTTagCompound casingNbt = nbt.getCompoundTag(TAG_CASING);
        casing.readFromNBT(casingNbt);
    }

    @Override
    protected void writeToNBTCommon(final NBTTagCompound nbt) {
        super.writeToNBTCommon(nbt);

        nbt.setByteArray(TAG_LOCKED, compressClosed(locked));

        // Needed on the client also, for picking and for actually instantiating
        // the installed modules on the client side (to find the provider).
        final NBTTagCompound inventoryNbt = new NBTTagCompound();
        inventory.writeToNBT(inventoryNbt);
        nbt.setTag(TAG_INVENTORY, inventoryNbt);

        // Needed on the client also, to allow initializing client side modules
        // immediately after creation.
        final NBTTagCompound casingNbt = new NBTTagCompound();
        casing.writeToNBT(casingNbt);
        nbt.setTag(TAG_CASING, casingNbt);
    }

    // --------------------------------------------------------------------- //
    // Synchronization

    /**
     * Used for synchronizing state between server and client, letting the
     * client know the new locked state of a case for overlay rendering.
     *
     * @param locked the new locked state of the case.
     */
    @SideOnly(Side.CLIENT)
    public void setCasingLockedClient(final boolean locked) {
        casing.setLocked(locked);
    }

    /**
     * Used for synchronizing state between server and client, letting the
     * client know the new item stack installed in a slot, and, if present
     * initialize its module with the original server state of the module.
     *
     * @param slot       the slot the item stack changed in.
     * @param stack      the new item stack in that slot, if any.
     * @param moduleData the original state of the module on the server, if present.
     */
    @SideOnly(Side.CLIENT)
    public void setStackAndModuleClient(final int slot, final ItemStack stack, final NBTTagCompound moduleData) {
        inventory.setInventorySlotContents(slot, stack);
        final Module module = casing.getModule(Face.VALUES[slot]);
        if (module != null) {
            module.readFromNBT(moduleData);
        }
    }

    /**
     * Used for synchronizing state between server and client, letting the
     * client know of the new enabled state of this casing, for rendering.
     *
     * @param value the new enabled state of this casing.
     */
    @SideOnly(Side.CLIENT)
    public void setEnabledClient(final boolean value) {
        isEnabled = value;
    }

    /**
     * Used for synchronizing state between server and client, letting the
     * client know of the new locked state of a port, for overlay rendering.
     *
     * @param face  the face the port belongs to.
     * @param port  the port to set the locked state of.
     * @param value the new enabled state of this casing.
     */
    @SideOnly(Side.CLIENT)
    public void setReceivingPipeLockedClient(final Face face, final Port port, final boolean value) {
        locked[face.ordinal()][port.ordinal()] = value;
    }

    // --------------------------------------------------------------------- //

    private TileEntityController findController() {
        // List of processed tile entities to avoid loops.
        final Set<TileEntity> processed = new HashSet<>();
        // List of pending tile entities that still need to be scanned.
        final Queue<TileEntity> queue = new ArrayDeque<>();

        // Number of casings we encountered for optional early exit.
        int casings = 0;

        // Start at our location, keep going until there's nothing left to do.
        processed.add(this);
        queue.add(this);
        while (!queue.isEmpty()) {
            final TileEntity tileEntity = queue.remove();
            if (tileEntity.isInvalid()) {
                continue;
            }

            // Check what we have. We only add controllers and casings to this list,
            // so we can skip the type check in the else branch.
            if (tileEntity instanceof TileEntityController) {
                return (TileEntityController) tileEntity;
            } else /* if (tileEntity instanceof TileEntityCasing) */ {
                // We only allow a certain number of casings per multi-block, so
                // we can early exit if there are too many (because even if we
                // notified the controller, it'd enter an error state again anyway).
                if (++casings > Settings.maxCasingsPerController) {
                    onDisabled();
                    return null;
                }

                // Keep looking...
                if (!TileEntityController.addNeighbors(getWorldObj(), tileEntity, processed, queue)) {
                    // Hit end of loaded area, so scheduling would just result in
                    // error again anyway. Do *not* disable casings, keep last
                    // known valid state when all parts were loaded.
                    return null;
                }
            }
        }

        // Could not find a controller, disable modules.
        onDisabled();
        return null;
    }

    private void sendState() {
        final MessageCasingEnabledState message = new MessageCasingEnabledState(this, isEnabled);
        Network.INSTANCE.getWrapper().sendToDimension(message, getWorldObj().provider.dimensionId);
    }

    private void dispose() {
        if (getController() != null) {
            getController().scheduleScan();
        }
        casing.onDisposed();
    }

    private void sendCasingLockedState() {
        Network.INSTANCE.getWrapper().sendToAllAround(new MessageCasingLockedState(this, isLocked()), Network.getTargetPoint(this, Network.RANGE_HIGH));
        getWorldObj().playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "random.click", 0.3f, isLocked() ? 0.5f : 0.6f);
    }

    private void sendReceivingPipeLockedState(final Face face, final Port port) {
        Network.INSTANCE.getWrapper().sendToAllAround(new MessageReceivingPipeLockedState(this, face, port, isReceivingPipeLocked(face, port)), Network.getTargetPoint(this, Network.RANGE_HIGH));
        getWorldObj().playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "random.click", 0.3f, isReceivingPipeLocked(face, port) ? 0.5f : 0.6f);
    }

    private void decompressClosed(final byte[] compressed, final boolean[][] decompressed) {
        if (compressed.length != 3) {
            return;
        }

        for (int i = 0; i < 6; i++) {
            int c = compressed[i >> 1] & 0b11111111;
            if ((i & 1) == 1) {
                c >>>= 4;
            }
            final boolean[] ports = decompressed[i];
            for (int j = 0; j < 4; j++) {
                ports[j] = (c & (1 << j)) != 0;
            }
        }
    }

    private byte[] compressClosed(final boolean[][] decompressed) {
        // Cram two faces into one byte (four ports use four bits).
        final byte[] compressed = new byte[3];
        for (int i = 0; i < 6; i++) {
            final boolean[] ports = decompressed[i];
            int c = 0;
            for (int j = 0; j < 4; j++) {
                if (ports[j]) {
                    c |= 1 << j;
                }
            }
            if ((i & 1) == 1) {
                c <<= 4;
            }
            compressed[i >> 1] |= (byte) c;
        }
        return compressed;
    }

    // --------------------------------------------------------------------- //

    /**
     * A pipe that cannot be written to nor read from, effectively locking up
     * blocking reads/writes. Used for locked ports. Since it is immutable, we
     * can use one for all ports on all faces in all casings.
     */
    private static final class LockedPipe implements Pipe {
        public static final Pipe INSTANCE = new LockedPipe();

        @Override
        public void beginWrite(final short value) throws IllegalStateException {
            throw new IllegalStateException("Trying to write to a busy pipe. Check isWriting().");
        }

        @Override
        public void cancelWrite() {
        }

        @Override
        public boolean isWriting() {
            return true;
        }

        @Override
        public void beginRead() throws IllegalStateException {
            throw new IllegalStateException("Trying to write to a busy pipe. Check isReading().");
        }

        @Override
        public void cancelRead() {
        }

        @Override
        public boolean isReading() {
            return true;
        }

        @Override
        public boolean canTransfer() {
            return false;
        }

        @Override
        public short read() throws IllegalStateException {
            throw new IllegalStateException("No data to read. Check canTransfer().");
        }
    }
}
