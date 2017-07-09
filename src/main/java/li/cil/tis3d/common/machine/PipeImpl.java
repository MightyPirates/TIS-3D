package li.cil.tis3d.common.machine;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Implementation of {@link Pipe}s for passing data between {@link Module}s.
 */
public final class PipeImpl implements Pipe {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The current state of the pipe.
     */
    private State readState = State.IDLE, writeState = State.IDLE;

    /**
     * The value currently being written over this pipe.
     */
    private short value = 0;

    // --------------------------------------------------------------------- //
    // Computed data

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

    // NBT tag names.
    private static final String TAG_READ_STATE = "readState";
    private static final String TAG_WRITE_STATE = "writeState";
    private static final String TAG_VALUE = "value";

    /**
     * The container this pipe belongs to.
     */
    private final PipeHost host;

    /**
     * The faces this pipe is connected to in the owning {@link Casing}.
     */
    private final Face receivingFace, sendingFace;

    /**
     * The input port of this pipe in the owning {@link Casing}.
     */
    private final Port sendingPort;

    // --------------------------------------------------------------------- //

    public PipeImpl(final PipeHost host, final Face receivingFace, final Face sendingFace, final Port sendingPort) {
        this.host = host;
        this.receivingFace = receivingFace;
        this.sendingFace = sendingFace;
        this.sendingPort = sendingPort;
    }

    /**
     * Called from the owning {@link Casing} after
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
        readState = EnumUtils.readFromNBT(State.class, TAG_READ_STATE, nbt);
        writeState = EnumUtils.readFromNBT(State.class, TAG_WRITE_STATE, nbt);
        value = nbt.getShort(TAG_VALUE);
    }

    public void writeToNBT(final NBTTagCompound nbt) {
        EnumUtils.writeToNBT(readState, TAG_READ_STATE, nbt);
        EnumUtils.writeToNBT(writeState, TAG_WRITE_STATE, nbt);
        nbt.setShort(TAG_VALUE, value);
    }

    // --------------------------------------------------------------------- //
    // Pipe

    @Override
    public void beginWrite(final short value) {
        if (writeState != State.IDLE) {
            throw new IllegalStateException("Trying to write to a busy pipe. Check isWriting().");
        }
        writeState = State.BUSY;
        this.value = value;
    }

    @Override
    public void cancelWrite() {
        writeState = State.IDLE;
        value = 0;
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
    public short read() {
        if (!canTransfer()) {
            throw new IllegalStateException("No data to read. Check canTransfer().");
        }

        sendEffect();

        final short result = value;

        cancelWrite();
        cancelRead();

        host.onWriteComplete(sendingFace, sendingPort);
        return result;
    }

    private void sendEffect() {
        final double ox = Face.toEnumFacing(receivingFace).getFrontOffsetX() + Face.toEnumFacing(sendingFace).getFrontOffsetX();
        final double oy = Face.toEnumFacing(receivingFace).getFrontOffsetY() + Face.toEnumFacing(sendingFace).getFrontOffsetY();
        final double oz = Face.toEnumFacing(receivingFace).getFrontOffsetZ() + Face.toEnumFacing(sendingFace).getFrontOffsetZ();
        final double x = ox * 0.55 + host.getPipeHostPositionX() + 0.5;
        final double y = oy * 0.55 + host.getPipeHostPositionY() + 0.5;
        final double z = oz * 0.55 + host.getPipeHostPositionZ() + 0.5;
        final double extraOffsetY = oy < 0 ? -0.2 : (oy > 0) ? 0.1 : 0;

        Network.sendPipeEffect(host.getPipeHostWorld(), x, y + extraOffsetY, z);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public String toString() {
        return "{" + host.getPipeHostPositionX() + ", " + host.getPipeHostPositionY() + ", " + host.getPipeHostPositionZ() + "}: " + sendingFace + " [" + writeState + "] -> " + receivingFace + " [" + readState + "]";
    }
}
