package li.cil.tis3d.system;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Implementation of {@link Pipe}s for passing data between {@link Module}s.
 */
public final class PipeImpl implements Pipe {
    /**
     * Current state of the pipe, to enforce synchronization, i.e. make sure
     * each read/write combination always takes the same amount of steps
     * regardless of whether reader or writer ran first (when they start in
     * the same step).
     */
    private enum State {
        /**
         * Waiting for a reader.
         */
        IDLE,

        /**
         * Reader registered in this update.
         */
        BUSY,

        /**
         * Reader has registered and is ready to receive.
         */
        READY,

        /**
         * Data was read from the pipe this update.
         */
        FLUSHING
    }

    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The current state of the pipe.
     */
    private State readState = State.IDLE, writeState = State.IDLE;

    /**
     * The value currently being written over this pipe.
     */
    private int value = Integer.MIN_VALUE;

    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * The casing this pipe belongs to.
     */
    private final Casing casing;

    /**
     * The faces this pipe is connected to in the owning {@link li.cil.tis3d.api.Casing}.
     */
    private final Face inputFace, outputFace;

    /**
     * The input port of this pipe in the owning {@link li.cil.tis3d.api.Casing}.
     */
    private final Port outputPort;

    // --------------------------------------------------------------------- //

    public PipeImpl(final Casing casing, final Face inputFace, final Face outputFace, final Port outputPort) {
        this.casing = casing;
        this.inputFace = inputFace;
        this.outputFace = outputFace;
        this.outputPort = outputPort;
    }

    /**
     * Called from the owning {@link li.cil.tis3d.api.Casing} after
     * all {@link Module}s have been updated to update the pipe's
     * state in a synchronized manner.
     */
    public void step() {
        if (writeState == State.BUSY) {
            writeState = State.READY;
        } else if (readState == State.BUSY) {
            readState = State.READY;
        } else if (writeState == State.READY && readState == State.READY) {
            writeState = State.FLUSHING;
            readState = State.FLUSHING;
        }
    }

    public void readFromNBT(final NBTTagCompound nbt) {
        readState = State.valueOf(nbt.getString("readState"));
        writeState = State.valueOf(nbt.getString("writeState"));
        value = nbt.getInteger("value");
    }

    public void writeToNBT(final NBTTagCompound nbt) {
        nbt.setString("readState", readState.name());
        nbt.setString("writeState", writeState.name());
        nbt.setInteger("value", value);
    }

    // --------------------------------------------------------------------- //
    // Pipe

    @Override
    public void beginWrite(final int value) {
        if (writeState != State.IDLE) {
            throw new IllegalStateException("Trying to write to a busy pipe. Check isWriting().");
        }
        writeState = State.BUSY;
        this.value = value;
    }

    @Override
    public void cancelWrite() {
        writeState = State.IDLE;
        value = Integer.MIN_VALUE;
        if (readState == State.FLUSHING) {
            readState = State.READY;
        }
    }

    @Override
    public boolean isWriting() {
        return writeState != State.IDLE;
    }

    @Override
    public void beginRead() {
        if (readState != State.IDLE) {
            throw new IllegalStateException("Trying to write to a busy pipe. Check isReading().");
        }
        readState = State.BUSY;
    }

    @Override
    public void cancelRead() {
        readState = State.IDLE;
        if (writeState == State.FLUSHING) {
            writeState = State.READY;
        }
    }

    @Override
    public boolean isReading() {
        return readState != State.IDLE;
    }

    @Override
    public boolean canTransfer() {
        return writeState == State.FLUSHING && readState == State.FLUSHING;
    }

    @Override
    public int read() {
        if (!canTransfer()) {
            throw new IllegalStateException("No data to read. Check canTransfer().");
        }

        final BlockPos position = casing.getPosition();
        final double ox = Face.toEnumFacing(inputFace).getFrontOffsetX() + Face.toEnumFacing(outputFace).getFrontOffsetX();
        final double oy = Face.toEnumFacing(inputFace).getFrontOffsetY() + Face.toEnumFacing(outputFace).getFrontOffsetY();
        final double oz = Face.toEnumFacing(inputFace).getFrontOffsetZ() + Face.toEnumFacing(outputFace).getFrontOffsetZ();
        final double x = ox * 0.6 + position.getX() + 0.5;
        final double y = oy * 0.6 + position.getY() + 0.5;
        final double z = oz * 0.6 + position.getZ() + 0.5;

        final World world = casing.getWorld();
        final MessageParticleEffect message = new MessageParticleEffect(world, EnumParticleTypes.REDSTONE, x, y, z);
        final NetworkRegistry.TargetPoint target = Network.getTargetPoint(world, x, y, z, Network.RANGE_LOW);
        Network.INSTANCE.getWrapper().sendToAllAround(message, target);

        final int result = value;

        cancelWrite();
        cancelRead();

        casing.getModule(outputFace).onWriteComplete(outputPort);
        return result;
    }
}
