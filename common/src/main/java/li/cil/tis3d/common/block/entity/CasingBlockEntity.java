package li.cil.tis3d.common.block.entity;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.ModuleWithBlockChangeListener;
import li.cil.tis3d.api.module.traits.ModuleWithRedstone;
import li.cil.tis3d.api.module.traits.ModuleWithRotation;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.inventory.CasingInventory;
import li.cil.tis3d.common.inventory.SidedInventoryProxy;
import li.cil.tis3d.common.machine.CasingImpl;
import li.cil.tis3d.common.machine.CasingProxy;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.CasingEnabledStateMessage;
import li.cil.tis3d.common.network.message.CasingLockedStateMessage;
import li.cil.tis3d.common.network.message.ClientCasingLoadedMessage;
import li.cil.tis3d.common.network.message.ReceivingPipeLockedStateMessage;
import li.cil.tis3d.common.provider.RedstoneInputProviders;
import li.cil.tis3d.util.InventoryUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;

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
public final class CasingBlockEntity extends ComputerBlockEntity implements SidedInventoryProxy, CasingProxy, InfraredReceiver {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final CasingInventory inventory = new CasingInventory(this);
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
    private static final String TAG_LOCKED = "locked";

    private ControllerBlockEntity controller;
    private boolean isEnabled;
    private boolean redstoneDirty = true;

    // --------------------------------------------------------------------- //

    public CasingBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.CASING.get(), pos, state);
    }

    @ApiStatus.Internal
    public void dispose() {
        if (getController() != null) {
            getController().scheduleScan();
        }
        casing.onDisposed();
    }

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
     * will in turn cause modules implementing {@link ModuleWithRedstone} to get notified.
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
     * module's rotation to the specified facing if it is a {@link ModuleWithRotation}
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

    @Nullable
    public ControllerBlockEntity getController() {
        return controller;
    }

    public void setController(@Nullable final ControllerBlockEntity controller) {
        this.controller = controller;
    }

    public void scheduleScan() {
        if (getBlockEntityLevel().isClientSide()) {
            return;
        }
        if (getController() != null) {
            getController().scheduleScan();
        } else {
            // If we don't have a controller there either isn't one, or
            // the controller is in an error state. In the latter case we
            // have ot actively look for a controller and notify it.
            final ControllerBlockEntity controller = findController();
            if (controller != null) {
                controller.scheduleScan();
            }
        }
    }

    public void setModule(final Face face, @Nullable final Module module) {
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

    public void notifyModulesOfBlockChange(final BlockPos neighborPos) {
        for (final Face face : Face.VALUES) {
            final Module module = getModule(face);
            if (module instanceof final ModuleWithBlockChangeListener listener) {
                final BlockPos moduleNeighborPos = getPosition().relative(Face.toDirection(face));
                final boolean isModuleNeighbor = Objects.equals(neighborPos, moduleNeighborPos);
                listener.onNeighborBlockChange(neighborPos, isModuleNeighbor);
            }
        }
    }

    void onEnabled() {
        if (isEnabled) {
            return;
        }
        isEnabled = true;
        sendState();
        casing.onEnabled();
    }

    void onDisabled() {
        if (!isEnabled) {
            return;
        }
        isEnabled = false;
        sendState();
        casing.onDisabled();
        markRedstoneDirty();
    }

    void stepRedstone() {
        if (!redstoneDirty) {
            return;
        }
        redstoneDirty = false;

        for (final Face face : Face.VALUES) {
            final Module module = getCasing().getModule(face);
            if (module instanceof final ModuleWithRedstone redstoneModule) {
                final short signal = (short) RedstoneInputProviders.getRedstoneInput(module);
                redstoneModule.setRedstoneInput(signal);
            }
        }
    }

    void stepModules() {
        casing.stepModules();
    }

    // --------------------------------------------------------------------- //
    // PipeHost

    @Override
    protected void setNeighbor(final Face face, @Nullable final ComputerBlockEntity neighbor) {
        super.setNeighbor(face, neighbor);

        // Ensure there are no modules installed between two casings.
        if (hasNeighbor(face)) {
            InventoryUtils.drop(getBlockEntityLevel(), getBlockPos(), this, face.ordinal(), getMaxStackSize(), Face.toDirection(face));
        }

        if (neighbor instanceof final ControllerBlockEntity neighborController) {
            // If we have a controller, and it's not our controller, tell our
            // controller to do a re-scan (because now we have more than one
            // controller, which is invalid). The other one will scan anyway.
            if (getController() != neighborController && getController() != null) {
                getController().scheduleScan();
            }
        }
    }

    @Override
    public void onBeforeWriteComplete(final Face sendingFace, final Port sendingPort) {
        super.onBeforeWriteComplete(sendingFace, sendingPort);

        final Module module = getModule(sendingFace);
        if (module != null) {
            module.onBeforeWriteComplete(sendingPort);
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
    // IInventory

    @Override
    public boolean stillValid(final Player player) {
        if (getBlockEntityLevel().getBlockEntity(getBlockPos()) != this) {
            return false;
        }

        return getBlockPos().closerToCenterThan(player.position(), 8);
    }

    // --------------------------------------------------------------------- //
    // SidedInventoryProxy

    @Override
    public WorldlyContainer getInventory() {
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
    public void onInfraredPacket(final InfraredPacket packet, final HitResult hit) {
        if (hit instanceof final BlockHitResult blockHit) {
            final var module = getModule(Face.fromDirection(blockHit.getDirection()));
            if (module instanceof final InfraredReceiver receiver) {
                receiver.onInfraredPacket(packet, hit);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // BlockEntity

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (!getBlockEntityLevel().isClientSide()) {
            onDisabled();
        }

        dispose();
    }

    // --------------------------------------------------------------------- //
    // BlockEntityComputer

    @Override
    public Pipe getReceivingPipe(final Face face, final Port port) {
        return isReceivingPipeLocked(face, port) ? LockedPipe.INSTANCE : super.getReceivingPipe(face, port);
    }

    @Override
    protected void loadClient(final CompoundTag tag) {
        super.loadClient(tag);

        isEnabled = tag.getBoolean(TAG_ENABLED);

        // This is a bit of a hack, but I can't find a better solution for now.
        //
        // The issue is that we can run into race conditions between Minecraft
        // initializing the casing block entity and modules in the server block
        // entity sending messages to tracking players - or, more generally,
        // between Minecraft's network package stream and our mod package stream.
        // For example:
        // - Server
        //   - Module enqueues data packet.
        //   - Minecraft adds player to server world.
        //   - Minecraft calls getUpdateTag for player.
        //   - Module send queue is processed.  It does not appear
        //     to make a difference when this is called precisely,
        //     flushing our queues to attempt sending before MC sends
        //     the update tag does not make a difference.  Data packet
        //     from before getUpdateTag is sent to player the update tag
        //     is for...
        // - Client
        //   - Minecraft loads data from update tag.
        //   - Minecraft processes our network messages.
        //   - Messages sent before server serialization of module are received
        //     by module, potentially resulting in duplicated data, as is the
        //     case for the terminal module, for example.
        //
        // Therefore, to ensure modules are in a consistent state on the client, we
        // re-request the state of the module to be sent over the mod network channel
        // after the client is ready for this.  We still send this data in the update
        // packet, to ensure minimal latency.  At best, this will result in a very
        // short timeframe of incorrect module data on the client.
        //
        // The downside of this approach is that we send the module data more than
        // once, which is a bit of a waste of bandwidth, but as it only occurs when
        // players load the modules initially, this should be acceptable.
        Network.sendToServer(new ClientCasingLoadedMessage(this));
    }

    @Override
    protected void saveClient(final CompoundTag tag) {
        super.saveClient(tag);

        tag.putBoolean(TAG_ENABLED, isEnabled);
    }

    @Override
    protected void loadCommon(final CompoundTag tag) {
        super.loadCommon(tag);

        decompressClosed(tag.getByteArray(TAG_LOCKED), locked);

        final CompoundTag inventoryTag = tag.getCompound(TAG_INVENTORY);
        inventory.load(inventoryTag);

        final CompoundTag casingTag = tag.getCompound(TAG_CASING);
        casing.load(casingTag);
    }

    @Override
    protected void saveCommon(final CompoundTag tag) {
        super.saveCommon(tag);

        tag.putByteArray(TAG_LOCKED, compressClosed(locked));

        // Needed on the client also, for picking and for actually instantiating
        // the installed modules on the client side (to find the provider).
        final CompoundTag inventoryTag = new CompoundTag();
        inventory.save(inventoryTag);
        tag.put(TAG_INVENTORY, inventoryTag);

        // Needed on the client also, to allow initializing client side modules
        // immediately after creation.
        final CompoundTag casingTag = new CompoundTag();
        casing.save(casingTag);
        tag.put(TAG_CASING, casingTag);
    }

    // --------------------------------------------------------------------- //
    // Synchronization

    /**
     * Used for synchronizing state between server and client, letting the
     * client know the new locked state of a case for overlay rendering.
     *
     * @param locked the new locked state of the case.
     */
    @Environment(EnvType.CLIENT)
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
    @Environment(EnvType.CLIENT)
    public void setStackAndModuleClient(final int slot, final ItemStack stack, final CompoundTag moduleData) {
        inventory.setItem(slot, stack);
        final Module module = casing.getModule(Face.VALUES[slot]);
        if (module != null) {
            module.load(moduleData);
        }
    }

    /**
     * Used for synchronizing state between server and client, letting the
     * client know of the new enabled state of this casing, for rendering.
     *
     * @param value the new enabled state of this casing.
     */
    @Environment(EnvType.CLIENT)
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
    @Environment(EnvType.CLIENT)
    public void setReceivingPipeLockedClient(final Face face, final Port port, final boolean value) {
        locked[face.ordinal()][port.ordinal()] = value;
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private ControllerBlockEntity findController() {
        final Level level = getBlockEntityLevel();

        // List of processed tile entities to avoid loops.
        final Set<BlockEntity> processed = new HashSet<>();
        // List of pending tile entities that still need to be scanned.
        final Queue<BlockEntity> queue = new ArrayDeque<>();

        // Number of casings we encountered for optional early exit.
        int casings = 0;

        // Start at our location, keep going until there's nothing left to do.
        processed.add(this);
        queue.add(this);
        while (!queue.isEmpty()) {
            final BlockEntity blockEntity = queue.remove();
            if (blockEntity.isRemoved()) {
                continue;
            }

            // Check what we have. We only add controllers and casings to this list,
            // so we can skip the type check in the else branch.
            if (blockEntity instanceof final ControllerBlockEntity foundController) {
                return foundController;
            } else {
                assert blockEntity instanceof CasingBlockEntity;
                // We only allow a certain number of casings per multi-block, so
                // we can early exit if there are too many (because even if we
                // notified the controller, it'd enter an error state again anyway).
                if (++casings > CommonConfig.maxCasingsPerController) {
                    onDisabled();
                    return null;
                }

                // Keep looking...
                if (!ControllerBlockEntity.addNeighbors(level, blockEntity, processed, queue)) {
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
        final CasingEnabledStateMessage message = new CasingEnabledStateMessage(this, isEnabled);
        Network.sendToTrackingPlayers(this, message);
    }

    private void sendCasingLockedState() {
        final CasingLockedStateMessage message = new CasingLockedStateMessage(this, isLocked());
        Network.sendToTrackingPlayers(this, message);

        getBlockEntityLevel().playSound(null, getBlockPos(),
            SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, isLocked() ? 0.5f : 0.6f);
    }

    private void sendReceivingPipeLockedState(final Face face, final Port port) {
        final ReceivingPipeLockedStateMessage message = new ReceivingPipeLockedStateMessage(this, face, port, isReceivingPipeLocked(face, port));
        Network.sendToTrackingPlayers(this, message);

        getBlockEntityLevel().playSound(null, getBlockPos(),
            SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, isReceivingPipeLocked(face, port) ? 0.5f : 0.6f);
    }

    private static void decompressClosed(final byte[] compressed, final boolean[][] decompressed) {
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

    private static byte[] compressClosed(final boolean[][] decompressed) {
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
