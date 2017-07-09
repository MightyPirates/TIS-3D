package li.cil.tis3d.common.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import li.cil.tis3d.client.renderer.TextureLoader;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * The stack module can be used to store a number of values to be retrieved
 * later on. It operates as LIFO queue, providing the top element to all ports
 * but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class ModuleSerialPort extends AbstractModule implements BlockChangeAware {
    // --------------------------------------------------------------------- //
    // Persisted data

    private short writing;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_VALUE = "value";
    private static final String TAG_SERIAL_INTERFACE = "serialInterface";

    private Optional<SerialInterface> serialInterface = Optional.empty();
    private Optional<NBTTagCompound> serialInterfaceNbt = Optional.empty();
    private boolean isScanScheduled = true;

    // --------------------------------------------------------------------- //

    public ModuleSerialPort(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // BlockChangeAware

    @Override
    public void onNeighborBlockChange(final Block neighborBlock) {
        isScanScheduled = true;
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
    public void onWriteComplete(final Port port) {
        // Consume the read value (the one that was being written).
        serialInterface.ifPresent(SerialInterface::skip);

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        RenderUtil.ignoreLighting();

        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_SERIAL_PORT_OVERLAY));
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        writing = nbt.getShort(TAG_VALUE);

        if (nbt.hasKey(TAG_SERIAL_INTERFACE)) {
            if (serialInterface.isPresent()) {
                serialInterface.get().readFromNBT(nbt.getCompoundTag(TAG_SERIAL_INTERFACE));
            } else {
                serialInterfaceNbt = Optional.of(nbt.getCompoundTag(TAG_SERIAL_INTERFACE));
            }
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setShort(TAG_VALUE, writing);

        if (serialInterface.isPresent()) {
            final NBTTagCompound serialInterfaceNbt = new NBTTagCompound();
            serialInterface.get().writeToNBT(serialInterfaceNbt);
            if (!nbt.hasNoTags()) {
                nbt.setTag(TAG_SERIAL_INTERFACE, serialInterfaceNbt);
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
        final int neighborX = getCasing().getPositionX() + Face.toEnumFacing(getFace()).getFrontOffsetX();
        final int neighborY = getCasing().getPositionY() + Face.toEnumFacing(getFace()).getFrontOffsetY();
        final int neighborZ = getCasing().getPositionZ() + Face.toEnumFacing(getFace()).getFrontOffsetZ();
        final EnumFacing neighborSide = Face.toEnumFacing(getFace().getOpposite());
        if (world.blockExists(neighborX, neighborY, neighborZ)) {
            final SerialInterfaceProvider provider = SerialAPI.getProviderFor(world, neighborX, neighborY, neighborZ, neighborSide);
            if (provider != null) {
                if (!serialInterface.map(s -> provider.isValid(world, neighborX, neighborY, neighborZ, neighborSide, s)).orElse(false)) {
                    // Either we didn't have an interface for our neighbor yet,
                    // or the interface has become invalid, so create a new one.
                    reset();
                    serialInterface = Optional.ofNullable(provider.interfaceFor(world, neighborX, neighborY, neighborZ, neighborSide));
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
            final short value = serialInterface.map(SerialInterface::peek).orElse((short) 0);
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
     * Update the inputs of the stack, pulling values onto the stack.
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

                    // Start reading again right away to read as fast as possible.
                    if (serialInterface.map(SerialInterface::canWrite).orElse(false)) {
                        receivingPipe.beginRead();
                    }
                }
            }
        } else {
            // No interface or can't write to it.
            cancelRead();
        }
    }
}
