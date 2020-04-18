package li.cil.tis3d.common.module;

import li.cil.tis3d.api.SerialAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.BlockChangeAware;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.serial.SerialInterface;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * The stack module can be used to store a number of values to be retrieved
 * later on. It operates as LIFO queue, providing the top element to all ports
 * but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class SerialPortModule extends AbstractModule implements BlockChangeAware {
    // --------------------------------------------------------------------- //
    // Persisted data

    private short writing;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_VALUE = "value";
    private static final String TAG_SERIAL_INTERFACE = "serialInterface";

    private Optional<SerialInterface> serialInterface = Optional.empty();
    private Optional<CompoundTag> serialInterfaceNbt = Optional.empty();
    private boolean isScanScheduled = true;

    // --------------------------------------------------------------------- //

    public SerialPortModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // BlockChangeAware

    @Override
    public void onNeighborBlockChange(final BlockPos neighborPos, final boolean isModuleNeighbor) {
        if (isModuleNeighbor) {
            isScanScheduled = true;
        }
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        scan();
        stepOutput();
        stepInput();
    }

    @Override
    public void onDisabled() {
        // Reset serial interface on shutdown.
        serialInterface.ifPresent(SerialInterface::reset);
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Consume the read value (the one that was being written).
        serialInterface.ifPresent(SerialInterface::skip);

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure all our writes are in sync.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks) {
        if (!getCasing().isEnabled()) {
            return;
        }

        RenderUtil.ignoreLighting();

        RenderUtil.drawQuad(RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_SERIAL_PORT));
    }

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);

        writing = nbt.getShort(TAG_VALUE);

        if (nbt.contains(TAG_SERIAL_INTERFACE)) {
            if (serialInterface.isPresent()) {
                serialInterface.get().readFromNBT(nbt.getCompound(TAG_SERIAL_INTERFACE));
            } else {
                serialInterfaceNbt = Optional.of(nbt.getCompound(TAG_SERIAL_INTERFACE));
            }
        }
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);

        nbt.putShort(TAG_VALUE, writing);

        if (serialInterface.isPresent()) {
            final CompoundTag serialInterfaceNbt = new CompoundTag();
            serialInterface.get().writeToNBT(serialInterfaceNbt);
            if (!nbt.isEmpty()) {
                nbt.put(TAG_SERIAL_INTERFACE, serialInterfaceNbt);
            }
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Check if our serial interface is still valid.
     * <p>
     * Clears an existing interface if there's none available for the neighbor
     * block and creates one if there is and we don't have one yet.
     */
    private void scan() {
        // Only check for interface validity when necessary.
        if (!isScanScheduled) {
            return;
        }
        isScanScheduled = false;

        final World world = getCasing().getCasingWorld();
        final BlockPos neighborPos = getCasing().getPosition().offset(Face.toDirection(getFace()));
        final Direction neighborSide = Face.toDirection(getFace().getOpposite());
        if (true) {//~ if (world.isBlockLoaded(neighborPos)) {
            final SerialInterfaceProvider provider = SerialAPI.getProviderFor(world, neighborPos, neighborSide);
            if (provider != null) {
                if (!serialInterface.map(s -> provider.isValid(world, neighborPos, neighborSide, s)).orElse(false)) {
                    // Either we didn't have an interface for our neighbor yet,
                    // or the interface has become invalid, so create a new one.
                    reset();
                    serialInterface = Optional.ofNullable(provider.interfaceFor(world, neighborPos, neighborSide));
                    if (serialInterface.isPresent() && serialInterfaceNbt.isPresent()) {
                        serialInterface.get().readFromNBT(serialInterfaceNbt.get());
                        serialInterfaceNbt = Optional.empty(); // Done reading, don't re-use.
                    }
                } // else: interface still valid.
            } else {
                // No provider for neighbor, can't interact.
                reset();
            }
        } else {
            // Neighboring slot is not loaded, can't interact.
            reset();
        }
    }

    /**
     * Reset the state of the module, its ports, and, if there was one, the
     * serial interface, then clears it.
     */
    private void reset() {
        serialInterface.ifPresent(SerialInterface::reset);
        serialInterface = Optional.empty();
        cancelRead();
        cancelWrite();
    }

    /**
     * Update the output of the serial interface.
     */
    private void stepOutput() {
        if (serialInterface.map(SerialInterface::canRead).orElse(false)) {
            final short value = serialInterface.map(SerialInterface::peek).orElse((short)0);
            if (value != writing) {
                cancelWrite();
                writing = value;
            }
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    sendingPipe.beginWrite(writing);
                }
            }
        } else {
            // No interface or can't read from it.
            cancelWrite();
        }
    }

    /**
     * Update the inputs of the serial port, moving values to the present serial interface.
     */
    private void stepInput() {
        if (serialInterface.map(SerialInterface::canWrite).orElse(false)) {
            for (final Port port : Port.VALUES) {
                // Continuously read from all ports, write last received value.
                final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
                if (!receivingPipe.isReading()) {
                    receivingPipe.beginRead();
                }
                if (receivingPipe.canTransfer()) {
                    // Forward the value.
                    serialInterface.ifPresent(s -> s.write(receivingPipe.read()));
                }
            }
        } else {
            // No interface or can't write to it.
            cancelRead();
        }
    }
}
